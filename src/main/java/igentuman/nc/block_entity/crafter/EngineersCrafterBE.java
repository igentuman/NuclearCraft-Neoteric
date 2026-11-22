package igentuman.nc.block_entity.crafter;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.AggregatedItemHandler;
import igentuman.nc.handler.crafter.AutoCraftSolver;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.CraftKey;
import igentuman.nc.handler.crafter.CraftingJob;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.handler.crafter.ProcessorIndex;
import igentuman.nc.handler.crafter.ProcessorIndex.Link;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.setup.entries.Crafter;
import igentuman.nc.handler.item.CustomInventoryHandler;
import igentuman.nc.util.persistence.TagDataIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EngineersCrafterBE extends BlockEntity {

    public static final int ENERGY_CAPACITY = 10_000;
    public static final int ENERGY_MAX_RECEIVE = 1_000;
    public static final int PASSIVE_FE = 100;
    public static final int CRAFT_FE = 200;
    public static final int PATTERNS_SIZE = 72;
    public static final int CONTAINER_SLOTS = 9;
    public static final int OPEN_ENCODER_BTN = 191;
    private static final int LINK_REFRESH = 40;

    public final CustomInventoryHandler containerSlots = new CustomInventoryHandler(CONTAINER_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markUpdated();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return AggregatedInventory.isStorageItem(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public final CustomInventoryHandler encoderBlanks = new CustomInventoryHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markUpdated();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return CraftingPattern.isBlank(stack);
        }
    };

    public final CustomInventoryHandler patterns = new CustomInventoryHandler(PATTERNS_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            markUpdated();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return CraftingPattern.isPattern(stack);
        }
    };

    public final LargeEnergyStorage energy = LargeEnergyStorage.create(ENERGY_CAPACITY, ENERGY_MAX_RECEIVE, 0, this::markUpdated);

    private final AggregatedItemHandler aggregatedItems = new AggregatedItemHandler(containerSlots, () -> !isRemoved());

    private CraftingJob job;
    public int craftOpIndex = 0;
    public int craftOpTotal = 0;

    private boolean wasChanged = false;

    private List<Link> linkCache = List.of();
    private int linkStamp = Integer.MIN_VALUE;
    private long linkTime = Long.MIN_VALUE;

    public EngineersCrafterBE(BlockPos pos, BlockState state) {
        super(Crafter.ENGINEERS_CRAFTING_TABLE_BE.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null) return;
        dropHandler(pos, containerSlots);
        dropHandler(pos, patterns);
        dropHandler(pos, encoderBlanks);
    }

    private void dropHandler(BlockPos pos, CustomInventoryHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            handler.voidSlot(i);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    public void markUpdated() {
        wasChanged = true;
        setChanged();
    }

    private void flushUpdate() {
        if (wasChanged && level != null && !level.isClientSide()) {
            wasChanged = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return aggregatedItems;
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energy;
    }

    public void tickServer() {
        if (level == null || level.isClientSide()) return;

        if (job != null && !job.isFinished()) {
            switch (job.phase()) {
                case COLLECTING, CRAFTING -> {
                    if (energy.getEnergyStoredL() >= CRAFT_FE) {
                        energy.drainEnergy(CRAFT_FE);
                        job.step(this);
                        markUpdated();
                    }
                }
                case DONE -> job.step(this);
            }
            craftOpIndex = job.opIndex();
            craftOpTotal = job.operationsSize();
            if (job.isFinished()) {
                job = null;
                craftOpIndex = 0;
                craftOpTotal = 0;
                markUpdated();
            }
        } else if (energy.getEnergyStoredL() >= PASSIVE_FE) {
            energy.drainEnergy(PASSIVE_FE);
            setChanged();
        }

        flushUpdate();
    }

    public void tickClient() {
    }

    public boolean hasActiveJob() {
        return job != null && !job.isFinished();
    }

    public boolean ownsProcessor(UUID jobId, BlockPos processor) {
        return hasActiveJob() && job.ownsProcessor(jobId, processor);
    }

    public boolean startJob(ItemStack target, int qty, Plan<CraftKey> plan) {
        if (hasActiveJob() || plan.operations().isEmpty()) return false;
        job = new CraftingJob(target, qty, plan);
        markUpdated();
        return true;
    }

    public void cancelJob() {
        if (hasActiveJob()) {
            job.cancel(this);
            if (job.isFinished()) {
                job = null;
                craftOpIndex = 0;
                craftOpTotal = 0;
            }
            markUpdated();
        }
    }

    public void openEncoder(ServerPlayer player) {
        BlockPos pos = getBlockPos();
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.nuclearcraft.engineers_encoder");
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                return new EngineersEncoderContainer(windowId, pos, inv);
            }
        }, buf -> buf.writeBlockPos(pos));
    }

    // --- processor discovery ---

    public List<Link> processorLinks() {
        if (!(level instanceof ServerLevel sl)) return List.of();
        int stamp = ProcessorIndex.topologyStamp(sl, worldPosition);
        long now = sl.getGameTime();
        if (stamp != linkStamp || now - linkTime >= LINK_REFRESH) {
            linkCache = ProcessorIndex.scan(sl, worldPosition);
            linkStamp = stamp;
            linkTime = now;
        }
        return linkCache;
    }

    private static boolean supported(List<Link> links, CraftingPattern p) {
        for (Link l : links) if (l.supports(p)) return true;
        return false;
    }

    // --- planning ---

    public AutoCraftSolver.Result<CraftKey> planCraft(ItemStack target, int qty) {
        return planCraft(CraftKey.of(target), qty);
    }

    public AutoCraftSolver.Result<CraftKey> planCraft(CraftKey target, int qty) {
        AggregatedInventory agg = new AggregatedInventory(containerSlots);
        Map<CraftKey, Integer> available = new LinkedHashMap<>();
        for (AggregatedInventory.Entry e : agg.entries()) {
            available.merge(CraftKey.of(e.key()), e.count(), Integer::sum);
        }
        for (AggregatedInventory.FluidEntry e : agg.fluidEntries()) {
            available.merge(e.key(), e.amount(), Integer::sum);
        }
        return new AutoCraftSolver<>(patternDefs()).solve(target, qty, available);
    }

    public List<PatternDef<CraftKey>> patternDefs() {
        List<Link> links = processorLinks();
        List<PatternDef<CraftKey>> defs = new ArrayList<>();
        for (int i = 0; i < patterns.getSlots(); i++) {
            CraftingPattern p = CraftingPattern.from(patterns.getStackInSlot(i));
            if (p == null) continue;
            PatternDef<CraftKey> def = toDef(p, links);
            if (def != null) defs.add(def);
        }
        return defs;
    }

    @Nullable
    private static PatternDef<CraftKey> toDef(CraftingPattern p, List<Link> links) {
        if (p.isProcessing() && !supported(links, p)) return null;
        Map<CraftKey, Integer> inputs = new LinkedHashMap<>();
        // processing recipes need one slot/tank per ingredient, so keep each ingredient's own amount per key
        Map<CraftKey, List<Integer>> parts = new LinkedHashMap<>();
        for (ItemStack in : p.inputs()) {
            if (in.isEmpty()) continue;
            CraftKey key = CraftKey.of(in);
            inputs.merge(key, in.getCount(), Integer::sum);
            if (p.isProcessing()) parts.computeIfAbsent(key, k -> new ArrayList<>()).add(in.getCount());
        }
        for (FluidStack in : p.fluidInputs()) {
            if (in.isEmpty()) continue;
            CraftKey key = CraftKey.of(in);
            inputs.merge(key, in.getAmount(), Integer::sum);
            if (p.isProcessing()) parts.computeIfAbsent(key, k -> new ArrayList<>()).add(in.getAmount());
        }
        if (inputs.isEmpty()) return null;

        Map<CraftKey, Integer> outputs = new LinkedHashMap<>();
        for (ItemStack item : p.itemOutputs()) outputs.merge(CraftKey.of(item), item.getCount(), Integer::sum);
        for (FluidStack fluid : p.fluidOutputs()) outputs.merge(CraftKey.of(fluid), fluid.getAmount(), Integer::sum);
        if (outputs.isEmpty()) return null;
        if (p.isProcessing() && p.recipeId() == null) return null;
        return new PatternDef<>(outputs, inputs, p.processor(),
                p.recipeId() == null ? "" : p.recipeId().toString(), parts);
    }

    public String getName() {
        return "engineers_crafter";
    }

    private static String itemKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static String fluidKey(FluidStack stack) {
        return BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
    }

    public int getInventorySlots() {
        return aggregatedItems.size();
    }

    @Nullable
    public Object getSlotData(int id) {
        if (id < 0 || id >= aggregatedItems.size()) return null;
        ItemStack s = aggregatedItems.getResource(id).toStack(aggregatedItems.getAmountAsInt(id));
        if (s.isEmpty()) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("item", itemKey(s));
        m.put("qty", s.getCount());
        return m;
    }

    public Object[] getFluidsInfo() {
        List<Object> out = new ArrayList<>();
        for (AggregatedInventory.FluidEntry e : new AggregatedInventory(containerSlots).fluidEntries()) {
            Map<String, Object> m = new HashMap<>();
            m.put("fluid", fluidKey(e.stack()));
            m.put("amount", e.amount());
            out.add(m);
        }
        return out.toArray();
    }

    public Object[] getPatternsInfo() {
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < patterns.getSlots(); i++) {
            CraftingPattern p = CraftingPattern.from(patterns.getStackInSlot(i));
            if (p == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", i);
            m.put("processor", p.processor());
            Map<String, Integer> agg = new LinkedHashMap<>();
            for (ItemStack in : p.inputs()) {
                if (in.isEmpty()) continue;
                agg.merge(itemKey(in), in.getCount(), Integer::sum);
            }
            List<Object> inputs = new ArrayList<>();
            for (Map.Entry<String, Integer> e : agg.entrySet()) {
                Map<String, Object> im = new HashMap<>();
                im.put("item", e.getKey());
                im.put("qty", e.getValue());
                inputs.add(im);
            }
            m.put("input", inputs.toArray());
            List<Object> fluidInputs = new ArrayList<>();
            for (FluidStack f : p.fluidInputs()) {
                Map<String, Object> fm = new HashMap<>();
                fm.put("fluid", fluidKey(f));
                fm.put("amount", f.getAmount());
                fluidInputs.add(fm);
            }
            m.put("fluidInput", fluidInputs.toArray());
            ItemStack primary = p.output();
            m.put("output", primary.isEmpty() ? "" : itemKey(primary));
            m.put("outputQty", primary.getCount());
            List<Object> outputs = new ArrayList<>();
            for (ItemStack s : p.itemOutputs()) {
                Map<String, Object> om = new HashMap<>();
                om.put("item", itemKey(s));
                om.put("qty", s.getCount());
                outputs.add(om);
            }
            m.put("outputs", outputs.toArray());
            List<Object> fluidOutputs = new ArrayList<>();
            for (FluidStack f : p.fluidOutputs()) {
                Map<String, Object> fm = new HashMap<>();
                fm.put("fluid", fluidKey(f));
                fm.put("amount", f.getAmount());
                fluidOutputs.add(fm);
            }
            m.put("fluidOutput", fluidOutputs.toArray());
            out.add(m);
        }
        return out.toArray();
    }

    public boolean startCraft(int patternId, int qty) {
        if (level == null || level.isClientSide()) return false;
        if (qty <= 0 || hasActiveJob()) return false;
        if (patternId < 0 || patternId >= patterns.getSlots()) return false;
        CraftingPattern p = CraftingPattern.from(patterns.getStackInSlot(patternId));
        if (p == null) return false;
        ItemStack target = p.output().copy();
        if (target.isEmpty()) return false;
        AutoCraftSolver.Result<CraftKey> res = planCraft(target, qty);
        if (!res.feasible()) return false;
        return startJob(target, qty, res.plan());
    }

    @Override
    protected final void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        writeContents(output);
        if (job != null && !job.isFinished()) {
            TagDataIO.write(output.child("Job"), job::save);
        }
    }

    private void writeContents(ValueOutput output) {
        containerSlots.serialize(output.child("ContainerSlots"));
        patterns.serialize(output.child("Patterns"));
        encoderBlanks.serialize(output.child("EncoderBlanks"));
        energy.serialize(output.child("Energy"));
    }

    @Override
    protected final void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        containerSlots.deserialize(input.childOrEmpty("ContainerSlots"));
        patterns.deserialize(input.childOrEmpty("Patterns"));
        encoderBlanks.deserialize(input.childOrEmpty("EncoderBlanks"));
        energy.deserialize(input.childOrEmpty("Energy"));
        job = input.child("Job").flatMap(child -> TagDataIO.read(child, CraftingJob::load)).orElse(null);
        craftOpIndex = input.getIntOr("CraftOpIndex", job == null ? 0 : job.opIndex());
        craftOpTotal = input.getIntOr("CraftOpTotal", job == null ? 0 : job.operationsSize());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (var problems = new ProblemReporter.ScopedCollector(problemPath(), igentuman.nc.NuclearCraft.LOGGER)) {
            var output = TagValueOutput.createWithContext(problems, registries);
            writeContents(output);
            output.putInt("CraftOpIndex", job != null && !job.isFinished() ? job.opIndex() : 0);
            output.putInt("CraftOpTotal", job != null && !job.isFinished() ? job.operationsSize() : 0);
            return output.buildResult();
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
