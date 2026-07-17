package igentuman.nc.handler.crafter;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateful, multi-tick autocraft job. Persisted in the crafter BE's NBT so it survives unload/restart.
 * <p>
 * Lifecycle: {@code COLLECTING} pulls the whole {@code baseCost} out of the container inventory into an
 * internal buffer ("consume all first, then craft"); {@code CRAFTING} executes exactly one plan
 * operation per tick, consuming inputs from and adding outputs to the buffer; {@code DONE} flushes the
 * buffer (target plus surplus intermediates) back to the containers, dropping any overflow in world.
 * <p>
 * Energy gating is the caller's job (the BE decides affordability and drains). The buffer is a plain
 * count map rather than an {@code ItemStackHandler} because a plan can reserve far more than 64 of an
 * item, which no fixed-slot handler could hold.
 */
public class CraftingJob {

    public enum Phase { COLLECTING, CRAFTING, DONE }

    private Phase phase;
    private ItemStack target;
    private int qty;
    private final List<PatternDef<ItemKey>> operations;
    private int opIndex;
    private final Map<ItemKey, Integer> baseCost;
    private final Map<ItemKey, Integer> buffer;
    private boolean finished;

    public CraftingJob(ItemStack target, int qty, Plan<ItemKey> plan) {
        this.phase = Phase.COLLECTING;
        this.target = target.copy();
        this.target.setCount(1);
        this.qty = qty;
        this.operations = new ArrayList<>(plan.operations());
        this.opIndex = 0;
        this.baseCost = new LinkedHashMap<>(plan.baseCost());
        this.buffer = new LinkedHashMap<>();
    }

    private CraftingJob() {
        this.operations = new ArrayList<>();
        this.baseCost = new LinkedHashMap<>();
        this.buffer = new LinkedHashMap<>();
    }

    public boolean isFinished() {
        return finished;
    }

    public Phase phase() {
        return phase;
    }

    public int opIndex() {
        return opIndex;
    }

    public int operationsSize() {
        return operations.size();
    }

    /** One tick of work. The caller must have already checked/charged energy for the active phases. */
    public void step(EngineersCrafterBE be) {
        switch (phase) {
            case COLLECTING -> collect(be);
            case CRAFTING -> craftOne();
            case DONE -> finish(be);
        }
    }

    /** Aborts the job and returns whatever is already reserved back to the containers. */
    public void cancel(EngineersCrafterBE be) {
        flushBuffer(be);
        finished = true;
    }

    private void collect(EngineersCrafterBE be) {
        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        for (Map.Entry<ItemKey, Integer> e : baseCost.entrySet()) {
            if (agg.count(sample(e.getKey())) < e.getValue()) {
                // an ingredient went missing since planning -> return what we have and give up
                cancel(be);
                return;
            }
        }
        for (Map.Entry<ItemKey, Integer> e : baseCost.entrySet()) {
            ItemStack got = agg.extract(sample(e.getKey()), e.getValue(), false);
            bufferAdd(e.getKey(), got.getCount());
            if (got.getCount() < e.getValue()) {
                cancel(be);
                return;
            }
        }
        be.markUpdated();
        phase = operations.isEmpty() ? Phase.DONE : Phase.CRAFTING;
    }

    private void craftOne() {
        PatternDef<ItemKey> op = operations.get(opIndex);
        for (Map.Entry<ItemKey, Integer> in : op.inputs().entrySet()) {
            bufferRemove(in.getKey(), in.getValue());
        }
        bufferAdd(op.output(), op.outputCount());
        opIndex++;
        if (opIndex >= operations.size()) {
            phase = Phase.DONE;
        }
    }

    private void finish(EngineersCrafterBE be) {
        flushBuffer(be);
        finished = true;
    }

