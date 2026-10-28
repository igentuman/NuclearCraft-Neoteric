package igentuman.nc.handler.crafter;

import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.ProcessorIndex.Link;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class CraftingJob {

    public enum Phase { COLLECTING, CRAFTING, DONE }

    private static final int STALL_LIMIT = 1200;
    private static final int IDLE_LIMIT = 2400;
    private static final int ACTIVE_IDLE_LIMIT = 12000;

    private UUID id = UUID.randomUUID();
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

    public boolean ownsProcessor(UUID jobId, BlockPos processor) {
        return id.equals(jobId) && isBusy(processor);
    }

    public void cancel(EngineersCrafterBE be) {
        for (ProcTask task : tasks) {
            if (be.getLevel().isLoaded(task.pos)
                    && be.getLevel().getBlockEntity(task.pos) instanceof UniversalProcessorBE proc) {
                proc.recipeInfo.releaseCrafting(id);
            }
        }
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
        op.outputs().forEach(this::bufferAdd);
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
        for (CraftKey k : op.outputs().keySet()) {
            if (k.isFluid()) needFluids = true;
            else needItems = true;
        }

        boolean any = false;
        for (Link link : be.processorLinks()) {
            if (run <= 0) break;
            if (!link.supports(op.processor(), needItems, needFluids) || isBusy(link.pos())) continue;
            if (!be.getLevel().isLoaded(link.pos())) continue;
            if (!(be.getLevel().getBlockEntity(link.pos()) instanceof UniversalProcessorBE proc)) continue;
            if (!proc.name.equals(op.processor()) || !proc.recipeInfo.canAcceptCraftingOrder()) continue;
            ProcessorTransfer transfer = ProcessorTransfer.of(proc);
            ProcessorTransfer.InputPlan placement = transfer.plan(op, run);
            if (placement == null) continue;
            int n = placement.operations();
            if (!proc.recipeInfo.reserveCrafting(id, be.getBlockPos(), op.recipeId(), n)) continue;
            // Existing stock is returned separately; it cannot satisfy the newly submitted work.
            transfer.collectOutputs().forEach(this::bufferAdd);
            if (!transfer.insert(placement)) {
                proc.recipeInfo.releaseCrafting(id);
                continue;
            }
            for (Map.Entry<CraftKey, Integer> in : op.inputs().entrySet()) {
                bufferRemove(in.getKey(), in.getValue() * n);
            }
            ProcTask task = new ProcTask();
            task.pos = link.pos().immutable();
            op.outputs().forEach((key, amount) -> task.expected.put(key, amount * n));
            proc.setChanged();
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

    private boolean collectTasks(EngineersCrafterBE be) {
        Level level = be.getLevel();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            ProcTask task = tasks.get(i);
            if (!level.isLoaded(task.pos)) continue;
            BlockEntity raw = level.getBlockEntity(task.pos);
            if (!(raw instanceof UniversalProcessorBE proc) || !proc.recipeInfo.isReservedBy(id)) return false;
            Map<CraftKey, Integer> collected = ProcessorTransfer.of(proc).collectOutputs();
            collected.forEach((key, amount) -> {
                bufferAdd(key, amount);
                task.expected.computeIfPresent(key, (k, remaining) -> remaining > amount ? remaining - amount : null);
            });
            if (!collected.isEmpty()) {
                task.idle = 0;
                proc.setChanged();
            } else if (++task.idle > (proc.recipeInfo.active ? ACTIVE_IDLE_LIMIT : IDLE_LIMIT)) {
                return false;
            }
            if (task.expected.isEmpty() && proc.recipeInfo.isCraftingComplete(id)) {
                proc.recipeInfo.releaseCrafting(id);
                tasks.remove(i);
            }
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
        tag.putUUID("Id", id);
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
        if (tag.hasUUID("Id")) job.id = tag.getUUID("Id");
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
        for (int i = 0; job.phase == Phase.CRAFTING && i < taskList.size(); i++) {
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
            c.put("Outputs", writeMap(op.outputs(), provider));
            c.put("In", writeMap(op.inputs(), provider));
            if (op.isProcessing()) {
                c.putString("Proc", op.processor());
                c.putString("Recipe", op.recipeId());
                c.put("Parts", writeAmounts(op.ingredientAmounts(), provider));
            }
            list.add(c);
        }
        return list;
    }

    private static boolean readOps(ListTag list, List<PatternDef<CraftKey>> out, HolderLookup.Provider provider) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            Map<CraftKey, Integer> outputs = new LinkedHashMap<>();
            if (c.contains("Outputs")) {
                readMap(c.getList("Outputs", Tag.TAG_COMPOUND), outputs, provider);
            } else {
                CraftKey outKey = readKey(c, "Out", provider);
                if (outKey == null) return false;
                outputs.put(outKey, c.getInt("OutN"));
                readMap(c.getList("Extra", Tag.TAG_COMPOUND), outputs, provider);
            }
            Map<CraftKey, Integer> inputs = new LinkedHashMap<>();
            readMap(c.getList("In", Tag.TAG_COMPOUND), inputs, provider);
            String processor = c.getString("Proc");
            String recipe = c.getString("Recipe");
            // Legacy processing jobs cannot safely resume without a pinned recipe; keep their buffer for return.
            if (!processor.isEmpty() && (recipe.isEmpty() || !ProcessorRecipes.isAllowed(processor))) return false;
            if (outputs.isEmpty() || outputs.values().stream().anyMatch(n -> n <= 0)
                    || inputs.isEmpty() || inputs.values().stream().anyMatch(n -> n <= 0)) return false;
            Map<CraftKey, List<Integer>> parts = readAmounts(c.getList("Parts", Tag.TAG_COMPOUND), provider);
            out.add(new PatternDef<>(outputs, inputs, processor, recipe, parts));
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
