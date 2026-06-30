package igentuman.nc.multiblock;

import igentuman.nc.Main;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockLogic;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.network.PacketMultiblockBroken;
import igentuman.nc.network.PacketMultiblockFormed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static igentuman.nc.Main.TICK_COUNTER;

/**
 * Singleton manager: per-level executor for off-main-thread {@code tickServer},
 * active instances by controller position, and a structure-position index for O(1)
 * block-change lookup.
 */
@EventBusSubscriber(modid = Main.MODID)
public final class MultiblockHandler {

    private static final Map<ResourceKey<Level>, ExecutorService> LEVEL_THREADS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, MultiblockInstance>> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, Long>> STRUCTURE_INDEX = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, BlockState>> PENDING_CHANGES = new ConcurrentHashMap<>();

    private MultiblockHandler() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        LEVEL_THREADS.computeIfAbsent(dim, k -> Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "multiblock-" + k.location());
            t.setDaemon(true);
            return t;
        }));
        INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        STRUCTURE_INDEX.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        PENDING_CHANGES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        ExecutorService ex = LEVEL_THREADS.remove(dim);
        if (ex != null) ex.shutdownNow();
        INSTANCES.remove(dim);
        STRUCTURE_INDEX.remove(dim);
        PENDING_CHANGES.remove(dim);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        Map<Long, BlockState> pending = PENDING_CHANGES.get(dim);
        if (pending == null || pending.isEmpty()) return;
        Map<Long, BlockState> snapshot = new HashMap<>(pending);
        pending.clear();
        Map<Long, Long> idx = STRUCTURE_INDEX.get(dim);
        if (idx == null) return;
        for (Map.Entry<Long, BlockState> e : snapshot.entrySet()) {
            Long controllerKey = idx.get(e.getKey());
            if (controllerKey == null) continue;
            MultiblockInstance instance = INSTANCES.getOrDefault(dim, Collections.emptyMap()).get(controllerKey);
            if (instance != null) {
                instance.onStructureBlockChanged(serverLevel, BlockPos.of(controllerKey), BlockPos.of(e.getKey()));
            }
        }
    }

    public static void trackBlockChange(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        PENDING_CHANGES.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>()).put(pos.asLong(), state);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        handleBlockChange(serverLevel, event.getPos());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        handleBlockChange(serverLevel, event.getPos());
    }

    private static void handleBlockChange(ServerLevel level, BlockPos changed) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, Long> idx = STRUCTURE_INDEX.get(dim);
        if (idx == null) return;
        Long controllerKey = idx.get(changed.asLong());
        if (controllerKey == null) return;
        MultiblockInstance instance = INSTANCES.getOrDefault(dim, Collections.emptyMap()).get(controllerKey);
        if (instance != null) instance.onStructureBlockChanged(level, BlockPos.of(controllerKey), changed);
    }

    /** Initialize a new multiblock at the controller position. Attempts immediate validation. */
    public static MultiblockInstance initMultiblock(ServerLevel level, BlockPos controllerPos, Direction facing, MultiblockEntry entry) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        MultiblockInstance instance = new MultiblockInstance(entry, facing);
        map.put(controllerPos.asLong(), instance);
        return instance;
    }

    /** Destroy a multiblock. Called on controller-block removal. */
    public static void destroyMultiblock(ServerLevel level, BlockPos controllerPos) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.get(dim);
        if (map == null) return;
        MultiblockInstance instance = map.remove(controllerPos.asLong());
        if (instance == null) return;
        if (instance.formed) {
            removeFromStructureIndex(dim, instance.cache.getStructurePositions());
            instance.logic.onBroken(level, controllerPos, instance.cache);
            sendBroken(level, controllerPos);
        }
        instance.cache.clear();
    }

    /** Restore an instance on world load from a serialized cache tag. No re-validation. */
    public static void restoreMultiblock(ServerLevel level, BlockPos controllerPos, Direction facing,
                                          MultiblockEntry entry, CompoundTag cacheNbt, HolderLookup.Provider registries) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        MultiblockInstance instance = new MultiblockInstance(entry, facing);
        if (cacheNbt != null) {
            instance.cache.loadNbt(cacheNbt, registries);
        }
        instance.formed = !instance.cache.getStructurePositions().isEmpty();
        if (instance.formed) {
            indexStructure(dim, controllerPos.asLong(), instance.cache.getStructurePositions());
            instance.logic.onFormed(level, controllerPos, instance.cache);
        }
        map.put(controllerPos.asLong(), instance);
    }

    /** Per-tick submission of {@code logic.tickServer} to the level executor. */
    public static void submitTick(ServerLevel level, BlockPos controllerPos) {
        Map<Long, MultiblockInstance> map = INSTANCES.get(level.dimension());
        if (map == null) return;
        MultiblockInstance instance = map.get(controllerPos.asLong());
        submitTick(level, instance, controllerPos);
    }

    public static void submitTick(ServerLevel level, MultiblockInstance instance, BlockPos controllerPos) {
        if (instance == null) {
            instance = getInstance(level, controllerPos);
        }
        if (instance == null) {
            return;
        }
        ExecutorService ex = LEVEL_THREADS.get(level.dimension());
        if (ex == null || ex.isShutdown()) return;
        IMultiblockLogic logic = instance.logic;
        IMultiblockCache cache = instance.cache;
        MultiblockInstance finalInstance = instance;
        ex.submit(() -> {
            try {
                if (!finalInstance.formed) {
                    if (TICK_COUNTER % 5 == 0) {
                        finalInstance.tryValidate(level, controllerPos);
                    }
                    return;
                }
                logic.tickServer(level, controllerPos, cache);
            } catch (Throwable t) {
                Main.LOGGER.error("Multiblock tickServer error at {}", controllerPos, t);
            }
        });
    }

    public static MultiblockInstance getInstance(ServerLevel level, BlockPos controllerPos) {
        Map<Long, MultiblockInstance> map = INSTANCES.get(level.dimension());
        return map == null ? null : map.get(controllerPos.asLong());
    }

    public static BlockPos getControllerForPos(ServerLevel level, BlockPos pos) {
        Map<Long, Long> idx = STRUCTURE_INDEX.get(level.dimension());
        if (idx == null) return null;
        Long controllerKey = idx.get(pos.asLong());
        if (controllerKey == null) return null;
        return BlockPos.of(controllerKey);
    }

    static void indexStructure(ResourceKey<Level> dim, long controllerKey, Set<Long> positions) {
        Map<Long, Long> idx = STRUCTURE_INDEX.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        for (long p : positions) idx.put(p, controllerKey);
    }

    static void removeFromStructureIndex(ResourceKey<Level> dim, Set<Long> positions) {
        Map<Long, Long> idx = STRUCTURE_INDEX.get(dim);
        if (idx == null) return;
        for (long p : positions) idx.remove(p);
    }

    static void sendFormed(ServerLevel level, BlockPos controllerPos, Set<Long> positions) {
        long[] arr = new long[positions.size()];
        int i = 0;
        for (long p : positions) arr[i++] = p;
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(controllerPos),
                new PacketMultiblockFormed(controllerPos, arr));
    }

    static void sendBroken(ServerLevel level, BlockPos controllerPos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(controllerPos),
                new PacketMultiblockBroken(controllerPos));
    }

    /** Internal instance state. Package-private - exposed only via {@link #getInstance}. */
    public static final class MultiblockInstance {
        public final MultiblockEntry entry;
        public final IMultiblockValidator validator;
        public final IMultiblockLogic logic;
        public final IMultiblockCache cache;
        public final Direction facing;
        public boolean formed;

        MultiblockInstance(MultiblockEntry entry, Direction facing) {
            this.entry = entry;
            this.validator = entry.validatorSupplier().get();
            this.logic = entry.logicSupplier().get();
            this.cache = entry.cacheSupplier().get();
            this.facing = facing;
            this.formed = false;
        }

        void tryValidate(ServerLevel level, BlockPos controllerPos) {
            Set<Long> previousPositions = new HashSet<>(cache.getStructurePositions());
            cache.clear();
            boolean valid = validator.validate(level, controllerPos, facing, cache);
            if (valid && !formed) {
                formed = true;
                indexStructure(level.dimension(), controllerPos.asLong(), cache.getStructurePositions());
                logic.onFormed(level, controllerPos, cache);
                sendFormed(level, controllerPos, cache.getStructurePositions());
            } else if (!valid && formed) {
                formed = false;
                removeFromStructureIndex(level.dimension(), previousPositions);
                cache.getStructurePositions().addAll(previousPositions);
                logic.onBroken(level, controllerPos, cache);
                sendBroken(level, controllerPos);
                cache.getStructurePositions().clear();
            }
        }

        void onStructureBlockChanged(ServerLevel level, BlockPos controllerPos, BlockPos changed) {
            cache.invalidate(changed);
            Set<Long> previousPositions = new HashSet<>(cache.getStructurePositions());
            boolean wasFormed = formed;
            boolean valid = validator.validate(level, controllerPos, facing, cache);
            if (wasFormed && !valid) {
                formed = false;
                removeFromStructureIndex(level.dimension(), previousPositions);
                cache.getStructurePositions().addAll(previousPositions);
                logic.onBroken(level, controllerPos, cache);
                sendBroken(level, controllerPos);
                cache.getStructurePositions().clear();
            } else if (!wasFormed && valid) {
                formed = true;
                indexStructure(level.dimension(), controllerPos.asLong(), cache.getStructurePositions());
                logic.onFormed(level, controllerPos, cache);
                sendFormed(level, controllerPos, cache.getStructurePositions());
            } else if (wasFormed && valid) {
                // positions could have shifted - refresh index
                removeFromStructureIndex(level.dimension(), previousPositions);
                indexStructure(level.dimension(), controllerPos.asLong(), cache.getStructurePositions());
            }
        }
    }
}
