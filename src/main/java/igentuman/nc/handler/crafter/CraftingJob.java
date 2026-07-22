package igentuman.nc.handler.crafter;

import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

    public void step(EngineersCrafterBE be) {
        switch (phase) {
            case COLLECTING -> collect(be);
            case CRAFTING -> craftOne();
            case DONE -> finish(be);
        }
    }

    public void cancel(EngineersCrafterBE be) {
        flushBuffer(be);
        finished = true;
    }

    private void collect(EngineersCrafterBE be) {
        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        for (Map.Entry<ItemKey, Integer> e : baseCost.entrySet()) {
            if (agg.count(sample(e.getKey())) < e.getValue()) {
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
        return key.sample();
    }

    // --- NBT ---

    public void save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("Phase", phase.name());
        tag.put("Target", target.save(provider));
        tag.putInt("Qty", qty);
        tag.putInt("OpIndex", opIndex);
        tag.putBoolean("Finished", finished);
        tag.put("Ops", writeOps(provider));
        tag.put("BaseCost", writeMap(baseCost, provider));
        tag.put("Buffer", writeMap(buffer, provider));
    }

    public static CraftingJob load(CompoundTag tag, HolderLookup.Provider provider) {
        CraftingJob job = new CraftingJob();
        job.phase = Phase.valueOf(tag.getString("Phase"));
        job.target = ItemStack.parseOptional(provider, tag.getCompound("Target"));
        job.qty = tag.getInt("Qty");
        job.opIndex = tag.getInt("OpIndex");
        job.finished = tag.getBoolean("Finished");
        readOps(tag.getList("Ops", Tag.TAG_COMPOUND), job.operations, provider);
        readMap(tag.getList("BaseCost", Tag.TAG_COMPOUND), job.baseCost, provider);
        readMap(tag.getList("Buffer", Tag.TAG_COMPOUND), job.buffer, provider);
        return job;
    }

    private ListTag writeOps(HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (PatternDef<ItemKey> op : operations) {
            CompoundTag c = new CompoundTag();
            c.put("Out", sample(op.output()).save(provider));
            c.putInt("OutN", op.outputCount());
            c.put("In", writeMap(op.inputs(), provider));
            list.add(c);
        }
        return list;
    }

    private static void readOps(ListTag list, List<PatternDef<ItemKey>> out, HolderLookup.Provider provider) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            ItemKey outKey = ItemKey.of(ItemStack.parseOptional(provider, c.getCompound("Out")));
            Map<ItemKey, Integer> inputs = new LinkedHashMap<>();
            readMap(c.getList("In", Tag.TAG_COMPOUND), inputs, provider);
            out.add(new PatternDef<>(outKey, c.getInt("OutN"), inputs));
        }
    }

    private static ListTag writeMap(Map<ItemKey, Integer> map, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<ItemKey, Integer> e : map.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.put("Item", sample(e.getKey()).save(provider));
            c.putInt("N", e.getValue());
            list.add(c);
        }
        return list;
    }

    private static void readMap(ListTag list, Map<ItemKey, Integer> out, HolderLookup.Provider provider) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            ItemKey key = ItemKey.of(ItemStack.parseOptional(provider, c.getCompound("Item")));
            out.merge(key, c.getInt("N"), Integer::sum);
        }
    }
}
