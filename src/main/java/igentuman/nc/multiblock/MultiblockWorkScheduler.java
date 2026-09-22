package igentuman.nc.multiblock;

import igentuman.nc.multiblock.discovery.GeometryDiscoveryJob;
import igentuman.nc.multiblock.discovery.GeometryDiscoveryResult;
import igentuman.nc.multiblock.validation.ValidationBudget;
import igentuman.nc.multiblock.validation.ValidationJob;
import igentuman.nc.multiblock.validation.LoadedStructureReader;
import igentuman.nc.multiblock.validation.ValidationResult;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class MultiblockWorkScheduler {

    private final Deque<GeometryDiscoveryJob> discoveryJobs = new ArrayDeque<>();
    private final Deque<ValidationJob> validationJobs = new ArrayDeque<>();
    private final Deque<StructureApplicationJob> applicationJobs = new ArrayDeque<>();
    private final Set<UUID> validationIds = new HashSet<>();
    private final Set<UUID> discoveryIds = new HashSet<>();
    private ValidationBudget budget;
    private boolean discoveryTurn = true;

    public MultiblockWorkScheduler(ValidationBudget budget) {
        if (budget == null) throw new IllegalArgumentException("budget is required");
        this.budget = budget;
    }

    public void submit(ValidationJob job) {
        if (job == null) throw new IllegalArgumentException("job is required");
        if (validationIds.add(job.structureId())) validationJobs.addLast(job);
    }

    public void submit(GeometryDiscoveryJob job) {
        if (job == null) throw new IllegalArgumentException("job is required");
        if (discoveryIds.add(job.structureId())) discoveryJobs.addLast(job);
    }

    public void submit(StructureApplicationJob job) {
        if (job == null) throw new IllegalArgumentException("job is required");
        applicationJobs.addLast(job);
    }

    public boolean cancelValidation(UUID structureId) {
        validationIds.remove(structureId);
        return validationJobs.removeIf(job -> job.structureId().equals(structureId));
    }

    public void cancelApplications(UUID structureId) {
        applicationJobs.removeIf(job -> job.structureId().equals(structureId));
    }

    public boolean cancelDiscovery(UUID structureId) {
        discoveryIds.remove(structureId);
        return discoveryJobs.removeIf(job -> job.structureId().equals(structureId));
    }

    public boolean isPending(UUID structureId) {
        return discoveryIds.contains(structureId) || validationIds.contains(structureId);
    }

    public int discoveryJobCount() {
        return discoveryJobs.size();
    }

    public int validationJobCount() {
        return validationJobs.size() - observationJobCount();
    }

    public int observationJobCount() {
        return (int) validationJobs.stream().filter(job -> job instanceof igentuman.nc.multiblock.validation.StructureObservationJob).count();
    }

    public int applicationJobCount() {
        return applicationJobs.size();
    }

    public void tickDiscovery(LoadedStructureReader reader,
                              BiConsumer<UUID, GeometryDiscoveryResult> resultConsumer) {
        GeometryDiscoveryJob job = discoveryJobs.pollFirst();
        if (job == null) return;
        GeometryDiscoveryResult result = job.advance(reader, budget);
        resultConsumer.accept(job.structureId(), result);
        if (result.terminal()) discoveryIds.remove(job.structureId());
        else discoveryJobs.addLast(job);
    }

    public void tickWorldWork(LoadedStructureReader reader,
                              BiConsumer<UUID, GeometryDiscoveryResult> discoveryConsumer,
                              BiConsumer<UUID, ValidationResult> validationConsumer) {
        if (!discoveryJobs.isEmpty() && (validationJobs.isEmpty() || discoveryTurn)) {
            tickDiscovery(reader, discoveryConsumer);
            discoveryTurn = false;
            return;
        }
        if (!validationJobs.isEmpty()) {
            tickValidation(reader, validationConsumer);
            discoveryTurn = true;
        }
    }

    public void tickValidation(LoadedStructureReader reader, BiConsumer<UUID, ValidationResult> resultConsumer) {
        ValidationJob job = validationJobs.pollFirst();
        if (job == null) return;
        ValidationResult result = job.advance(reader, budget);
        resultConsumer.accept(job.structureId(), result);
        if (result.terminal()) validationIds.remove(job.structureId());
        else validationJobs.addLast(job);
    }

    public void tickApplications(ApplicationHandler handler) {
        int mutations = 0;
        int deferred = 0;
        while (mutations < budget.maximumMutations() && !applicationJobs.isEmpty()) {
            StructureApplicationJob job = applicationJobs.pollFirst();
            if (!handler.isCurrent(job)) continue;
            if (!handler.isReady(job)) {
                applicationJobs.addLast(job);
                if (++deferred >= applicationJobs.size()) break;
                continue;
            }
            deferred = 0;
            if (!job.isComplete()) {
                if (!handler.apply(job.structureId(), job.operation(), job.currentPosition())) {
                    applicationJobs.addLast(job);
                    break;
                }
                job = job.advance();
                mutations++;
            }
            if (job.isComplete()) handler.completed(job.structureId(), job.operation());
            else applicationJobs.addLast(job);
        }
    }

    public ValidationBudget budget() {
        return budget;
    }

    public void updateBudget(ValidationBudget budget) {
        if (budget == null) throw new IllegalArgumentException("budget is required");
        this.budget = budget;
    }

    public interface ApplicationHandler {
        default boolean isCurrent(StructureApplicationJob job) { return true; }
        default boolean isReady(StructureApplicationJob job) { return true; }

        boolean apply(UUID structureId, StructureApplicationJob.Operation operation, BlockPos portPosition);

        default void completed(UUID structureId, StructureApplicationJob.Operation operation) {
        }
    }
}