    private void flushBuffer(EngineersCrafterBE be) {
        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        boolean changed = false;
        for (Map.Entry<ItemKey, Integer> e : buffer.entrySet()) {
            ItemStack sample = sample(e.getKey());
            if (sample.isEmpty()) continue;
            int max = Math.max(1, sample.getMaxStackSize());
            int count = e.getValue();
            while (count > 0) {
                int chunk = Math.min(count, max);
                count -= chunk;
                ItemStack s = sample.copy();
                s.setCount(chunk);
                ItemStack leftover = agg.insert(s, false);
                if (leftover.getCount() < chunk) changed = true;
                if (!leftover.isEmpty()) dropInWorld(be, leftover);
            }
        }
        buffer.clear();
        if (changed) be.markUpdated();
    }

    private void dropInWorld(EngineersCrafterBE be, ItemStack stack) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide) return;
        BlockPos pos = be.getBlockPos();
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
    }

    private void bufferAdd(ItemKey key, int n) {
        if (n > 0) buffer.merge(key, n, Integer::sum);
    }

    private void bufferRemove(ItemKey key, int n) {
        if (n <= 0) return;
        int left = buffer.getOrDefault(key, 0) - n;
        if (left > 0) buffer.put(key, left);
        else buffer.remove(key);
    }

    private static ItemStack sample(ItemKey key) {
        ItemStack s = new ItemStack(key.item());
        if (key.tag() != null) s.setTag(key.tag().copy());
        return s;
    }

    // --- NBT ---

    public void save(CompoundTag tag) {
        tag.putString("Phase", phase.name());
        tag.put("Target", target.save(new CompoundTag()));
        tag.putInt("Qty", qty);
        tag.putInt("OpIndex", opIndex);
        tag.putBoolean("Finished", finished);
        tag.put("Ops", writeOps());
        tag.put("BaseCost", writeMap(baseCost));
        tag.put("Buffer", writeMap(buffer));
    }

    public static CraftingJob load(CompoundTag tag) {
        CraftingJob job = new CraftingJob();
        job.phase = Phase.valueOf(tag.getString("Phase"));
        job.target = ItemStack.of(tag.getCompound("Target"));
        job.qty = tag.getInt("Qty");
        job.opIndex = tag.getInt("OpIndex");
        job.finished = tag.getBoolean("Finished");
        readOps(tag.getList("Ops", Tag.TAG_COMPOUND), job.operations);
        readMap(tag.getList("BaseCost", Tag.TAG_COMPOUND), job.baseCost);
        readMap(tag.getList("Buffer", Tag.TAG_COMPOUND), job.buffer);
        return job;
    }

    private ListTag writeOps() {
        ListTag list = new ListTag();
        for (PatternDef<ItemKey> op : operations) {
            CompoundTag c = new CompoundTag();
            c.put("Out", sample(op.output()).save(new CompoundTag()));
            c.putInt("OutN", op.outputCount());
            c.put("In", writeMap(op.inputs()));
            list.add(c);
        }
        return list;
    }

    private static void readOps(ListTag list, List<PatternDef<ItemKey>> out) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            ItemKey outKey = ItemKey.of(ItemStack.of(c.getCompound("Out")));
            Map<ItemKey, Integer> inputs = new LinkedHashMap<>();
            readMap(c.getList("In", Tag.TAG_COMPOUND), inputs);
            out.add(new PatternDef<>(outKey, c.getInt("OutN"), inputs));
        }
    }

    private static ListTag writeMap(Map<ItemKey, Integer> map) {
        ListTag list = new ListTag();
        for (Map.Entry<ItemKey, Integer> e : map.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.put("Item", sample(e.getKey()).save(new CompoundTag()));
            c.putInt("N", e.getValue());
            list.add(c);
        }
        return list;
    }

    private static void readMap(ListTag list, Map<ItemKey, Integer> out) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            ItemKey key = ItemKey.of(ItemStack.of(c.getCompound("Item")));
            out.merge(key, c.getInt("N"), Integer::sum);
        }
    }
}
