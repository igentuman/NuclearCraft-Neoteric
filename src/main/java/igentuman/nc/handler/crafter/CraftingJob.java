package igentuman.nc.handler.crafter;

import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.ProcessorIndex.Link;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/** Resumable crafting work with an atomic collection phase and current-format state. */
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
        boolean complete = true;
        try (var transaction = Transaction.openRoot()) {
            for (Map.Entry<CraftKey, Integer> e : baseCost.entrySet()) {
                CraftKey key = e.getKey();
                int got = key.isFluid()
                        ? agg.extractFluid(key.sampleFluid(), e.getValue(), transaction).getAmount()
                        : agg.extract(key.sampleItem(), e.getValue(), transaction).getCount();
                if (got != e.getValue()) {
                    complete = false;
                    break;
                }
            }
            if (complete) transaction.commit();
        }
        if (!complete) {
            cancel(be);
            return;
        }
        baseCost.forEach(this::bufferAdd);
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
        if (level == null || level.isClientSide()) return;
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
        tag.store("Id", UUIDUtil.CODEC, id);
        tag.putString("Phase", phase.name());
        tag.store("Target", ItemStack.CODEC, provider.createSerializationContext(NbtOps.INSTANCE), target);
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
            c.store("Pos", BlockPos.CODEC, t.pos);
            c.putInt("Idle", t.idle);
            c.put("Expected", writeMap(t.expected, provider));
            taskList.add(c);
        }
        tag.put("Tasks", taskList);
    }

    public static CraftingJob load(CompoundTag tag, HolderLookup.Provider provider) {
        CraftingJob job = new CraftingJob();
        job.id = tag.read("Id", UUIDUtil.CODEC).orElseThrow(() -> new IllegalArgumentException("Missing crafting job Id"));
        job.phase = Phase.valueOf(tag.getStringOr("Phase", ""));
        job.target = tag.read("Target", ItemStack.CODEC, provider.createSerializationContext(NbtOps.INSTANCE))
                .orElseThrow(() -> new IllegalArgumentException("Invalid crafting job target"));
        job.qty = tag.getIntOr("Qty", 0);
        job.opIndex = tag.getIntOr("OpIndex", 0);
        job.stallTicks = tag.getIntOr("Stall", 0);
        job.finished = tag.getBooleanOr("Finished", false);
        if (!readOps(currentList(tag, "Ops"), job.operations, provider)) {
            // an op no longer decodes (mod removed?): abandon the plan and just return what was buffered
            job.operations.clear();
            job.opIndex = 0;
            job.tasks.clear();
            job.phase = Phase.DONE;
        }
        readMap(currentList(tag, "BaseCost"), job.baseCost, provider);
        readMap(currentList(tag, "Buffer"), job.buffer, provider);
        ListTag taskList = currentList(tag, "Tasks");
        for (int i = 0; job.phase == Phase.CRAFTING && i < taskList.size(); i++) {
            CompoundTag c = taskList.getCompoundOrEmpty(i);
            ProcTask t = new ProcTask();
            t.pos = c.read("Pos", BlockPos.CODEC)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid crafting processor position"));
            t.idle = c.getIntOr("Idle", 0);
            if (t.idle < 0 || job.tasks.stream().anyMatch(task -> task.pos.equals(t.pos))) {
                throw new IllegalArgumentException("Invalid or duplicate crafting processor task");
            }
            readMap(currentList(c, "Expected"), t.expected, provider);
            job.tasks.add(t);
        }
        if (job.qty <= 0 || job.opIndex < 0 || job.opIndex > job.operations.size() || job.stallTicks < 0) {
            throw new IllegalArgumentException("Invalid crafting job progress");
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
            CompoundTag c = list.getCompoundOrEmpty(i);
            Map<CraftKey, Integer> outputs = new LinkedHashMap<>();
            readMap(currentList(c, "Outputs"), outputs, provider);
            Map<CraftKey, Integer> inputs = new LinkedHashMap<>();
            readMap(currentList(c, "In"), inputs, provider);
            String processor = c.getStringOr("Proc", "");
            String recipe = c.getStringOr("Recipe", "");
            if (!processor.isEmpty() && recipe.isEmpty()) throw new IllegalArgumentException("Missing crafting processor recipe");
            // A processing destination must still be supported before resuming its plan.
            if (!processor.isEmpty() && !ProcessorRecipes.isAllowed(processor)) return false;
            if (outputs.isEmpty() || inputs.isEmpty()) throw new IllegalArgumentException("Empty crafting operation");
            Map<CraftKey, List<Integer>> parts = processor.isEmpty()
                    ? Map.of() : readAmounts(currentList(c, "Parts"), provider);
            if (!processor.isEmpty()) {
                if (!parts.keySet().equals(inputs.keySet())) throw new IllegalArgumentException("Job input placements do not match inputs");
                for (var entry : parts.entrySet()) {
                    long amount = entry.getValue().stream().mapToLong(Integer::longValue).sum();
                    if (amount != inputs.get(entry.getKey())) throw new IllegalArgumentException("Job input amounts do not match inputs");
                }
            }
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
            CompoundTag c = list.getCompoundOrEmpty(i);
            CraftKey key = readKey(c, "Item", provider);
            List<Integer> amounts = new ArrayList<>();
            for (int amount : c.getIntArray("Amounts").orElseThrow(() -> new IllegalArgumentException("Missing job ingredient amounts"))) {
                if (amount <= 0) throw new IllegalArgumentException("Invalid job ingredient amount");
                amounts.add(amount);
            }
            if (amounts.isEmpty() || out.putIfAbsent(key, amounts) != null) {
                throw new IllegalArgumentException("Empty or duplicate job ingredient amounts");
            }
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
            CompoundTag c = list.getCompoundOrEmpty(i);
            CraftKey key = readKey(c, "Item", provider);
            int amount = c.getIntOr("N", 0);
            if (amount <= 0 || out.putIfAbsent(key, amount) != null) {
                throw new IllegalArgumentException("Invalid or duplicate crafting job resource amount");
            }
        }
    }

    private static void writeKey(CompoundTag c, String name, CraftKey key, HolderLookup.Provider provider) {
        if (key.isFluid()) {
            c.store(name + "Fluid", FluidStack.CODEC, provider.createSerializationContext(NbtOps.INSTANCE), key.sampleFluid());
        } else {
            c.store(name, ItemStack.CODEC, provider.createSerializationContext(NbtOps.INSTANCE), key.sampleItem());
        }
    }

    private static CraftKey readKey(CompoundTag tag, String name, HolderLookup.Provider provider) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        if (tag.contains(name + "Fluid")) {
            if (tag.contains(name)) throw new IllegalArgumentException("Ambiguous crafting job resource");
            return CraftKey.of(tag.read(name + "Fluid", FluidStack.CODEC, ops)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid crafting job fluid")));
        }
        return CraftKey.of(tag.read(name, ItemStack.CODEC, ops)
                .orElseThrow(() -> new IllegalArgumentException("Invalid crafting job item")));
    }

    private static ListTag currentList(CompoundTag tag, String name) {
        ListTag list = tag.getList(name)
                .orElseThrow(() -> new IllegalArgumentException("Missing crafting job list: " + name));
        for (var entry : list) {
            if (!(entry instanceof CompoundTag)) throw new IllegalArgumentException("Invalid crafting job list: " + name);
        }
        return list;
    }
}
