package igentuman.nc.multiblock;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.impl.MultiblockCacheImpl;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockLogic;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.multiblock.validation.UnloadedStructureException;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static igentuman.nc.NuclearCraft.TICK_COUNTER;

/** Routes legacy multiblocks to their worker path and scheduled multiblocks to per-level server-thread work. */
@EventBusSubscriber(modid = NuclearCraft.MODID)
public final class MultiblockHandler {

    private static final Map<ResourceKey<Level>, Map<Long, MultiblockInstance>> INSTANCES = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, Set<Long>>> STRUCTURE_INDEX = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<Long, Integer>> TRACKED_SECTIONS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<Long>> PENDING_CHANGES = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, MultiblockLevelState> LEVEL_STATES = new ConcurrentHashMap<>();

    private MultiblockHandler() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        STRUCTURE_INDEX.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        PENDING_CHANGES.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet());
        LEVEL_STATES.put(dim, MultiblockLevelState.get(serverLevel));
        MultiblockExecutorManager.getExecutor();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        INSTANCES.remove(dim);
        STRUCTURE_INDEX.remove(dim);
        TRACKED_SECTIONS.remove(dim);
        PENDING_CHANGES.remove(dim);
        LEVEL_STATES.remove(dim);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent event) {
        INSTANCES.clear();
        STRUCTURE_INDEX.clear();
        TRACKED_SECTIONS.clear();
        PENDING_CHANGES.clear();
        LEVEL_STATES.clear();
        MultiblockExecutorManager.shutdown();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ResourceKey<Level> dim = level.dimension();
        Set<Long> pending = PENDING_CHANGES.get(dim);
        Map<Long, Set<Long>> index = STRUCTURE_INDEX.get(dim);
        Map<Long, MultiblockInstance> instances = INSTANCES.get(dim);
        Map<MultiblockInstance, Set<Long>> changed = new java.util.HashMap<>();
        var budget = igentuman.nc.config.Multiblocks.particleMachines().scheduler();
        int readLimit = Math.min(256, Math.max(1, budget.blockReadBudget() / 4));
        if (budget.blockReadBudget() == 1 && level.getGameTime() % 4 != 0) readLimit = 0;
        long started = System.nanoTime();
        long deadline = started + budget.timeBudgetNanos() / 4;
        int reads = 0;
        if (pending != null && index != null && instances != null) {
            Set<Long> moving = new HashSet<>();
            for (var iterator = pending.iterator(); iterator.hasNext() && reads < readLimit && System.nanoTime() < deadline;) {
                long key = iterator.next();
                iterator.remove();
                Set<Long> controllers = index.get(key);
                if (controllers == null) continue;
                BlockPos pos = BlockPos.of(key);
                for (long controller : controllers) {
                    MultiblockInstance instance = instances.get(controller);
                    if (instance != null) instance.candidates.remove(key);
                }
                if (!level.hasChunkAt(pos)) continue;
                BlockState current = level.getBlockState(pos);
                reads++;
                for (long controller : controllers) {
                    MultiblockInstance instance = instances.get(controller);
                    if (instance != null && instance.observations.observe(pos, current)) {
                        changed.computeIfAbsent(instance, ignored -> new HashSet<>()).add(key);
                    }
                }
                if (current.is(Blocks.MOVING_PISTON)) {
                    moving.add(key);
                    for (long controller : controllers) {
                        MultiblockInstance instance = instances.get(controller);
                        if (instance != null) instance.candidates.add(key);
                    }
                }
            }
            pending.addAll(moving);
            // Round-robin exact-cell integrity observation also seeds restored/missing baselines.
            int remaining = readLimit - reads;
            if (!instances.isEmpty() && remaining > 0) {
                var list = java.util.List.copyOf(instances.values());
                MultiblockInstance instance = list.get(Math.floorMod(level.getGameTime(), list.size()));
                reads += instance.observeIntegrity(level, remaining, deadline, changed);
            }
        }
        changed.forEach((instance, positions) -> {
            instance.generation++;
            instance.dirty = true;
            instance.pendingChanges.addAll(Set.copyOf(positions));
        });
        if (instances != null) {
            for (MultiblockInstance instance : instances.values()) {
                Runnable publication = instance.pendingPublication;
                if (publication != null && instance.candidates.isEmpty()) {
                    instance.pendingPublication = null;
                    try {
                        publication.run();
                    } catch (Throwable error) {
                        instance.dirty = true;
                        NuclearCraft.LOGGER.error("Multiblock publication failed", error);
                    } finally {
                        instance.busy.set(false);
                    }
                }
            }
        }
        MultiblockLevelState.get(level).tick(level, reads, System.nanoTime() - started);
    }

    public static void trackBlockChange(Level level, BlockPos pos, BlockState previousState, BlockState currentState) {
        if (level.isClientSide() || !isStructuralChange(previousState, currentState)) return;
        observeBlockChange(level, pos, previousState);
    }

    static boolean isStructuralChange(BlockState previousState, BlockState currentState) {
        return !StructuralBlockState.equivalent(previousState, currentState);
    }

    public static void observeBlockChange(Level level, BlockPos pos) {
        observeBlockChange(level, pos, null);
    }

    private static void observeBlockChange(Level level, BlockPos pos, BlockState previous) {
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()) return;
        MultiblockLevelState scheduled = LEVEL_STATES.get(level.dimension());
        if (scheduled != null) scheduled.observeChange(pos, previous);
        Map<Long, Set<Long>> index = STRUCTURE_INDEX.get(level.dimension());
        Set<Long> controllers = index == null ? null : index.get(pos.asLong());
        if (controllers == null) return;
        Map<Long, MultiblockInstance> instances = INSTANCES.get(level.dimension());
        for (long controller : controllers) {
            MultiblockInstance instance = instances == null ? null : instances.get(controller);
            if (instance != null) {
                instance.candidates.add(pos.asLong());
                if (previous != null) instance.observations.seed(pos, previous);
            }
        }
        PENDING_CHANGES.computeIfAbsent(level.dimension(), ignored -> ConcurrentHashMap.newKeySet()).add(pos.asLong());
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) observeBlockChange(level, event.getPos());
    }

    private static void observeBeforeChange(ServerLevel level, BlockPos pos) {
        MultiblockLevelState scheduled = LEVEL_STATES.get(level.dimension());
        Map<Long, Set<Long>> index = STRUCTURE_INDEX.get(level.dimension());
        if ((scheduled == null || !scheduled.tracks(pos)) && (index == null || !index.containsKey(pos.asLong()))) return;
        if (level.hasChunkAt(pos)) observeBlockChange(level, pos, level.getBlockState(pos));
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
        MultiblockLevelState state = LEVEL_STATES.get(level.dimension());
        Map<Long, Integer> legacy = TRACKED_SECTIONS.get(level.dimension());
        for (int x = (base.getX() - 13) >> 4; x <= (base.getX() + 13) >> 4; x++) {
            for (int y = (base.getY() - 13) >> 4; y <= (base.getY() + 13) >> 4; y++) {
                for (int z = (base.getZ() - 13) >> 4; z <= (base.getZ() + 13) >> 4; z++) {
                    long section = net.minecraft.core.SectionPos.asLong(x, y, z);
                    if ((state != null && state.sectionIndex().hasTrackedSection(section))
                            || (legacy != null && legacy.containsKey(section))) return true;
                }
            }
        }
        return false;
    }

    /** Initialize a new multiblock at the controller position. Validation happens on the next tick. */
    public static MultiblockInstance initMultiblock(ServerLevel level, BlockPos controllerPos, Direction facing, MultiblockEntry entry) {
        if (entry.executionStrategy() == MultiblockExecutionStrategy.SCHEDULED_SERVER_THREAD) {
            MultiblockLevelState.get(level).beginDiscovery(entry.scheduledDefinition(), controllerPos, facing);
            return null;
        }
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        MultiblockInstance instance = new MultiblockInstance(entry, facing);
        MultiblockInstance old = map.put(controllerPos.asLong(), instance);
        if (old != null) {
            old.disposed = true;
            removeFromStructureIndex(dim, controllerPos.asLong(), old.trackedPositions);
            indexStructure(dim, controllerPos.asLong(), instance.trackedPositions);
        }
        return instance;
    }

    public static MultiblockInstance restoreMultiblock(ServerLevel level, BlockPos controllerPos, Direction facing,
                                                       MultiblockEntry entry, CompoundTag cacheNbt, HolderLookup.Provider registries) {
        if (entry.executionStrategy() == MultiblockExecutionStrategy.SCHEDULED_SERVER_THREAD) {
            MultiblockLevelState.get(level).beginDiscovery(entry.scheduledDefinition(), controllerPos, facing);
            return null;
        }
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        MultiblockInstance instance = new MultiblockInstance(entry, facing);
        if (cacheNbt != null) {
            instance.cache.loadNbt(cacheNbt, registries);
        }
        if (cacheNbt != null && cacheNbt.contains("tracking_min")) {
            instance.trackingMin = BlockPos.of(cacheNbt.getLong("tracking_min"));
            instance.trackingMax = BlockPos.of(cacheNbt.getLong("tracking_max"));
            instance.trackedPositions = boxPositions(instance.trackingMin, instance.trackingMax);
        } else if (cacheNbt != null && cacheNbt.contains("tracked_positions")) {
            Set<Long> positions = new HashSet<>();
            for (long pos : cacheNbt.getLongArray("tracked_positions")) positions.add(pos);
            instance.trackedPositions = Set.copyOf(positions);
        } else if (instance.cache.hasAABB()) {
            instance.trackingMin = BlockPos.of(instance.cache.aabbMinPacked());
            instance.trackingMax = BlockPos.of(instance.cache.aabbMaxPacked());
            instance.trackedPositions = boxPositions(instance.trackingMin, instance.trackingMax);
        } else instance.trackedPositions = Set.copyOf(instance.cache.getStructurePositions());
        instance.dirty = true;
        indexStructure(dim, controllerPos.asLong(), instance.trackedPositions);
        MultiblockInstance old = map.put(controllerPos.asLong(), instance);
        if (old != null) {
            old.disposed = true;
            removeFromStructureIndex(dim, controllerPos.asLong(), old.trackedPositions);
            indexStructure(dim, controllerPos.asLong(), instance.trackedPositions);
        }
        return instance;
    }

    /** Destroy a multiblock. Called on controller-block removal. */
    public static void destroyMultiblock(ServerLevel level, BlockPos controllerPos) {
        ResourceKey<Level> dim = level.dimension();
        Map<Long, MultiblockInstance> map = INSTANCES.get(dim);
        MultiblockInstance instance = map == null ? null : map.remove(controllerPos.asLong());
        if (instance == null) {
            MultiblockLevelState.get(level).destroyAt(controllerPos);
            return;
        }
        instance.disposed = true;
        instance.generation++;
        removeFromStructureIndex(dim, controllerPos.asLong(), instance.trackedPositions);
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
        if (instance.waitingForChunk != null) {
            if (!level.hasChunkAt(instance.waitingForChunk)) return;
            instance.waitingForChunk = null;
            instance.dirty = true;
        }
        if (!instance.formed && !instance.dirty && instance.pendingChanges.isEmpty()
                && (!instance.trackedPositions.isEmpty() || TICK_COUNTER % 5 != 0)) return;
        MultiblockInstance finalInstance = instance;
        if (!finalInstance.busy.compareAndSet(false, true)) return;
        long generation = finalInstance.generation;
        Set<Long> changes = Set.copyOf(finalInstance.pendingChanges);
        finalInstance.pendingChanges.removeAll(changes);
        ExecutorService ex = MultiblockExecutorManager.getExecutor();
        ex.submit(() -> {
            try {
                finalInstance.runWork(level, controllerPos, generation, changes);
            } catch (Throwable t) {
                NuclearCraft.LOGGER.error("Multiblock tick error at {}", controllerPos, t);
            } finally {
                level.getServer().execute(() -> {
                    if (finalInstance.pendingPublication == null) finalInstance.busy.set(false);
                });
            }
        });
    }

    public static MultiblockInstance getInstance(ServerLevel level, BlockPos controllerPos) {
        Map<Long, MultiblockInstance> map = INSTANCES.get(level.dimension());
        return map == null ? null : map.get(controllerPos.asLong());
    }

    public static BlockPos getControllerForPos(ServerLevel level, BlockPos pos) {
        Set<UUID> scheduled = MultiblockLevelState.get(level).sectionIndex()
                .ownedCandidates(StructureSectionIndex.sectionKey(pos));
        for (UUID id : scheduled) {
            StructureRecord record = MultiblockLevelState.get(level).structure(id).orElse(null);
            if (record != null && record.state() == StructureLifecycleState.FORMED
                    && record.footprint().contains(pos)) return record.controllerPos();
        }
        Map<Long, Set<Long>> index = STRUCTURE_INDEX.get(level.dimension());
        Set<Long> controllers = index == null ? null : index.get(pos.asLong());
        if (controllers == null) return null;
        Map<Long, MultiblockInstance> instances = INSTANCES.get(level.dimension());
        for (long controller : controllers) {
            MultiblockInstance instance = instances == null ? null : instances.get(controller);
            if (instance != null && instance.formed && !instance.dirty) return BlockPos.of(controller);
        }
        return null;
    }

    private static Set<Long> boxPositions(BlockPos min, BlockPos max) {
        MultiblockCacheImpl geometry = new MultiblockCacheImpl();
        geometry.setAABB(min, max);
        return geometry.getStructurePositions();
    }

    static void indexStructure(ResourceKey<Level> dim, long controllerKey, Set<Long> positions) {
        Map<Long, Set<Long>> index = STRUCTURE_INDEX.computeIfAbsent(dim, ignored -> new ConcurrentHashMap<>());
        Map<Long, Integer> sections = TRACKED_SECTIONS.computeIfAbsent(dim, ignored -> new ConcurrentHashMap<>());
        Set<Long> singleController = Set.of(controllerKey);
        for (long pos : positions) {
            Set<Long> previous = index.get(pos);
            if (previous != null && previous.contains(controllerKey)) continue;
            if (previous == null) index.put(pos, singleController);
            else {
                Set<Long> owners = new HashSet<>(previous);
                owners.add(controllerKey);
                index.put(pos, Set.copyOf(owners));
            }
            sections.merge(StructureSectionIndex.sectionKey(BlockPos.of(pos)), 1, Integer::sum);
        }
    }

    static void removeFromStructureIndex(ResourceKey<Level> dim, long controllerKey, Set<Long> positions) {
        Map<Long, Set<Long>> index = STRUCTURE_INDEX.get(dim);
        if (index == null) return;
        Map<Long, Integer> sections = TRACKED_SECTIONS.get(dim);
        for (long pos : positions) {
            Set<Long> controllers = index.get(pos);
            if (controllers == null || !controllers.contains(controllerKey)) continue;
            if (controllers.size() == 1) index.remove(pos);
            else {
                Set<Long> remaining = new HashSet<>(controllers);
                remaining.remove(controllerKey);
                index.put(pos, Set.copyOf(remaining));
            }
            if (sections != null) sections.computeIfPresent(StructureSectionIndex.sectionKey(BlockPos.of(pos)),
                    (key, count) -> count > 1 ? count - 1 : null);
        }
    }

    static void sendFormed(ServerLevel level, BlockPos controllerPos, IMultiblockCache cache) {
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
        public final IMultiblockValidator validator;
        public final IMultiblockLogic logic;
        public final IMultiblockCache cache;
        public final Direction facing;
        public volatile boolean formed;
        volatile boolean disposed;
        public volatile boolean dirty;
        volatile long generation;
        private Set<Long> trackedPositions = Set.of();
        private BlockPos trackingMin;
        private BlockPos trackingMax;
        private final StructureObservations observations = new StructureObservations();
        private java.util.Iterator<Long> observationCursor = java.util.Collections.emptyIterator();
        private volatile Runnable pendingPublication;
        private BlockPos waitingForChunk;
        private final Set<Long> candidates = new HashSet<>();
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

        /** Worker validation keeps immutable change batches; publication is guarded on the server thread. */
        void runWork(ServerLevel level, BlockPos controllerPos, long generation, Set<Long> changed) {
            if (disposed) return;
            if (!changed.isEmpty() || dirty || (!formed && trackedPositions.isEmpty() && TICK_COUNTER % 5 == 0)) {
                tryValidate(level, controllerPos, generation, changed);
                return;
            }
            if (formed) logic.tickServer(level, controllerPos, cache);
        }

        void tryValidate(ServerLevel level, BlockPos controllerPos, long startedGeneration, Set<Long> changed) {
            boolean wasFormed = formed;
            boolean hadAABB = trackingMin != null;
            long previousMin = hadAABB ? trackingMin.asLong() : 0;
            long previousMax = hadAABB ? trackingMax.asLong() : 0;
            MultiblockDebug.log("{} legacy validation START controller={} reason={} changes={}", entry.name(), controllerPos,
                    changed.isEmpty() ? "initial/load recovery" : "confirmed structural change", changed);
            for (long key : changed) cache.invalidate(BlockPos.of(key));
            cache.clear();
            boolean valid;
            try {
                valid = runValidation(level, controllerPos);
            } catch (UnloadedStructureException unloaded) {
                pendingPublication = () -> {
                    if (disposed || generation != startedGeneration) return;
                    waitingForChunk = unloaded.position();
                    dirty = true;
                    if (hadAABB) cache.setAABB(BlockPos.of(previousMin), BlockPos.of(previousMax));
                    else cache.getStructurePositions().addAll(trackedPositions);
                };
                return;
            }
            BlockPos validatedMin = valid && cache.hasAABB() ? BlockPos.of(cache.aabbMinPacked()) : null;
            BlockPos validatedMax = valid && cache.hasAABB() ? BlockPos.of(cache.aabbMaxPacked()) : null;
            Set<Long> validatedPositions = !valid ? trackedPositions : validatedMin != null
                    ? boxPositions(validatedMin, validatedMax) : Set.copyOf(cache.getStructurePositions());
            pendingPublication = () -> {
                if (disposed || generation != startedGeneration) {
                    dirty = !disposed;
                    return;
                }
                if (cache instanceof MultiblockCacheImpl cached) {
                    for (long key : validatedPositions) {
                        BlockPos pos = BlockPos.of(key);
                        BlockState actual = cached.observedBlockState(pos);
                        if (actual != null) observations.observe(pos, actual);
                    }
                }
                dirty = false;
                formed = valid;
                if (valid) {
                    boolean sameGeometry = trackingMin != null && trackingMin.equals(validatedMin)
                            && trackingMax.equals(validatedMax);
                    if (!sameGeometry && !trackedPositions.equals(validatedPositions)) {
                        removeFromStructureIndex(level.dimension(), controllerPos.asLong(), trackedPositions);
                        trackedPositions = validatedPositions;
                        observationCursor = trackedPositions.iterator();
                        indexStructure(level.dimension(), controllerPos.asLong(), trackedPositions);
                    }
                    trackingMin = validatedMin;
                    trackingMax = validatedMax;
                    if (!wasFormed) {
                        logic.onFormed(level, controllerPos, cache);
                        sendFormed(level, controllerPos, cache);
                    }
                } else {
                    // Validators may clear their working geometry on failure. Keep repair membership separately.
                    if (hadAABB) cache.setAABB(BlockPos.of(previousMin), BlockPos.of(previousMax));
                    else cache.getStructurePositions().addAll(trackedPositions);
                    if (wasFormed) {
                        logic.onBroken(level, controllerPos, cache);
                        sendBroken(level, controllerPos);
                    }
                }
            };
        }

        private int observeIntegrity(ServerLevel level, int budget, long deadline, Map<MultiblockInstance, Set<Long>> changed) {
            if (disposed || trackedPositions.isEmpty()) return 0;
            int reads = 0;
            for (int i = 0; i < budget && System.nanoTime() < deadline; i++) {
                if (!observationCursor.hasNext()) {
                    observationCursor = trackedPositions.iterator();
                    if (i > 0) break;
                }
                long key = observationCursor.next();
                BlockPos pos = BlockPos.of(key);
                if (!level.hasChunkAt(pos)) continue;
                reads++;
                if (observations.observe(pos, level.getBlockState(pos))) {
                    changed.computeIfAbsent(this, ignored -> new HashSet<>()).add(key);
                }
            }
            return reads;
        }

        private boolean runValidation(ServerLevel level, BlockPos controllerPos) {
            var machineId = NuclearCraft.rl(entry.name());
            MultiblockDebug.beginLegacy(machineId, controllerPos);
            boolean valid = false;
            try {
                valid = validator.validate(level, controllerPos, facing, cache);
                return valid;
            } finally {
                MultiblockDebug.finishLegacy(level, machineId, controllerPos, cache, valid);
            }
        }

        public void saveTracking(CompoundTag tag) {
            if (trackingMin != null) {
                tag.putLong("tracking_min", trackingMin.asLong());
                tag.putLong("tracking_max", trackingMax.asLong());
            } else tag.putLongArray("tracked_positions", trackedPositions.stream().mapToLong(Long::longValue).toArray());
        }

        public BlockPos getCenter(MultiblockCacheImpl multiblockCache) {
            if (multiblockCache.hasAABB()) {
                BlockPos min = BlockPos.of(multiblockCache.aabbMinPacked());
                BlockPos max = BlockPos.of(multiblockCache.aabbMaxPacked());
                return new BlockPos((min.getX() + max.getX()) / 2,
                        (min.getY() + max.getY()) / 2,
                        (min.getZ() + max.getZ()) / 2);
            }
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
