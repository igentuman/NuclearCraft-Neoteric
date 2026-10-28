package igentuman.nc.handler.crafter;

import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.ProcessorIndex.Link;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CraftingJob {

    public enum Phase { COLLECTING, CRAFTING, DONE }

    private static final int STALL_LIMIT = 1200;
    private static final int IDLE_LIMIT = 2400;
    private static final int ACTIVE_IDLE_LIMIT = 12000;

    private Phase phase;
    private ItemStack target;
    private int qty;
    private final List<PatternDef<CraftKey>> operations;
    private int opIndex;
    private final Map<CraftKey, Integer> baseCost;
    private final Map<CraftKey, Integer> buffer;
    private final List<ProcTask> tasks = new ArrayList<>();
    private int stallTicks;
    private boolean finished;

    private static class ProcTask {
        BlockPos pos;
        Map<CraftKey, Integer> expected = new LinkedHashMap<>();
        int idle;
    }

    public CraftingJob(ItemStack target, int qty, Plan<CraftKey> plan) {
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
            case CRAFTING -> craft(be);
            case DONE -> finish(be);
        }
    }

    public void cancel(EngineersCrafterBE be) {
        tasks.clear();
        phase = Phase.DONE;
        finish(be);
    }

    // --- collecting ---

    private void collect(EngineersCrafterBE be) {
        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        for (Map.Entry<CraftKey, Integer> e : baseCost.entrySet()) {
            int have = e.getKey().isFluid()
                    ? agg.countFluid(e.getKey().sampleFluid())
                    : agg.count(e.getKey().sampleItem());
            if (have < e.getValue()) {
                cancel(be);
                return;
            }
        }
        for (Map.Entry<CraftKey, Integer> e : baseCost.entrySet()) {
            CraftKey key = e.getKey();
            int got;
            if (key.isFluid()) {
                got = agg.extractFluid(key.sampleFluid(), e.getValue(), false).getAmount();
            } else {
                got = agg.extract(key.sampleItem(), e.getValue(), false).getCount();
            }
            bufferAdd(key, got);
            if (got < e.getValue()) {
                cancel(be);
                return;
            }
        }
        be.markUpdated();
        phase = operations.isEmpty() ? Phase.DONE : Phase.CRAFTING;
    }

    // --- crafting ---

    private void craft(EngineersCrafterBE be) {
        if (!tasks.isEmpty() && !collectTasks(be)) {
            cancel(be);
            return;
        }
        if (opIndex >= operations.size()) {
            if (tasks.isEmpty()) phase = Phase.DONE;
            return;
        }
        PatternDef<CraftKey> op = operations.get(opIndex);
        if (!op.isProcessing()) {
            if (hasInputs(op, 1)) {
                craftOne(op);
                stallTicks = 0;
            } else if (tasks.isEmpty()) {
                // nothing in flight can still supply the inputs: the buffer is inconsistent
                cancel(be);
            }
            return;
        }
        if (dispatch(be, op)) {
            stallTicks = 0;
        } else if (tasks.isEmpty() && ++stallTicks > STALL_LIMIT) {
            cancel(be);
        }
    }

    private boolean hasInputs(PatternDef<CraftKey> op, int times) {
        for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
            if ((long) buffer.getOrDefault(in.getKey(), 0) < (long) in.getValue() * times) return false;
        }
        return true;
    }

    private void craftOne(PatternDef<CraftKey> op) {
        for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
            bufferRemove(in.getKey(), in.getValue());
        }
        bufferAdd(op.output(), op.outputCount());
        for (Map.Entry<CraftKey, Integer> extra : op.extraOutputs().entrySet()) {
            bufferAdd(extra.getKey(), extra.getValue());
        }
        opIndex++;
        if (opIndex >= operations.size() && tasks.isEmpty()) {
            phase = Phase.DONE;
        }
    }

    private boolean dispatch(EngineersCrafterBE be, PatternDef<CraftKey> op) {
        int run = 1;
        while (opIndex + run < operations.size() && operations.get(opIndex + run).equals(op)) run++;
        int byBuffer = Integer.MAX_VALUE;
        for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
            if (in.getValue() <= 0) continue;
            byBuffer = Math.min(byBuffer, buffer.getOrDefault(in.getKey(), 0) / in.getValue());
        }
        run = Math.min(run, byBuffer);
        if (run <= 0) return false;

        boolean needItems = false;
        boolean needFluids = false;
        for (CraftKey k : op.inputs().keySet()) {
            if (k.isFluid()) needFluids = true;
            else needItems = true;
        }
        if (op.output().isFluid()) needFluids = true;
        else needItems = true;
        for (CraftKey k : op.extraOutputs().keySet()) {
            if (k.isFluid()) needFluids = true;
            else needItems = true;
        }

        boolean any = false;
        for (Link link : be.processorLinks()) {
            if (run <= 0) break;
            if (!link.supports(op.processor(), needItems, needFluids) || isBusy(link.pos())) continue;
            if (!be.getLevel().isLoaded(link.pos())) continue;
            if (!(be.getLevel().getBlockEntity(link.pos()) instanceof UniversalProcessorBE proc)) continue;
            int n = Math.min(run, fit(proc, op, run));
            if (n <= 0) continue;
            insertInputs(proc, op, n);
            for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
                bufferRemove(in.getKey(), in.getValue() * n);
            }
            ProcTask task = new ProcTask();
            task.pos = link.pos().immutable();
            task.expected.merge(op.output(), op.outputCount() * n, Integer::sum);
            for (Map.Entry<CraftKey, Integer> extra : op.extraOutputs().entrySet()) {
                task.expected.merge(extra.getKey(), extra.getValue() * n, Integer::sum);
            }
            tasks.add(task);
            opIndex += n;
            run -= n;
            any = true;
        }
        return any;
    }

    private boolean isBusy(BlockPos pos) {
        for (ProcTask t : tasks) if (t.pos.equals(pos)) return true;
        return false;
    }

    private static int inputSlots(UniversalProcessorBE proc) {
        ModEntry entry = ModEntries.get(proc.name);
        return entry != null && entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
    }

    private static int outputSlots(UniversalProcessorBE proc) {
        ModEntry entry = ModEntries.get(proc.name);
        return entry != null && entry.itemCap() != null ? entry.itemCap().outputSlots : 0;
    }

    private static int inputTanks(UniversalProcessorBE proc) {
        ModEntry entry = ModEntries.get(proc.name);
        return entry != null && entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
    }

    private static int outputTanks(UniversalProcessorBE proc) {
        ModEntry entry = ModEntries.get(proc.name);
        return entry != null && entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;
    }

    private int fit(UniversalProcessorBE proc, PatternDef<CraftKey> op, int max) {
        int n = max;
        boolean[] usedSlots = new boolean[Math.max(0, inputSlots(proc))];
        boolean[] usedTanks = new boolean[Math.max(0, inputTanks(proc))];
        for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
            CraftKey key = in.getKey();
            for (int per : op.amounts(key)) {
                if (per <= 0) continue;
                int accepted;
                if (key.isFluid()) {
                    FluidCapabilityHandler fh = proc.contentHandler.getFluidHandler();
                    if (fh == null) return 0;
                    int tank = pickTank(fh, key.sampleFluid(), usedTanks);
                    if (tank < 0) return 0;
                    usedTanks[tank] = true;
                    accepted = fh.fillTank(tank, key.sampleFluid(per * n), FluidAction.SIMULATE);
                } else {
                    ItemCapabilityHandler ih = proc.contentHandler.getItemHandler();
                    if (ih == null) return 0;
                    ItemStack sample = key.sampleItem();
                    int slot = pickSlot(ih, sample, usedSlots);
                    if (slot < 0) return 0;
                    usedSlots[slot] = true;
                    long want = Math.min((long) per * n, sample.getMaxStackSize());
                    ItemStack left = ih.insertItem(slot, sample.copyWithCount((int) want), true);
                    accepted = (int) want - left.getCount();
                }
                n = Math.min(n, accepted / per);
                if (n <= 0) return 0;
            }
        }
        return n;
    }

    private static int pickSlot(ItemCapabilityHandler ih, ItemStack sample, boolean[] used) {
        int empty = -1;
        for (int s = 0; s < used.length; s++) {
            if (used[s]) continue;
            ItemStack cur = ih.getStackInSlot(s);
            if (!cur.isEmpty() && ItemStack.isSameItemSameComponents(cur, sample)) {
                int limit = Math.min(cur.getMaxStackSize(), ih.getSlotLimit(s));
                if (cur.getCount() < limit) return s;
            } else if (cur.isEmpty() && empty < 0 && ih.isItemValid(s, sample)) {
                empty = s;
            }
        }
        return empty;
    }

    private static int pickTank(FluidCapabilityHandler fh, FluidStack sample, boolean[] used) {
        int empty = -1;
        for (int t = 0; t < used.length; t++) {
            if (used[t]) continue;
            FluidStack cur = fh.getFluidInTank(t);
            if (!cur.isEmpty() && FluidStack.isSameFluidSameComponents(cur, sample)) {
                if (cur.getAmount() < fh.getTankCapacity(t)) return t;
            } else if (cur.isEmpty() && empty < 0 && fh.isFluidValid(t, sample)) {
                empty = t;
            }
        }
        return empty;
    }

    private void insertInputs(UniversalProcessorBE proc, PatternDef<CraftKey> op, int n) {
        boolean[] usedSlots = new boolean[Math.max(0, inputSlots(proc))];
        boolean[] usedTanks = new boolean[Math.max(0, inputTanks(proc))];
        for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
            CraftKey key = in.getKey();
            for (int per : op.amounts(key)) {
                int amount = per * n;
                if (amount <= 0) continue;
                if (key.isFluid()) {
                    FluidCapabilityHandler fh = proc.contentHandler.getFluidHandler();
                    int tank = pickTank(fh, key.sampleFluid(), usedTanks);
                    if (tank < 0) {
                        bufferAdd(key, amount);
                        continue;
                    }
                    usedTanks[tank] = true;
                    int filled = fh.fillTank(tank, key.sampleFluid(amount), FluidAction.EXECUTE);
                    if (filled < amount) bufferAdd(key, amount - filled);
                } else {
                    ItemCapabilityHandler ih = proc.contentHandler.getItemHandler();
                    ItemStack sample = key.sampleItem();
                    int slot = pickSlot(ih, sample, usedSlots);
                    if (slot < 0) {
                        bufferAdd(key, amount);
                        continue;
                    }
                    usedSlots[slot] = true;
                    ItemStack left = ih.insertItem(slot, sample.copyWithCount(amount), false);
                    // anything the slot refused after the simulate pass goes back to the buffer
                    if (!left.isEmpty()) bufferAdd(key, left.getCount());
                }
            }
        }
        proc.setChanged();
    }

    private boolean collectTasks(EngineersCrafterBE be) {
        Level level = be.getLevel();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            ProcTask task = tasks.get(i);
            if (!level.isLoaded(task.pos)) continue; // wait for the chunk; never force-load it
            BlockEntity raw = level.getBlockEntity(task.pos);
            if (!(raw instanceof UniversalProcessorBE proc)) return false; // machine is gone, plan cannot complete
            boolean progressed = false;
            int inSlots = inputSlots(proc);
            int outSlots = outputSlots(proc);
            int inTanks = inputTanks(proc);
            int outTanks = outputTanks(proc);
            for (Map.Entry<CraftKey, Integer> e : new ArrayList<>(task.expected.entrySet())) {
                CraftKey key = e.getKey();
                int want = e.getValue();
                if (want <= 0) {
                    task.expected.remove(key);
                    continue;
                }
                int got = 0;
                if (key.isFluid()) {
                    FluidCapabilityHandler fh = proc.contentHandler.getFluidHandler();
                    if (fh != null) {
                        FluidStack sample = key.sampleFluid();
                        for (int t = inTanks; t < inTanks + outTanks && want > 0; t++) {
                            FluidStack cur = fh.getFluidInTank(t);
                            if (cur.isEmpty() || !FluidStack.isSameFluidSameComponents(cur, sample)) continue;
                            FluidStack drained = fh.drainTank(t, want, FluidAction.EXECUTE);
                            if (drained.isEmpty()) continue;
                            got += drained.getAmount();
                            want -= drained.getAmount();
                        }
                    }
                } else {
                    ItemCapabilityHandler ih = proc.contentHandler.getItemHandler();
                    if (ih != null) {
                        ItemStack sample = key.sampleItem();
                        for (int s = inSlots; s < inSlots + outSlots && want > 0; s++) {
                            ItemStack cur = ih.getStackInSlot(s);
                            if (cur.isEmpty() || !ItemStack.isSameItemSameComponents(cur, sample)) continue;
                            ItemStack taken = ih.extractItem(s, want, false);
                            if (taken.isEmpty()) continue;
                            got += taken.getCount();
                            want -= taken.getCount();
                        }
                    }
                }
                if (got > 0) {
                    progressed = true;
                    bufferAdd(key, got);
                    if (want <= 0) task.expected.remove(key);
                    else task.expected.put(key, want);
                }
            }
            if (progressed) {
                task.idle = 0;
                proc.setChanged();
            } else if (++task.idle > (proc.recipeInfo.active ? ACTIVE_IDLE_LIMIT : IDLE_LIMIT)) {
                return false;
            }
            if (task.expected.isEmpty()) tasks.remove(i);
        }
        return true;
    }

    // --- finishing ---

    private void finish(EngineersCrafterBE be) {
        flushBuffer(be);
        finished = buffer.isEmpty();
    }

    private void flushBuffer(EngineersCrafterBE be) {
        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        boolean changed = false;
        Map<CraftKey, Integer> keep = new LinkedHashMap<>();
        for (Map.Entry<CraftKey, Integer> e : buffer.entrySet()) {
            CraftKey key = e.getKey();
            int count = e.getValue();
            if (count <= 0) continue;
            if (key.isFluid()) {
                FluidStack fluid = key.sampleFluid(count);
                if (fluid.isEmpty()) continue;
                int filled = agg.insertFluid(fluid, false);
                if (filled > 0) changed = true;
                if (filled < count) keep.put(key, count - filled);
                continue;
            }
            ItemStack sample = key.sampleItem();
            if (sample.isEmpty()) continue;
            int max = Math.max(1, sample.getMaxStackSize());
            while (count > 0) {
                int chunk = Math.min(count, max);
                count -= chunk;
                ItemStack s = sample.copyWithCount(chunk);
                ItemStack leftover = agg.insert(s, false);
                if (leftover.getCount() < chunk) changed = true;
                if (!leftover.isEmpty()) dropInWorld(be, leftover);
            }
        }
        buffer.clear();
        buffer.putAll(keep);
        if (changed) be.markUpdated();
    }

    private void dropInWorld(EngineersCrafterBE be, ItemStack stack) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide) return;
        BlockPos pos = be.getBlockPos();
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
    }

    private void bufferAdd(CraftKey key, int n) {
        if (n > 0) buffer.merge(key, n, Integer::sum);
    }

    private void bufferRemove(CraftKey key, int n) {
        if (n <= 0) return;
        int left = buffer.getOrDefault(key, 0) - n;
        if (left > 0) buffer.put(key, left);
        else buffer.remove(key);
    }

    // --- NBT ---

    public void save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("Phase", phase.name());
        tag.put("Target", target.save(provider));
        tag.putInt("Qty", qty);
        tag.putInt("OpIndex", opIndex);
        tag.putInt("Stall", stallTicks);
        tag.putBoolean("Finished", finished);
        tag.put("Ops", writeOps(provider));
        tag.put("BaseCost", writeMap(baseCost, provider));
        tag.put("Buffer", writeMap(buffer, provider));
        ListTag taskList = new ListTag();
        for (ProcTask t : tasks) {
            CompoundTag c = new CompoundTag();
            c.put("Pos", NbtUtils.writeBlockPos(t.pos));
            c.putInt("Idle", t.idle);
            c.put("Expected", writeMap(t.expected, provider));
            taskList.add(c);
        }
        tag.put("Tasks", taskList);
    }

    public static CraftingJob load(CompoundTag tag, HolderLookup.Provider provider) {
        CraftingJob job = new CraftingJob();
        job.phase = Phase.valueOf(tag.getString("Phase"));
        job.target = ItemStack.parseOptional(provider, tag.getCompound("Target"));
        job.qty = tag.getInt("Qty");
        job.opIndex = tag.getInt("OpIndex");
        job.stallTicks = tag.getInt("Stall");
        job.finished = tag.getBoolean("Finished");
        if (!readOps(tag.getList("Ops", Tag.TAG_COMPOUND), job.operations, provider)) {
            // an op no longer decodes (mod removed?): abandon the plan and just return what was buffered
            job.operations.clear();
            job.tasks.clear();
            job.phase = Phase.DONE;
        }
        readMap(tag.getList("BaseCost", Tag.TAG_COMPOUND), job.baseCost, provider);
        readMap(tag.getList("Buffer", Tag.TAG_COMPOUND), job.buffer, provider);
        ListTag taskList = tag.getList("Tasks", Tag.TAG_COMPOUND);
        for (int i = 0; i < taskList.size(); i++) {
            CompoundTag c = taskList.getCompound(i);
            ProcTask t = new ProcTask();
            t.pos = NbtUtils.readBlockPos(c, "Pos").orElse(BlockPos.ZERO);
            t.idle = c.getInt("Idle");
            readMap(c.getList("Expected", Tag.TAG_COMPOUND), t.expected, provider);
            job.tasks.add(t);
        }
        return job;
    }

    private ListTag writeOps(HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (PatternDef<CraftKey> op : operations) {
            CompoundTag c = new CompoundTag();
            writeKey(c, "Out", op.output(), provider);
            c.putInt("OutN", op.outputCount());
            c.put("In", writeMap(op.inputs(), provider));
            if (op.isProcessing()) {
                c.putString("Proc", op.processor());
                c.put("Extra", writeMap(op.extraOutputs(), provider));
                c.put("Parts", writeAmounts(op.ingredientAmounts(), provider));
            }
            list.add(c);
        }
        return list;
    }

    private static boolean readOps(ListTag list, List<PatternDef<CraftKey>> out, HolderLookup.Provider provider) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            CraftKey outKey = readKey(c, "Out", provider);
            if (outKey == null) return false;
            Map<CraftKey, Integer> inputs = new LinkedHashMap<>();
            readMap(c.getList("In", Tag.TAG_COMPOUND), inputs, provider);
            Map<CraftKey, Integer> extra = new LinkedHashMap<>();
            readMap(c.getList("Extra", Tag.TAG_COMPOUND), extra, provider);
            Map<CraftKey, List<Integer>> parts = readAmounts(c.getList("Parts", Tag.TAG_COMPOUND), provider);
            out.add(new PatternDef<>(outKey, c.getInt("OutN"), inputs, c.getString("Proc"), extra, parts));
        }
        return true;
    }

    private static ListTag writeAmounts(Map<CraftKey, List<Integer>> map, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<CraftKey, List<Integer>> e : map.entrySet()) {
            CompoundTag c = new CompoundTag();
            writeKey(c, "Item", e.getKey(), provider);
            c.putIntArray("Amounts", e.getValue().stream().mapToInt(Integer::intValue).toArray());
            list.add(c);
        }
        return list;
    }

    private static Map<CraftKey, List<Integer>> readAmounts(ListTag list, HolderLookup.Provider provider) {
        Map<CraftKey, List<Integer>> out = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            CraftKey key = readKey(c, "Item", provider);
            if (key == null) continue;
            List<Integer> amounts = new ArrayList<>();
            for (int a : c.getIntArray("Amounts")) amounts.add(a);
            out.put(key, amounts);
        }
        return out;
    }

    private static ListTag writeMap(Map<CraftKey, Integer> map, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<CraftKey, Integer> e : map.entrySet()) {
            CompoundTag c = new CompoundTag();
            writeKey(c, "Item", e.getKey(), provider);
            c.putInt("N", e.getValue());
            list.add(c);
        }
        return list;
    }

    private static void readMap(ListTag list, Map<CraftKey, Integer> out, HolderLookup.Provider provider) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            CraftKey key = readKey(c, "Item", provider);
            if (key != null) out.merge(key, c.getInt("N"), Integer::sum);
        }
    }

    private static void writeKey(CompoundTag c, String name, CraftKey key, HolderLookup.Provider provider) {
        if (key.isFluid()) {
            c.put(name + "Fluid", key.sampleFluid().save(provider));
        } else {
            c.put(name, key.sampleItem().save(provider));
        }
    }

    private static CraftKey readKey(CompoundTag c, String name, HolderLookup.Provider provider) {
        if (c.contains(name + "Fluid")) {
            FluidStack f = FluidStack.parseOptional(provider, c.getCompound(name + "Fluid"));
            return f.isEmpty() ? null : CraftKey.of(f);
        }
        ItemStack s = ItemStack.parseOptional(provider, c.getCompound(name));
        return s.isEmpty() ? null : CraftKey.of(s);
    }
}
