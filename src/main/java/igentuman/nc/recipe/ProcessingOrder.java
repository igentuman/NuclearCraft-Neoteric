package igentuman.nc.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** A crafter owns the processor until its exact recipe has finished every submitted operation. */
public final class ProcessingOrder {

    private final UUID jobId;
    private final BlockPos crafter;
    private final String recipeId;
    private int remaining;
    private int inFlight;

    public ProcessingOrder(UUID jobId, BlockPos crafter, String recipeId, int operations) {
        if (operations <= 0 || recipeId.isEmpty()) throw new IllegalArgumentException("Invalid processing order");
        this.jobId = jobId;
        this.crafter = crafter.immutable();
        this.recipeId = recipeId;
        this.remaining = operations;
    }

    public UUID jobId() { return jobId; }
    public BlockPos crafter() { return crafter; }
    public String recipeId() { return recipeId; }

    public int batchLimit(int parallelLimit) {
        return inFlight == 0 ? Math.min(remaining, parallelLimit) : 0;
    }

    public void started(int operations) {
        if (inFlight != 0 || operations <= 0 || operations > remaining) {
            throw new IllegalStateException("Invalid processing batch");
        }
        remaining -= operations;
        inFlight = operations;
    }

    public void completed() {
        inFlight = 0;
    }

    public boolean isComplete() {
        return remaining == 0 && inFlight == 0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.store("Job", UUIDUtil.CODEC, jobId);
        tag.store("Crafter", BlockPos.CODEC, crafter);
        tag.putString("Recipe", recipeId);
        tag.putInt("Remaining", remaining);
        tag.putInt("InFlight", inFlight);
        return tag;
    }

    public static ProcessingOrder load(CompoundTag tag) {
        BlockPos crafter = tag.read("Crafter", BlockPos.CODEC).orElse(null);
        String recipe = tag.getStringOr("Recipe", "");
        int remaining = tag.getIntOr("Remaining", 0);
        int inFlight = tag.getIntOr("InFlight", 0);
        UUID jobId = tag.read("Job", UUIDUtil.CODEC).orElse(null);
        if (jobId == null || crafter == null || recipe.isEmpty() || remaining < 0 || inFlight < 0) return null;
        ProcessingOrder order = new ProcessingOrder(jobId, crafter, recipe, Math.max(1, remaining));
        order.remaining = remaining;
        order.inFlight = inFlight;
        return order;
    }
}
