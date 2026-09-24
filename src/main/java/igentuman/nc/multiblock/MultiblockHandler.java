package igentuman.nc.multiblock;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.network.PacketMultiblockBroken;
import igentuman.nc.network.PacketMultiblockFormed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
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
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns every multiblock instance: worker validation, change observation and server-side publication. */
@EventBusSubscriber(modid = NuclearCraft.MODID)
public final class MultiblockHandler {

    private static final Map<ResourceKey<Level>, Map<Long, MultiblockInstance>> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, Set<MultiblockInstance>>> WATCHERS = new ConcurrentHashMap<>();

    private static final long IDLE_REVALIDATION_INTERVAL = 200;
    private static final long MAX_IDLE_REVALIDATION_INTERVAL = 6000;
    private static final long CELLS_PER_IDLE_INTERVAL = 32_768;
    private static final int INCOMPLETE_RETRY_INTERVAL = 40;

    private MultiblockHandler() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        WATCHERS.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        MultiblockExecutorManager.getExecutor();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        INSTANCES.remove(dim);
        WATCHERS.remove(dim);
        MultiblockWorldInput.discard(dim);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent event) {
        INSTANCES.clear();
        WATCHERS.clear();
        MultiblockWorldInput.discardAll();
        MultiblockExecutorManager.shutdown();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        MultiblockWorldInput.service(level);
    }

    public static void trackBlockChange(Level level, BlockPos pos, BlockState previousState, BlockState currentState) {
        if (level.isClientSide() || !isStructuralChange(previousState, currentState)) return;
        observeBlockChange(level, pos);
    }

    static boolean isStructuralChange(BlockState previousState, BlockState currentState) {
        return !StructuralBlockState.equivalent(previousState, currentState);
    }

    public static void observeBlockChange(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()) return;
        notifyWatchers(server, pos);
    }

    @SubscribeEvent
    public static void onChunkUnload(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Map<Long, Set<MultiblockInstance>> watchers = WATCHERS.get(level.dimension());
        if (watchers == null) return;
        ChunkPos chunkPos = event.getChunk().getPos();
        Set<MultiblockInstance> instances = watchers.get(chunkPos.toLong());
        if (instances == null) return;
        for (MultiblockInstance instance : instances) {
            instance.cache.invalidateChunk(chunkPos.x, chunkPos.z);
            instance.generation++;
            instance.dirty = true;
        }
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) observeBlockChange(level, event.getPos());
    }

    private static void observeBeforeChange(ServerLevel level, BlockPos pos) {
        if (isWatched(level, pos) && level.hasChunkAt(pos)) observeBlockChange(level, pos);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        trackBlockChange(serverLevel, event.getPos(), event.getState(),
                event.getState().getFluidState().createLegacyBlock());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multiPlaceEvent) {
            multiPlaceEvent.getReplacedBlockSnapshots().forEach(snapshot ->
                    trackBlockChange(serverLevel, snapshot.getPos(), snapshot.getState(), snapshot.getCurrentState()));
            return;
        }
        trackBlockChange(serverLevel, event.getPos(), event.getBlockSnapshot().getState(), event.getPlacedBlock());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        for (BlockPos pos : event.getAffectedBlocks()) {
            observeBeforeChange(serverLevel, pos);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (event.isCanceled()) return;
        if (!mayReachTrackedSection(serverLevel, event.getPos())) return;
        observeBeforeChange(serverLevel, event.getPos());
        observeBeforeChange(serverLevel, event.getFaceOffsetPos());
        var resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;
        Direction movement = event.getPistonMoveType().isExtend ? event.getDirection() : event.getDirection().getOpposite();
        for (BlockPos pos : resolver.getToPush()) {
            observeBeforeChange(serverLevel, pos);
            observeBeforeChange(serverLevel, pos.relative(movement));
        }
        for (BlockPos pos : resolver.getToDestroy()) observeBeforeChange(serverLevel, pos);
    }

    private static boolean mayReachTrackedSection(ServerLevel level, BlockPos base) {
        Map<Long, Set<MultiblockInstance>> watchers = WATCHERS.get(level.dimension());
        if (watchers == null) return false;
        for (int x = (base.getX() - 13) >> 4; x <= (base.getX() + 13) >> 4; x++) {
            for (int z = (base.getZ() - 13) >> 4; z <= (base.getZ() + 13) >> 4; z++) {
                if (watchers.containsKey(ChunkPos.asLong(x, z))) return true;
            }
        }
        return false;
    }

    private static boolean isWatched(ServerLevel level, BlockPos pos) {
        Map<Long, Set<MultiblockInstance>> watchers = WATCHERS.get(level.dimension());
        return watchers != null && watchers.containsKey(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
    }

    /** Initialize a new multiblock at the controller position. Validation happens on the next tick. */
    public static MultiblockInstance initMultiblock(ServerLevel level, BlockPos controllerPos, Direction facing, MultiblockEntry entry) {
        return register(level, controllerPos, new MultiblockInstance(entry, facing));
    }

    public static MultiblockInstance restoreMultiblock(ServerLevel level, BlockPos controllerPos, Direction facing,
                                                       MultiblockEntry entry, CompoundTag cacheNbt, HolderLookup.Provider registries) {
        MultiblockInstance instance = new MultiblockInstance(entry, facing);
        if (cacheNbt != null) instance.cache.loadNbt(cacheNbt, registries);
        return register(level, controllerPos, instance);
    }

    private static MultiblockInstance register(ServerLevel level, BlockPos controllerPos, MultiblockInstance instance) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        instance.cache.bind(controllerPos, instance.facing);
        instance.dirty = true;
        watchChunk(dim, instance, ChunkPos.asLong(controllerPos.getX() >> 4, controllerPos.getZ() >> 4));
        BlockPos min = instance.cache.min();
        BlockPos max = instance.cache.max();
        if (min != null && max != null) watchBounds(dim, instance, min, max);
        MultiblockInstance replaced = map.put(controllerPos.asLong(), instance);
        if (replaced != null) {
            replaced.disposed = true;
            unwatchAll(dim, replaced);
        }
        return instance;
    }

    /** Destroy a multiblock. Called on controller-block removal. */
    public static void destroyMultiblock(ServerLevel level, BlockPos controllerPos) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.get(dim);
        MultiblockInstance instance = map == null ? null : map.remove(controllerPos.asLong());
        if (instance == null) return;
        instance.disposed = true;
        instance.generation++;
        unwatchAll(dim, instance);
        if (instance.formed) {
            instance.logic.onBroken(level, controllerPos, instance.cache);
            sendBroken(level, controllerPos);
        }
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
        tickInstance(level, instance, controllerPos);
    }

    private static void tickInstance(ServerLevel level, MultiblockInstance instance, BlockPos controllerPos) {
        AbstractMultiblockCache cache = instance.cache;
        long gameTime = level.getGameTime();
        if (gameTime < instance.retryAfterTick) return;
        long stagger = controllerPos.asLong();
        boolean integrity = false;
        if (!instance.formed) {
            if (gameTime % INCOMPLETE_RETRY_INTERVAL != Math.floorMod(stagger, INCOMPLETE_RETRY_INTERVAL)) return;
        } else if (!instance.dirty) {
            runLogicTick(level, instance, controllerPos, cache);
            long interval = idleRevalidationInterval(cache);
            if (gameTime % interval != Math.floorMod(stagger, interval)) return;
            integrity = true;
        }
        if (!instance.busy.compareAndSet(false, true)) return;
        long generation = instance.generation;
        boolean integrityCheck = integrity;
        boolean refresh = integrityCheck || instance.forceRefresh;
        instance.forceRefresh = false;
        Set<Long> changes = Set.copyOf(instance.pendingChanges);
        instance.pendingChanges.removeAll(changes);
        cache.bind(controllerPos, instance.facing);
        cache.bindInput(sectionKeys -> MultiblockWorldInput.fetch(level, sectionKeys,
                sectionKey -> watchSection(level, instance, sectionKey)));
        try {
            MultiblockExecutorManager.getExecutor().execute(() -> {
                try {
                    instance.validateOffThread(level, controllerPos, generation, changes, refresh, integrityCheck);
                } catch (Throwable error) {
                    NuclearCraft.LOGGER.error("Multiblock validation error at {}", controllerPos, error);
                    level.getServer().execute(() -> {
                        instance.dirty = true;
                        instance.busy.set(false);
                    });
                }
            });
        } catch (Throwable rejected) {
            if (!integrityCheck) {
                instance.dirty = true;
                instance.pendingChanges.addAll(changes);
                instance.forceRefresh |= refresh;
            }
            instance.busy.set(false);
        }
    }

    private static long idleRevalidationInterval(AbstractMultiblockCache cache) {
        BlockPos min = cache.min();
        BlockPos max = cache.max();
        if (min == null || max == null) return IDLE_REVALIDATION_INTERVAL;
        long cells = (long) (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1)
                * (max.getZ() - min.getZ() + 1);
        long multiplier = 1 + cells / CELLS_PER_IDLE_INTERVAL;
        return Math.min(MAX_IDLE_REVALIDATION_INTERVAL, IDLE_REVALIDATION_INTERVAL * multiplier);
    }

    private static void runLogicTick(ServerLevel level, MultiblockInstance instance, BlockPos controllerPos,
                                     AbstractMultiblockCache cache) {
        try {
            instance.logic.tickServer(level, controllerPos, cache);
        } catch (Throwable error) {
            NuclearCraft.LOGGER.error("Multiblock logic error at {}", controllerPos, error);
        }
    }

    static void watchSection(ServerLevel level, MultiblockInstance instance, long sectionKey) {
        watchChunk(level.dimension(), instance,
                ChunkPos.asLong(SectionPos.x(sectionKey), SectionPos.z(sectionKey)));
    }

    static void watchChunk(ResourceKey<Level> dim, MultiblockInstance instance, long chunkKey) {
        if (instance.disposed || !instance.watchedChunks.add(chunkKey)) return;
        WATCHERS.computeIfAbsent(dim, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, ignored -> ConcurrentHashMap.newKeySet())
                .add(instance);
    }

    static void watchBounds(ResourceKey<Level> dim, MultiblockInstance instance, BlockPos min, BlockPos max) {
        for (int x = min.getX() >> 4; x <= max.getX() >> 4; x++) {
            for (int z = min.getZ() >> 4; z <= max.getZ() >> 4; z++) {
                watchChunk(dim, instance, ChunkPos.asLong(x, z));
            }
        }
    }

    static void unwatchAll(ResourceKey<Level> dim, MultiblockInstance instance) {
        Map<Long, Set<MultiblockInstance>> watchers = WATCHERS.get(dim);
        if (watchers != null) {
            for (long chunkKey : instance.watchedChunks) {
                Set<MultiblockInstance> set = watchers.get(chunkKey);
                if (set == null) continue;
                set.remove(instance);
                if (set.isEmpty()) watchers.remove(chunkKey);
            }
        }
        instance.watchedChunks.clear();
    }

    private static void notifyWatchers(ServerLevel level, BlockPos pos) {
        Map<Long, Set<MultiblockInstance>> watchers = WATCHERS.get(level.dimension());
        if (watchers == null) return;
        Set<MultiblockInstance> instances = watchers.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (instances == null || instances.isEmpty()) return;
        for (MultiblockInstance instance : instances) instance.observeChange(level, pos);
    }

    public static MultiblockInstance getInstance(ServerLevel level, BlockPos controllerPos) {
        Map<Long, MultiblockInstance> map = INSTANCES.get(level.dimension());
        return map == null ? null : map.get(controllerPos.asLong());
    }

    public static BlockPos getControllerForPos(ServerLevel level, BlockPos pos) {
        Map<Long, Set<MultiblockInstance>> watchers = WATCHERS.get(level.dimension());
        Set<MultiblockInstance> watching = watchers == null ? null
                : watchers.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (watching == null) return null;
        long key = pos.asLong();
        BlockPos match = null;
        for (MultiblockInstance instance : watching) {
            if (!instance.formed || instance.dirty || instance.disposed) continue;
            if (instance.cache.ports().contains(key)) return instance.cache.controllerPos();
            if (match == null && instance.cache.contains(pos)) match = instance.cache.controllerPos();
        }
        return match;
    }

    static void sendFormed(ServerLevel level, BlockPos controllerPos, AbstractMultiblockCache cache) {
        long[] arr;
        boolean aabb = cache.hasAABB();
        if (aabb) {
            arr = new long[]{cache.aabbMinPacked(), cache.aabbMaxPacked()};
        } else {
            Set<Long> positions = cache.getStructurePositions();
            arr = new long[positions.size()];
            int i = 0;
            for (long p : positions) arr[i++] = p;
        }
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(controllerPos),
                new PacketMultiblockFormed(controllerPos, arr, aabb));
    }

    static void sendBroken(ServerLevel level, BlockPos controllerPos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(controllerPos),
                new PacketMultiblockBroken(controllerPos));
    }

    /** Internal instance state. Exposed only via {@link #getInstance}. */
    public static final class MultiblockInstance {
        public final MultiblockEntry entry;
        public final AbstractMultiblockValidator<?> validator;
        public final AbstractMultiblockLogic<?> logic;
        public final AbstractMultiblockCache cache;
        public final Direction facing;
        public volatile boolean formed;
        volatile boolean disposed;
        public volatile boolean dirty;
        volatile long generation;
        final AtomicBoolean busy = new AtomicBoolean(false);
        final Set<Long> pendingChanges = ConcurrentHashMap.newKeySet();
        final Set<Long> watchedChunks = ConcurrentHashMap.newKeySet();
        volatile boolean forceRefresh;
        volatile long retryAfterTick;
        public volatile AbstractMultiblockValidator.Result lastResult;

        MultiblockInstance(MultiblockEntry entry, Direction facing) {
            this.entry = entry;
            this.validator = entry.validatorSupplier().get();
            this.logic = entry.logicSupplier().get();
            this.cache = entry.cacheSupplier().get();
            this.facing = facing;
            this.formed = false;
        }

        void observeChange(ServerLevel level, BlockPos pos) {
            if (disposed) return;
            AbstractMultiblockCache abstractCache = cache;
            if (formed && !abstractCache.containsExpanded(pos, 1)) return;
            BlockState known = abstractCache.knownState(pos);
            if (known == null && formed && !busy.get()) return;
            if (known != null && level.hasChunkAt(pos)
                    && StructuralBlockState.equivalent(known, level.getBlockState(pos))) return;
            abstractCache.invalidate(pos);
            pendingChanges.add(pos.asLong());
            generation++;
            dirty = true;
        }

        void validateOffThread(ServerLevel level, BlockPos controllerPos, long startedGeneration,
                               Set<Long> changes, boolean refresh, boolean integrity) {
            AbstractMultiblockCache abstractCache = cache;
            if (disposed) {
                level.getServer().execute(() -> busy.set(false));
                return;
            }
            if (refresh) abstractCache.refreshInputs();
            else for (long key : changes) abstractCache.invalidate(BlockPos.of(key));
            if (!integrity) MultiblockDebug.beginTrace(NuclearCraft.rl(entry.name()), controllerPos);
            long started = System.nanoTime();
            AbstractMultiblockValidator.Result result;
            try {
                result = validator.runUnchecked(abstractCache);
            } finally {
                MultiblockDebug.endTrace();
            }
            long elapsed = System.nanoTime() - started;
            level.getServer().execute(() ->
                    publish(level, controllerPos, startedGeneration, result, changes, refresh, integrity, elapsed));
        }

        private void publish(ServerLevel level, BlockPos controllerPos, long startedGeneration,
                             AbstractMultiblockValidator.Result result, Set<Long> changes, boolean refresh,
                             boolean integrity, long elapsed) {
            AbstractMultiblockCache abstractCache = cache;
            try {
                if (disposed) return;
                if (generation != startedGeneration) {
                    dirty = true;
                    pendingChanges.addAll(changes);
                    forceRefresh |= refresh;
                    return;
                }
                switch (result.outcome()) {
                    case WAITING -> {
                        if (!integrity) {
                            dirty = true;
                            retryAfterTick = level.getGameTime() + INCOMPLETE_RETRY_INTERVAL;
                        }
                    }
                    case INVALID -> {
                        dirty = false;
                        lastResult = result;
                        if (formed) {
                            formed = false;
                            logic.onBroken(level, controllerPos, abstractCache);
                            sendBroken(level, controllerPos);
                        }
                    }
                    case VALID -> {
                        Set<Long> previousPorts = abstractCache.ports();
                        boolean wasFormed = formed;
                        abstractCache.publish();
                        BlockPos min = abstractCache.min();
                        BlockPos max = abstractCache.max();
                        if (min != null && max != null) watchBounds(level.dimension(), this, min, max);
                        dirty = false;
                        lastResult = result;
                        formed = true;
                        if (!wasFormed) {
                            logic.onFormed(level, controllerPos, abstractCache);
                            sendFormed(level, controllerPos, abstractCache);
                        } else {
                            Set<Long> removed = new HashSet<>(previousPorts);
                            removed.removeAll(abstractCache.ports());
                            logic.onPortsChangedUnchecked(level, controllerPos, abstractCache, removed);
                        }
                    }
                }
                if (!integrity || !result.valid()) {
                    MultiblockDebug.finishValidation(level, NuclearCraft.rl(entry.name()), controllerPos,
                            abstractCache, result, elapsed);
                }
            } finally {
                busy.set(false);
            }
        }
    }
}
