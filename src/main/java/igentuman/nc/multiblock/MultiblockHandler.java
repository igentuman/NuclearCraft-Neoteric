package igentuman.nc.multiblock;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.impl.MultiblockCacheImpl;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockLogic;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.network.PacketMultiblockBroken;
import igentuman.nc.network.PacketMultiblockFormed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static igentuman.nc.NuclearCraft.TICK_COUNTER;

/** Manages all multiblock instances: off-thread validation/ticking on a shared pool, controller lookup, and structure-change handling. */
@EventBusSubscriber(modid = NuclearCraft.MODID)
public final class MultiblockHandler {

    private static final Map<ResourceKey<Level>, Map<Long, MultiblockInstance>> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, Long>> STRUCTURE_INDEX = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<Long>> PENDING_CHANGES = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<Long>> IGNORE_UPDATE = new ConcurrentHashMap<>();

    private MultiblockHandler() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        STRUCTURE_INDEX.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        PENDING_CHANGES.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet());
        IGNORE_UPDATE.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet());
        MultiblockExecutorManager.getExecutor();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        INSTANCES.remove(dim);
        STRUCTURE_INDEX.remove(dim);
        PENDING_CHANGES.remove(dim);
        IGNORE_UPDATE.remove(dim);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent event) {
        INSTANCES.clear();
        STRUCTURE_INDEX.clear();
        PENDING_CHANGES.clear();
        IGNORE_UPDATE.clear();
        MultiblockExecutorManager.shutdown();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        Set<Long> pending = PENDING_CHANGES.get(dim);
        if (pending == null || pending.isEmpty()) return;
        Set<Long> snapshot = new HashSet<>(pending);
        pending.removeAll(snapshot);
        Map<Long, Long> idx = STRUCTURE_INDEX.get(dim);
        Map<Long, MultiblockInstance> instances = INSTANCES.get(dim);
        if (idx == null || instances == null) return;
        for (long changedKey : snapshot) {
            Long controllerKey = idx.get(changedKey);
            if (controllerKey == null) continue;
            MultiblockInstance instance = instances.get(controllerKey);
            if (instance != null) instance.pendingChanges.add(changedKey);
        }
    }

    /** Record a structure-relevant block change unless the position is flagged to be ignored (one-shot). */
    public static void trackBlockChange(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        ResourceKey<Level> dim = level.dimension();
        Set<Long> ignore = IGNORE_UPDATE.get(dim);
        if (ignore != null && ignore.remove(pos.asLong())) return;
        PENDING_CHANGES.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet()).add(pos.asLong());
    }

    /** Suppress the next {@link #trackBlockChange} for this position (self-inflicted changes from port BEs). */
    public static void addIgnoreToUpdate(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        IGNORE_UPDATE.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos.asLong());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        trackBlockChange(serverLevel, event.getPos(), event.getState());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        trackBlockChange(serverLevel, event.getPos(), event.getPlacedBlock());
    }

    /** Initialize a new multiblock at the controller position. Validation happens on the next tick. */
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
        instance.disposed = true;
        MultiblockExecutorManager.getExecutor().submit(() -> {
            try {
                if (instance.formed) {
                    removeFromStructureIndex(dim, instance.cache.getStructurePositions());
                    instance.logic.onBroken(level, controllerPos, instance.cache);
                    sendBroken(level, controllerPos);
                }
                instance.cache.clear();
            } catch (Throwable t) {
                NuclearCraft.LOGGER.error("Multiblock destroy error at {}", controllerPos, t);
            }
        });
    }

    /** Per-tick submission of instance work (validation / structure-change / logic tick) to the shared pool. */
    public static void submitTick(ServerLevel level, BlockPos controllerPos) {
        Map<Long, MultiblockInstance> map = INSTANCES.get(level.dimension());
        if (map == null) return;
        submitTick(level, map.get(controllerPos.asLong()), controllerPos);
    }

    public static void submitTick(ServerLevel level, MultiblockInstance instance, BlockPos controllerPos) {
        if (instance == null) {
            instance = getInstance(level, controllerPos);
        }
        if (instance == null || instance.disposed) return;
        MultiblockInstance finalInstance = instance;
        if (!finalInstance.busy.compareAndSet(false, true)) return;
        ExecutorService ex = MultiblockExecutorManager.getExecutor();
        ex.submit(() -> {
            try {
                finalInstance.runWork(level, controllerPos);
            } catch (Throwable t) {
                NuclearCraft.LOGGER.error("Multiblock tick error at {}", controllerPos, t);
            } finally {
                finalInstance.busy.set(false);
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

    /** Internal instance state. Exposed only via {@link #getInstance}. */
    public static final class MultiblockInstance {
        public final MultiblockEntry entry;
        public final IMultiblockValidator validator;
        public final IMultiblockLogic logic;
        public final IMultiblockCache cache;
        public final Direction facing;
        public volatile boolean formed;
        volatile boolean disposed;
        final AtomicBoolean busy = new AtomicBoolean(false);
        final Set<Long> pendingChanges = ConcurrentHashMap.newKeySet();

        MultiblockInstance(MultiblockEntry entry, Direction facing) {
            this.entry = entry;
            this.validator = entry.validatorSupplier().get();
            this.logic = entry.logicSupplier().get();
            this.cache = entry.cacheSupplier().get();
            this.facing = facing;
            this.formed = false;
        }

        /** Single guarded work unit: structure changes take priority, then formation, then runtime logic. */
        void runWork(ServerLevel level, BlockPos controllerPos) {
            if (disposed) return;
            if (!pendingChanges.isEmpty()) {
                Set<Long> changed = new HashSet<>(pendingChanges);
                pendingChanges.removeAll(changed);
                onStructureBlockChanged(level, controllerPos, changed);
                return;
            }
            if (!formed) {
                if (TICK_COUNTER % 5 == 0) tryValidate(level, controllerPos);
                return;
            }
            logic.tickServer(level, controllerPos, cache);
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

        void onStructureBlockChanged(ServerLevel level, BlockPos controllerPos, Set<Long> changed) {
            for (long c : changed) cache.invalidate(BlockPos.of(c));
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
                removeFromStructureIndex(level.dimension(), previousPositions);
                indexStructure(level.dimension(), controllerPos.asLong(), cache.getStructurePositions());
            }
        }

        public BlockPos getCenter(MultiblockCacheImpl multiblockCache) {
            Set<Long> positions = multiblockCache.getStructurePositions();
            if (positions.isEmpty()) return BlockPos.ZERO;
            long sumX = 0, sumY = 0, sumZ = 0;
            for (long packed : positions) {
                BlockPos p = BlockPos.of(packed);
                sumX += p.getX();
                sumY += p.getY();
                sumZ += p.getZ();
            }
            int count = positions.size();
            return new BlockPos((int)(sumX / count), (int)(sumY / count), (int)(sumZ / count));
        }
    }
}
