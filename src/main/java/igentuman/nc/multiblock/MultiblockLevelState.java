package igentuman.nc.multiblock;

import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.multiblock.discovery.GeometryDiscoveryResult;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.validation.ServerLevelStructureReader;
import igentuman.nc.multiblock.validation.ValidationBudget;
import igentuman.nc.multiblock.validation.ValidationJobFactory;
import igentuman.nc.multiblock.validation.ValidationResult;
import igentuman.nc.multiblock.validation.ValidationStatus;
import igentuman.nc.multiblock.validation.ValidationJob;
import igentuman.nc.multiblock.validation.LoadedStructureReader;
import igentuman.nc.multiblock.validation.StructureObservationJob;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import igentuman.nc.network.PacketMultiblockBroken;
import igentuman.nc.network.PacketMultiblockDebug;
import igentuman.nc.network.PacketScheduledMultiblockFormed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class MultiblockLevelState extends SavedData {

    private static final String DATA_NAME = "nuclearcraft_scheduled_multiblocks";
    private static final ValidationBudget DEFAULT_BUDGET = new ValidationBudget(2_000_000L, 4_096, 128, 16);

    private final Map<UUID, StructureRecord> structures = new LinkedHashMap<>();
    private final Map<Long, UUID> controllerIndex = new HashMap<>();
    private final StructureSectionIndex sectionIndex = new StructureSectionIndex();
    private final MultiblockWorkScheduler scheduler = new MultiblockWorkScheduler(DEFAULT_BUDGET);
    private final Map<ResourceLocation, ValidationJobFactory> validationFactories = new HashMap<>();
    private final Map<UUID, PendingDiscovery> pendingDiscoveries = new HashMap<>();
    private final Map<UUID, Long> discoveryStartedNanos = new HashMap<>();
    private final Map<UUID, GeometryDiscoveryResult.Status> discoveryStatuses = new HashMap<>();
    private final Map<UUID, Long> validationStartedNanos = new HashMap<>();
    private final Map<UUID, ValidationStatus> validationStatuses = new HashMap<>();
    private final Map<UUID, StructureObservations> observations = new HashMap<>();
    private final Map<UUID, StructureObservationJob> audits = new HashMap<>();
    private final Map<UUID, Long> generations = new HashMap<>();
    private final Set<UUID> baselined = new java.util.HashSet<>();
    private final Set<UUID> disposed = new java.util.HashSet<>();
    private final LongSet pendingObservations = new LongOpenHashSet();
    private final Set<UUID> awaitingObservation = new java.util.HashSet<>();
    private long nextGeneration;
    private long topologyRevision;
    private long configRevision;
    private long tickCounter;
    private int auditCursor;

    public static MultiblockLevelState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MultiblockLevelState::new, MultiblockLevelState::load), DATA_NAME);
    }

    public Map<UUID, StructureRecord> structures() {
        return Map.copyOf(structures);
    }

    public Optional<StructureRecord> structure(UUID id) {
        return Optional.ofNullable(structures.get(id));
    }

    public StructureSectionIndex sectionIndex() {
        return sectionIndex;
    }

    public MultiblockWorkScheduler scheduler() {
        return scheduler;
    }

    public long topologyRevision() {
        return topologyRevision;
    }

    public long configRevision() {
        return configRevision;
    }

    public long sectionRevision(long section) {
        return 0; // Kept for readers of older records; freshness is per structure.
    }

    public Optional<UUID> register(StructureRecord record) {
        Optional<UUID> overlap = sectionIndex.overlappingOwner(record, structures);
        if (overlap.isPresent()) return overlap;
        StructureRecord old = structures.put(record.id(), record);
        observations.computeIfAbsent(record.id(), ignored -> new StructureObservations());
        generations.computeIfAbsent(record.id(), ignored -> ++nextGeneration);
        if (old != null) {
            sectionIndex.unindex(old);
            controllerIndex.remove(old.controllerPos().asLong(), old.id());
        }
        sectionIndex.index(record);
        controllerIndex.put(record.controllerPos().asLong(), record.id());
        topologyRevision = Math.max(topologyRevision, record.topologyRevision());
        configRevision = Math.max(configRevision, record.configRevision());
        setDirty();
        return Optional.empty();
    }

    public boolean remove(UUID id) {
        StructureRecord removed = structures.remove(id);
        if (removed == null) return false;
        sectionIndex.unindex(removed);
        scheduler.cancelApplications(id);
        observations.remove(id);
        audits.remove(id);
        generations.remove(id);
        baselined.remove(id);
        disposed.remove(id);
        controllerIndex.remove(removed.controllerPos().asLong(), removed.id());
        scheduler.cancelDiscovery(id);
        scheduler.cancelValidation(id);
        pendingDiscoveries.remove(id);
        discoveryStartedNanos.remove(id);
        discoveryStatuses.remove(id);
        validationStartedNanos.remove(id);
        validationStatuses.remove(id);
        setDirty();
        return true;
    }

    public void validationObserved(UUID id, BlockPos pos, BlockState actual) {
        StructureObservations baseline = observations.get(id);
        if (baseline != null) baseline.observe(pos, actual);
    }

    public boolean tracks(BlockPos pos) {
        return sectionIndex.contains(pos, structures);
    }

    public void observeChange(BlockPos pos, BlockState previous) {
        if (!tracks(pos)) return;
        if (previous != null) {
            for (UUID id : sectionIndex.affected(pos, structures)) observations.get(id).seed(pos, previous);
        }
        pendingObservations.add(pos.asLong());
        awaitingObservation.addAll(sectionIndex.affected(pos, structures));
    }

    public Set<UUID> markChanged(BlockPos pos) {
        return markChanged(java.util.List.of(pos));
    }

    public Set<UUID> markChanged(Iterable<BlockPos> positions) {
        Map<UUID, BlockPos> affected = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            for (UUID id : sectionIndex.affected(pos, structures)) affected.putIfAbsent(id, pos);
        }
        affected.forEach((id, pos) -> {
            StructureRecord record = structures.get(id);
            if (record == null || disposed.contains(id)) return;
            scheduler.cancelValidation(id);
            scheduler.cancelApplications(id);
            audits.remove(id);
            generations.put(id, ++nextGeneration);
            structures.put(id, record.withRevisions(record.topologyRevision() + 1, configRevision, Map.of())
                    .withState(StructureLifecycleState.DIRTY,
                            new StructureRecord.StructureDiagnostic("multiblock.validation.structure_changed", pos, null, null)));
            MultiblockDebug.log("{} structural change controller={} position={}", record.machineId(), record.controllerPos(), pos);
        });
        if (!affected.isEmpty()) {
            topologyRevision++;
            setDirty();
        }
        return affected.keySet();
    }

    int confirmChanges(LoadedStructureReader reader, ValidationBudget budget) {
        if (pendingObservations.isEmpty()) return 0;
        long started = System.nanoTime();
        int reads = 0;
        java.util.List<BlockPos> changed = new java.util.ArrayList<>();
        LongSet moving = new LongOpenHashSet();
        var iterator = pendingObservations.iterator();
        while (iterator.hasNext() && reads < budget.maximumBlockReads()
                && System.nanoTime() - started < budget.maximumNanos()) {
            long key = iterator.nextLong();
            iterator.remove();
            BlockPos pos = BlockPos.of(key);
            if (!tracks(pos) || !reader.isLoaded(net.minecraft.core.SectionPos.of(pos))) continue;
            BlockState current = reader.blockState(pos);
            reads++;
            boolean differs = false;
            for (UUID id : sectionIndex.affected(pos, structures)) {
                differs |= observations.get(id).observe(pos, current);
            }
            if (differs) changed.add(pos);
            if (current.is(Blocks.MOVING_PISTON)) moving.add(key);
        }
        markChanged(changed);
        pendingObservations.addAll(moving);
        awaitingObservation.clear();
        pendingObservations.forEach((long key) -> awaitingObservation.addAll(sectionIndex.affected(BlockPos.of(key), structures)));
        return reads;
    }

    public void registerValidationFactory(ResourceLocation machineId, ValidationJobFactory factory) {
        if (machineId == null || factory == null) throw new IllegalArgumentException("machineId and factory are required");
        validationFactories.put(machineId, factory);
    }

    public UUID beginDiscovery(ScheduledMultiblockDefinition definition, BlockPos controllerPos,
                               net.minecraft.core.Direction facing) {
        if (definition == null || controllerPos == null || facing == null) {
            throw new IllegalArgumentException("Discovery definition, position, and facing are required");
        }
        registerValidationFactory(definition.machineId(), definition.validationFactory());
        Optional<StructureRecord> existing = structureAt(controllerPos);
        if (existing.isPresent()) return existing.get().id();
        UUID id = structureId(definition.machineId(), controllerPos);
        if (!pendingDiscoveries.containsKey(id) && !scheduler.isPending(id)) {
            pendingDiscoveries.put(id, new PendingDiscovery(definition, controllerPos.immutable(), facing));
            if (MultiblockDebug.enabled()) {
                discoveryStartedNanos.put(id, System.nanoTime());
                MultiblockDebug.log("{} discovery START controller={} facing={}", definition.machineId(),
                        controllerPos.toShortString(), facing);
            }
            scheduler.submit(definition.discoveryFactory().create(id, controllerPos, facing));
        }
        return id;
    }

    public Optional<StructureRecord> structureAt(BlockPos controllerPos) {
        UUID id = controllerIndex.get(controllerPos.asLong());
        return Optional.ofNullable(id == null ? null : structures.get(id));
    }

    public boolean isFormed(BlockPos controllerPos) {
        return structureAt(controllerPos)
                .map(record -> record.state() == StructureLifecycleState.FORMED)
                .orElse(false);
    }

    public void destroyAt(BlockPos controllerPos) {
        Optional<StructureRecord> existing = structureAt(controllerPos);
        if (existing.isPresent()) {
            StructureRecord record = existing.get();
            scheduler.cancelDiscovery(record.id());
            scheduler.cancelValidation(record.id());
            disposed.add(record.id());
            sectionIndex.unindex(record);
            observations.remove(record.id());
            baselined.remove(record.id());
            generations.put(record.id(), ++nextGeneration);
            scheduler.cancelApplications(record.id());
            audits.remove(record.id());
            StructureRecord breaking = record.withState(StructureLifecycleState.DISPOSED, record.diagnostic());
            structures.put(record.id(), breaking);
            scheduler.submit(new StructureApplicationJob(record.id(), StructureApplicationJob.Operation.BREAK,
                    applicationPositions(record), 0, generations.get(record.id())));
            setDirty();
            return;
        }
        pendingDiscoveries.entrySet().removeIf(entry -> {
            if (!entry.getValue().controllerPos().equals(controllerPos)) return false;
            scheduler.cancelDiscovery(entry.getKey());
            return true;
        });
    }

    public int enqueueDirtyRevalidations() {
        int enqueued = 0;
        for (StructureRecord record : List.copyOf(structures.values())) {
            if (record.state() != StructureLifecycleState.DIRTY) continue;
            if (scheduler.isPending(record.id())) continue;
            ValidationJobFactory factory = validationFactories.get(record.machineId());
            if (factory == null) continue;
            submitValidation(record, factory, "structure changed");
            structures.put(record.id(), record.withState(StructureLifecycleState.VALIDATING, record.diagnostic()));
            enqueued++;
        }
        if (enqueued > 0) setDirty();
        return enqueued;
    }

    public boolean enqueueNextAudit() {
        List<StructureRecord> candidates = structures.values().stream()
                .filter(record -> record.state() == StructureLifecycleState.FORMED
                        || record.state() == StructureLifecycleState.UNFORMED).toList();
        if (candidates.isEmpty()) return false;
        for (int i = 0; i < candidates.size(); i++) {
            StructureRecord candidate = candidates.get(Math.floorMod(auditCursor++, candidates.size()));
            if (scheduler.isPending(candidate.id()) || disposed.contains(candidate.id())) continue;
            StructureObservationJob job = new StructureObservationJob(candidate.id(), candidate.footprint(),
                    observations.get(candidate.id()));
            audits.put(candidate.id(), job);
            scheduler.submit(job);
            return true;
        }
        return false;
    }

    public void setConfigRevision(long revision) {
        if (revision < 0) throw new IllegalArgumentException("config revision must be nonnegative");
        if (revision == configRevision) return;
        configRevision = revision;
        structures.replaceAll((id, record) -> {
            if (disposed.contains(id)) return record;
            scheduler.cancelValidation(id);
            scheduler.cancelApplications(id);
            audits.remove(id);
            generations.put(id, ++nextGeneration);
            return record.withRevisions(record.topologyRevision(), revision, Map.of())
                    .withState(StructureLifecycleState.DIRTY,
                            new StructureRecord.StructureDiagnostic("multiblock.validation.config_changed",
                                    record.controllerPos(), null, null));
        });
        setDirty();
    }

    public void tick(ServerLevel level) {
        tick(level, 0, 0);
    }

    void tick(ServerLevel level, int priorReads, long priorNanos) {
        tickCounter++;
        ParticleMachinesConfig config = Multiblocks.particleMachines();
        if (config.revision() != configRevision) setConfigRevision(config.revision());
        ParticleMachinesConfig.Scheduler schedulerConfig = config.scheduler();
        long availableNanos = schedulerConfig.timeBudgetNanos() - priorNanos;
        int availableReads = schedulerConfig.blockReadBudget() - priorReads;
        if (availableNanos <= 0 || availableReads <= 0) return;
        scheduler.updateBudget(new ValidationBudget(availableNanos,
                availableReads, schedulerConfig.blockEntityReadBudget(),
                schedulerConfig.mutationBudget()));
        long observationStarted = System.nanoTime();
        int observationAllowance = availableReads == 1 && tickCounter % 2 != 0 ? 0 : Math.max(1, availableReads / 2);
        int observationReads = observationAllowance == 0 ? 0 : confirmChanges(new ServerLevelStructureReader(level, this),
                new ValidationBudget(Math.max(1, availableNanos / 2), observationAllowance, 0, 0));
        long remainingNanos = availableNanos - (System.nanoTime() - observationStarted);
        int remainingReads = availableReads - observationReads;
        if (remainingNanos <= 0 || remainingReads <= 0) return;
        scheduler.updateBudget(new ValidationBudget(remainingNanos, remainingReads,
                schedulerConfig.blockEntityReadBudget(), schedulerConfig.mutationBudget()));
        enqueueDirtyRevalidations();
        if (tickCounter % schedulerConfig.auditIntervalTicks() == 0) enqueueNextAudit();
        ServerLevelStructureReader reader = new ServerLevelStructureReader(level, this);
        scheduler.tickWorldWork(reader, (id, result) -> acceptDiscoveryResult(level, id, result),
                (id, result) -> acceptValidationResult(level, id, result));
        scheduler.tickApplications(new MultiblockWorkScheduler.ApplicationHandler() {
            @Override
            public boolean isCurrent(StructureApplicationJob job) {
                return generations.getOrDefault(job.structureId(), -1L) == job.generation();
            }

            @Override
            public boolean isReady(StructureApplicationJob job) {
                return job.operation() == StructureApplicationJob.Operation.BREAK || !awaitingObservation.contains(job.structureId());
            }

            @Override
            public boolean apply(UUID structureId, StructureApplicationJob.Operation operation, BlockPos portPosition) {
                if (!level.hasChunkAt(portPosition)) return false;
                StructureRecord record = structures.get(structureId);
                if (record == null) return true;
                if (level.getBlockEntity(portPosition) instanceof MultiblockPortBE port) {
                    if (operation == StructureApplicationJob.Operation.FORM) {
                        port.setControllerPos(record.controllerPos());
                        port.configureFromStructure(record);
                    } else if (record.controllerPos().equals(port.getControllerPos())) {
                        port.clearStructureConfiguration();
                        port.setControllerPos(null);
                    }
                }
                level.invalidateCapabilities(portPosition);
                return true;
            }

            @Override
            public void completed(UUID structureId, StructureApplicationJob.Operation operation) {
                StructureRecord record = structures.get(structureId);
                if (record == null) return;
                if (operation == StructureApplicationJob.Operation.FORM) {
                    structures.put(structureId, record.withState(StructureLifecycleState.FORMED, record.diagnostic()));
                    setDirty();
                    PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(record.controllerPos()),
                            new PacketScheduledMultiblockFormed(record.controllerPos(),
                                    MultiblockPersistence.saveClientGeometry(record.footprint())));
                } else {
                    PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(record.controllerPos()),
                            new PacketMultiblockBroken(record.controllerPos()));
                    if (disposed.contains(structureId)) remove(structureId);
                    else {
                        structures.put(structureId, record.withState(StructureLifecycleState.UNFORMED, record.diagnostic()));
                        setDirty();
                    }
                }
            }
        });
    }

    private void acceptDiscoveryResult(ServerLevel level, UUID structureId, GeometryDiscoveryResult result) {
        PendingDiscovery pending = pendingDiscoveries.get(structureId);
        if (pending == null) return;
        long elapsed = 0;
        if (MultiblockDebug.enabled()) {
            elapsed = elapsed(discoveryStartedNanos, structureId);
            GeometryDiscoveryResult.Status previous = discoveryStatuses.put(structureId, result.status());
            if (previous != result.status() || result.terminal()) {
                MultiblockDebug.log("{} discovery {} controller={} diagnostic={} reads={} elapsed={}ms failure={}",
                        pending.definition().machineId(), result.status(), pending.controllerPos().toShortString(),
                        result.diagnosticKey(), result.completedReads(), millis(elapsed), result.failingPosition());
            }
        }
        if (!result.terminal()) return;
        pendingDiscoveries.remove(structureId);
        discoveryStartedNanos.remove(structureId);
        discoveryStatuses.remove(structureId);
        if (result.status() != GeometryDiscoveryResult.Status.VALID || result.footprint() == null) {
            if (MultiblockDebug.enabled()) {
                MultiblockDebug.send(level, new PacketMultiblockDebug(pending.controllerPos(),
                        pending.definition().machineId(), "discovery", result.status().name(), result.diagnosticKey(),
                        null, null, pending.controllerPos(), result.failingPosition(), result.failingPosition(),
                        null, null, result.completedReads(), 0, elapsed));
            }
            return;
        }

        StructureRecord discovered = new StructureRecord(MultiblockPersistence.SCHEMA_VERSION, structureId,
                pending.definition().machineId(), level.dimension(), pending.controllerPos(), pending.facing(),
                result.footprint(), result.footprint().ownedSections(), result.footprint().watchedSections(),
                0, configRevision, Map.of(), roles(result.footprint()), emptyAggregates(),
                StructureLifecycleState.VALIDATING, null);
        if (MultiblockDebug.enabled()) {
            MultiblockDebug.Bounds bounds = MultiblockDebug.bounds(result.footprint());
            MultiblockDebug.send(level, new PacketMultiblockDebug(pending.controllerPos(),
                    pending.definition().machineId(), "discovery", result.status().name(), result.diagnosticKey(),
                    bounds.minimum(), bounds.maximum(), bounds.start(), bounds.end(), null, null, null,
                    result.completedReads(), result.completedReads(), elapsed));
        }
        if (register(discovered).isPresent()) return;
        submitValidation(discovered, pending.definition().validationFactory(), "initial formation");
    }

    private static Map<StructureRole, List<BlockPos>> roles(igentuman.nc.api.multiblock.StructureFootprint footprint) {
        Map<StructureRole, List<BlockPos>> roles = new java.util.EnumMap<>(StructureRole.class);
        for (StructureRole role : StructureRole.values()) {
            List<BlockPos> positions = footprint.positions(role);
            if (!positions.isEmpty()) roles.put(role, positions);
        }
        return roles;
    }

    private static StructureRecord.StructureAggregates emptyAggregates() {
        return new StructureRecord.StructureAggregates(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Map.of(), 0);
    }

    private static UUID structureId(ResourceLocation machineId, BlockPos controllerPos) {
        String key = machineId + "@" + controllerPos.getX() + "," + controllerPos.getY() + "," + controllerPos.getZ();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    void acceptValidationResult(UUID structureId, ValidationResult result) {
        acceptValidationResult(null, structureId, result);
    }

    private void acceptValidationResult(ServerLevel level, UUID structureId, ValidationResult result) {
        StructureObservationJob audit = audits.get(structureId);
        if (audit != null) {
            if (result.terminal()) {
                audits.remove(structureId);
                java.util.List<BlockPos> changed = new java.util.ArrayList<>();
                audit.changes().forEach((long key) -> changed.add(BlockPos.of(key)));
                markChanged(changed);
            }
            return;
        }
        StructureRecord current = structures.get(structureId);
        if (current == null || disposed.contains(structureId)) return;
        if (result.record() != null && (result.record().topologyRevision() != current.topologyRevision()
                || result.record().configRevision() != configRevision)) return;

        if (MultiblockDebug.enabled()) {
            StructureRecord debugRecord = result.record() != null ? result.record() : structures.get(structureId);
            long elapsed = elapsed(validationStartedNanos, structureId);
            ValidationStatus previousStatus = validationStatuses.put(structureId, result.status());
            if (debugRecord != null && (previousStatus != result.status() || result.terminal())) {
                MultiblockDebug.log("{} validation {} controller={} diagnostic={} progress={}/{} elapsed={}ms failure={} expected={} actual={}",
                        debugRecord.machineId(), result.status(), debugRecord.controllerPos().toShortString(),
                        result.diagnosticKey(), result.completedCells(), result.totalCells(), millis(elapsed),
                        result.failingPosition(), result.expected(), result.actual());
            }
            if (level != null && debugRecord != null && (previousStatus != result.status() || result.terminal())) {
                MultiblockDebug.Bounds bounds = MultiblockDebug.bounds(debugRecord.footprint());
                MultiblockDebug.send(level, new PacketMultiblockDebug(debugRecord.controllerPos(), debugRecord.machineId(),
                        "validation", result.status().name(), result.diagnosticKey(), bounds.minimum(), bounds.maximum(),
                        bounds.start(), bounds.end(), result.failingPosition(), result.expected(), result.actual(),
                        result.completedCells(), result.totalCells(), elapsed));
            }
        }
        if (result.terminal()) {
            validationStartedNanos.remove(structureId);
            validationStatuses.remove(structureId);
        }
        if (result.status() == ValidationStatus.VALID && result.record() != null) {
            StructureRecord existing = structures.get(structureId);
            if (existing != null && existing.state() == StructureLifecycleState.FORMED) {
                StructureRecord refreshed = result.record().withState(StructureLifecycleState.FORMED, null);
                structures.put(structureId, refreshed);
                sectionIndex.unindex(existing);
                sectionIndex.index(refreshed);
                setDirty();
                return;
            }
            StructureRecord applying = result.record().withState(StructureLifecycleState.APPLYING,
                    result.record().diagnostic());
            if (register(applying).isEmpty()) {
                scheduler.submit(new StructureApplicationJob(structureId, StructureApplicationJob.Operation.FORM,
                        applicationPositions(applying), 0, generations.get(structureId)));
            } else {
                structures.put(structureId, current.withState(StructureLifecycleState.BREAKING,
                        new StructureRecord.StructureDiagnostic("multiblock.validation.overlapping_structure",
                                current.controllerPos(), null, null)));
                scheduler.submit(new StructureApplicationJob(structureId, StructureApplicationJob.Operation.BREAK,
                        applicationPositions(current), 0, generations.get(structureId)));
                setDirty();
            }
            return;
        }
        StructureRecord record = structures.get(structureId);
        if (record == null) return;
        if (result.status() == ValidationStatus.STALE) {
            structures.put(structureId, record.withState(StructureLifecycleState.DIRTY, diagnostic(result)));
            setDirty();
            return;
        }
        if (result.status() == ValidationStatus.INVALID
                && record.state() != StructureLifecycleState.UNFORMED) {
            structures.put(structureId, record.withState(StructureLifecycleState.BREAKING,
                    diagnostic(result)));
            scheduler.submit(new StructureApplicationJob(structureId, StructureApplicationJob.Operation.BREAK,
                    applicationPositions(record), 0, generations.get(record.id())));
            setDirty();
            return;
        }
        StructureLifecycleState state = switch (result.status()) {
            case IN_PROGRESS -> StructureLifecycleState.VALIDATING;
            case WAITING_FOR_CHUNK -> StructureLifecycleState.WAITING_FOR_CHUNKS;
            case VALID -> StructureLifecycleState.APPLYING;
            case INVALID, STALE -> StructureLifecycleState.DIRTY;
            case CANCELLED -> StructureLifecycleState.SUSPENDED;
        };
        structures.put(structureId, record.withState(state, diagnostic(result)));
        setDirty();
    }

    private void submitValidation(StructureRecord record, ValidationJobFactory factory, String reason) {
        if (MultiblockDebug.enabled()) {
            validationStartedNanos.put(record.id(), System.nanoTime());
            validationStatuses.remove(record.id());
            MultiblockDebug.log("{} validation START controller={} reason={} cells={}", record.machineId(),
                    record.controllerPos().toShortString(), reason, record.footprint().cellCount());
        }
        ValidationJob delegate = factory.create(record.id(), record);
        long generation = generations.get(record.id());
        StructureObservationJob baseline = baselined.contains(record.id()) ? null
                : new StructureObservationJob(record.id(), record.footprint(), observations.get(record.id()));
        scheduler.submit(new ValidationJob() {
            private boolean ready = baseline == null;
            @Override
            public UUID structureId() { return record.id(); }
            @Override
            public ValidationResult advance(LoadedStructureReader reader, ValidationBudget budget) {
                if (generations.getOrDefault(record.id(), -1L) != generation) {
                    return new ValidationResult(ValidationStatus.STALE, "multiblock.validation.structure_changed",
                            null, null, null, 0, record.footprint().cellCount(), null, null);
                }
                if (awaitingObservation.contains(record.id())) {
                    return new ValidationResult(ValidationStatus.IN_PROGRESS, "multiblock.validation.pending_observation",
                            null, null, null, 0, record.footprint().cellCount(), null, null);
                }
                if (!ready) {
                    ValidationResult progress = baseline.advance(reader, budget);
                    if (!progress.terminal()) return progress;
                    ready = true;
                    baselined.add(record.id());
                    return new ValidationResult(ValidationStatus.IN_PROGRESS, "multiblock.validation.baseline_ready",
                            null, null, null, 0, record.footprint().cellCount(), null, null);
                }
                return delegate.advance(reader, budget);
            }
        });
    }

    private static long elapsed(Map<UUID, Long> starts, UUID id) {
        Long started = starts.get(id);
        return started == null ? 0 : Math.max(0, System.nanoTime() - started);
    }

    private static String millis(long nanos) {
        return String.format(java.util.Locale.ROOT, "%.3f", nanos / 1_000_000D);
    }

    private static StructureRecord.StructureDiagnostic diagnostic(ValidationResult result) {
        return new StructureRecord.StructureDiagnostic(result.diagnosticKey(), result.failingPosition(),
                result.expected(), result.actual());
    }

    private static List<BlockPos> applicationPositions(StructureRecord record) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        positions.addAll(record.roles().getOrDefault(StructureRole.SERVICE_PORT, List.of()));
        positions.addAll(record.roles().getOrDefault(StructureRole.BEAM_INPUT, List.of()));
        positions.addAll(record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of()));
        positions.addAll(record.roles().getOrDefault(StructureRole.ION_SOURCE, List.of()));
        return List.copyOf(positions);
    }

    public static MultiblockLevelState load(CompoundTag tag, HolderLookup.Provider registries) {
        MultiblockLevelState state = new MultiblockLevelState();
        state.topologyRevision = Math.max(0, tag.getLong("topology_revision"));
        state.configRevision = Math.max(0, tag.getLong("config_revision"));
        ListTag structures = tag.getList("structures", Tag.TAG_COMPOUND);
        for (int i = 0; i < structures.size(); i++) {
            MultiblockPersistence.loadRecord(structures.getCompound(i)).ifPresent(record -> {
                if (record.state() == StructureLifecycleState.DISPOSED) return;
                record = record.withState(StructureLifecycleState.DIRTY,
                        new StructureRecord.StructureDiagnostic("multiblock.validation.load_recovery",
                                record.controllerPos(), null, null));
                state.observations.put(record.id(), new StructureObservations());
                state.generations.put(record.id(), ++state.nextGeneration);
                state.structures.put(record.id(), record);
                state.sectionIndex.index(record);
                state.controllerIndex.put(record.controllerPos().asLong(), record.id());
            });
        }
        return state;
    }

    private record PendingDiscovery(ScheduledMultiblockDefinition definition, BlockPos controllerPos,
                                    net.minecraft.core.Direction facing) {
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        tag.putLong("topology_revision", topologyRevision);
        tag.putLong("config_revision", configRevision);
        ListTag records = new ListTag();
        structures.values().forEach(record -> records.add(MultiblockPersistence.saveRecord(record)));
        tag.put("structures", records);
        return tag;
    }
}
