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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EngineersCrafterBE extends BlockEntity {

    public static final int ENERGY_CAPACITY = 10_000;
    public static final int ENERGY_MAX_RECEIVE = 1_000;
    public static final int PASSIVE_FE = 100;
    public static final int CRAFT_FE = 200;
    public static final int PATTERNS_SIZE = 72;
    public static final int CONTAINER_SLOTS = 9;
    public static final int OPEN_ENCODER_BTN = 191;
    /** Ticks between processor rescans while the topology stamp is unchanged. */
    private static final int LINK_REFRESH = 40;

    public final ItemStackHandler containerSlots = new ItemStackHandler(CONTAINER_SLOTS) {
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

    public final ItemStackHandler encoderBlanks = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markUpdated();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return CraftingPattern.isBlank(stack);
        }
    };

    public final ItemStackHandler patterns = new ItemStackHandler(PATTERNS_SIZE) {
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

    private final AggregatedItemHandler aggregatedItems = new AggregatedItemHandler(containerSlots);

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

    public void markUpdated() {
        wasChanged = true;
        setChanged();
    }

    private void flushUpdate() {
        if (wasChanged && level != null && !level.isClientSide) {
            wasChanged = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return aggregatedItems;
    }

    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energy;
    }

    public void tickServer() {
        if (level == null || level.isClientSide) return;

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

        CraftKey output;
        int outputCount;
        Map<CraftKey, Integer> extra = new LinkedHashMap<>();
        List<ItemStack> items = p.itemOutputs();
        List<FluidStack> fluids = p.fluidOutputs();
        if (!items.isEmpty()) {
            output = CraftKey.of(items.getFirst());
            outputCount = items.getFirst().getCount();
            for (int i = 1; i < items.size(); i++) extra.merge(CraftKey.of(items.get(i)), items.get(i).getCount(), Integer::sum);
            for (FluidStack f : fluids) extra.merge(CraftKey.of(f), f.getAmount(), Integer::sum);
        } else if (!fluids.isEmpty()) {
            output = CraftKey.of(fluids.getFirst());
            outputCount = fluids.getFirst().getAmount();
            for (int i = 1; i < fluids.size(); i++) extra.merge(CraftKey.of(fluids.get(i)), fluids.get(i).getAmount(), Integer::sum);
        } else {
            return null;
        }
        if (outputCount <= 0) return null;
        return new PatternDef<>(output, outputCount, inputs, p.processor(), extra, parts);
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
        return aggregatedItems.getSlots();
    }

    @Nullable
    public Object getSlotData(int id) {
        if (id < 0 || id >= aggregatedItems.getSlots()) return null;
        ItemStack s = aggregatedItems.getStackInSlot(id);
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
        if (level == null || level.isClientSide) return false;
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("ContainerSlots", containerSlots.serializeNBT(registries));
        tag.put("Patterns", patterns.serializeNBT(registries));
        tag.put("EncoderBlanks", encoderBlanks.serializeNBT(registries));
        tag.put("Energy", energy.serializeNBT(registries));
        if (job != null && !job.isFinished()) {
            CompoundTag jobTag = new CompoundTag();
            job.save(jobTag, registries);
            tag.put("Job", jobTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ContainerSlots")) loadKeepingSize(containerSlots, tag.getCompound("ContainerSlots"), registries);
        if (tag.contains("Patterns")) loadKeepingSize(patterns, tag.getCompound("Patterns"), registries);
        if (tag.contains("EncoderBlanks")) encoderBlanks.deserializeNBT(registries, tag.getCompound("EncoderBlanks"));
        if (tag.contains("Energy")) energy.deserializeNBT(registries, tag.get("Energy"));
        job = tag.contains("Job") ? CraftingJob.load(tag.getCompound("Job"), registries) : null;
        craftOpIndex = tag.getInt("CraftOpIndex");
        craftOpTotal = tag.getInt("CraftOpTotal");
    }

    // deserializeNBT resizes the handler to the saved size; copy instead so older saves keep the current capacity
    private static void loadKeepingSize(ItemStackHandler target, CompoundTag tag, HolderLookup.Provider registries) {
        ItemStackHandler saved = new ItemStackHandler();
        saved.deserializeNBT(registries, tag);
        for (int i = 0; i < target.getSlots(); i++) {
            target.setStackInSlot(i, i < saved.getSlots() ? saved.getStackInSlot(i) : ItemStack.EMPTY);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("ContainerSlots", containerSlots.serializeNBT(registries));
        tag.put("Patterns", patterns.serializeNBT(registries));
        tag.put("EncoderBlanks", encoderBlanks.serializeNBT(registries));
        tag.put("Energy", energy.serializeNBT(registries));
        if (job != null && !job.isFinished() && job.operationsSize() > 0) {
            tag.putInt("CraftOpIndex", job.opIndex());
            tag.putInt("CraftOpTotal", job.operationsSize());
        } else {
            tag.putInt("CraftOpIndex", 0);
            tag.putInt("CraftOpTotal", 0);
        }
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
