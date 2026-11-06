package igentuman.nc.block.crafter.entity;

import igentuman.nc.compat.cc.EngineersCrafterPeripheral;
import igentuman.nc.compat.oc2.EngineersCrafterDevice;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.AggregatedItemHandler;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.AutoCraftSolver;
import igentuman.nc.handler.crafter.CraftKey;
import igentuman.nc.handler.crafter.CraftingJob;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.handler.crafter.ProcessorIndex.Link;
import igentuman.nc.handler.crafter.ProcessorIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.compat.oc2.EngineersCrafterDevice.DEVICE_CAPABILITY;
import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static igentuman.nc.util.TextUtils.__;

public class EngineersCrafterBE extends BlockEntity {

    public static final int ENERGY_CAPACITY = 10_000;
    public static final int ENERGY_MAX_RECEIVE = 1_000;

    public static final int PASSIVE_FE = 100;
    public static final int CRAFT_FE = 200;

    public static final int PATTERNS_SIZE = 72;
    public static final int CONTAINER_SLOTS = 9;
    public static final int OPEN_ENCODER_BTN = 191;

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

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            loadKeepingSize(this, nbt);
            onLoad();
        }
    };

    public class CrafterEnergy extends EnergyStorage {
        public CrafterEnergy() {
            super(ENERGY_CAPACITY, ENERGY_MAX_RECEIVE, 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int r = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && r > 0) markUpdated();
            return r;
        }

        public void consume(int amount) {
            this.energy = Math.max(0, this.energy - amount);
        }
    }

    public final CrafterEnergy energy = new CrafterEnergy();

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private final LazyOptional<IItemHandler> aggregatedItemCap =
            LazyOptional.of(() -> new AggregatedItemHandler(containerSlots));

    private LazyOptional<EngineersCrafterPeripheral> peripheralCap;

    private List<Link> linkCache = List.of();
    private int linkStamp = Integer.MIN_VALUE;
    private long linkTime = Long.MIN_VALUE;
    private static final int LINK_REFRESH = 40;
    private boolean wasChanged;
    private CraftingJob job;
    public int craftOpIndex = 0;
    public int craftOpTotal = 0;

    public EngineersCrafterBE(BlockPos pos, BlockState state) {
        super(ENGINEERS_CRAFTING_TABLE_BE.get(), pos, state);
    }

    public void markUpdated() {
        wasChanged = true;
        setChanged();
    }

    private void flushUpdate() {
        if (wasChanged && level != null && !level.isClientSide) {
            wasChanged = false;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void tickServer() {
        if (level == null || level.isClientSide) return;

        if (job != null && !job.isFinished()) {
            switch (job.phase()) {
                case COLLECTING, CRAFTING -> {
                    if (energy.getEnergyStored() >= CRAFT_FE) {
                        energy.consume(CRAFT_FE);
                        job.step(this);
                        markUpdated();
                    }
                }
                case DONE -> job.step(this); // output routing is free
            }
            if (job.isFinished()) {
                job = null;
                markUpdated();
            }
        } else if (energy.getEnergyStored() >= PASSIVE_FE) {
            energy.consume(PASSIVE_FE);
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

    public boolean ownsProcessor(UUID jobId, BlockPos processor) {
        return hasActiveJob() && job.ownsProcessor(jobId, processor);
    }

    public void cancelJob() {
        if (hasActiveJob()) {
            job.cancel(this);
            if (job.isFinished()) job = null;
            markUpdated();
        }
    }

    public void openEncoder(ServerPlayer player) {
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return __("container.nc.engineers_encoder");
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                return new EngineersEncoderContainer(windowId, getBlockPos(), inv);
            }
        };
        NetworkHooks.openScreen(player, provider, getBlockPos());
    }

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
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }

    private static String fluidKey(FluidStack stack) {
        return ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString();
    }

    private static void loadKeepingSize(ItemStackHandler target, CompoundTag tag) {
        ItemStackHandler saved = new ItemStackHandler();
        saved.deserializeNBT(tag);
        for (int i = 0; i < target.getSlots(); i++) {
            target.setStackInSlot(i, i < saved.getSlots() ? saved.getStackInSlot(i) : ItemStack.EMPTY);
        }
    }

    /** Number of flat slots across every inserted container item. */
    public int getInventorySlots() {
        return new AggregatedItemHandler(containerSlots).getSlots();
    }

    /** {@code {item="namespace:id", qty=n}} for the given aggregated slot, or null when empty/out of range. */
    public Object getSlotData(int id) {
        AggregatedItemHandler h = new AggregatedItemHandler(containerSlots);
        if (id < 0 || id >= h.getSlots()) return null;
        ItemStack s = h.getStackInSlot(id);
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

    /** One entry per encoded pattern: {@code {id, output, outputQty, input=[{item, qty}]}}. */
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

    public <T> LazyOptional<T> getPeripheral(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new EngineersCrafterPeripheral(this));
        }
        return peripheralCap.cast();
    }

    private <T> LazyOptional<T> getOCDevice(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.of(() -> EngineersCrafterDevice.createDevice(this)).cast();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return aggregatedItemCap.cast();
        if (isCcLoaded() && cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
            return getPeripheral(cap, side);
        }
        if (isOC2Loaded() && cap == DEVICE_CAPABILITY) {
            return getOCDevice(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        aggregatedItemCap.invalidate();
        if (peripheralCap != null) peripheralCap.invalidate();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Energy")) energy.deserializeNBT(tag.get("Energy"));
        if (tag.contains("Patterns")) patterns.deserializeNBT(tag.getCompound("Patterns"));
        if (tag.contains("EncoderBlanks")) encoderBlanks.deserializeNBT(tag.getCompound("EncoderBlanks"));
        if (tag.contains("Containers")) loadKeepingSize(containerSlots, tag.getCompound("Containers"));
        job = tag.contains("Job") ? CraftingJob.load(tag.getCompound("Job")) : null;
        craftOpIndex = tag.getInt("CraftOpIndex");
        craftOpTotal = tag.getInt("CraftOpTotal");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Energy", energy.serializeNBT());
        tag.put("Patterns", patterns.serializeNBT());
        tag.put("EncoderBlanks", encoderBlanks.serializeNBT());
        tag.put("Containers", containerSlots.serializeNBT());
        if (job != null && !job.isFinished()) {
            CompoundTag jobTag = new CompoundTag();
            job.save(jobTag);
            tag.put("Job", jobTag);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("Energy", energy.serializeNBT());
        tag.put("Patterns", patterns.serializeNBT());
        tag.put("EncoderBlanks", encoderBlanks.serializeNBT());
        tag.put("Containers", containerSlots.serializeNBT());
        if (job != null && !job.isFinished() && job.operationsSize() > 0) {
            tag.putInt("CraftOpIndex", job.opIndex());
            tag.putInt("CraftOpTotal", job.operationsSize());
        } else {
            tag.putInt("CraftOpIndex", 0);
            tag.putInt("CraftOpTotal", 0);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) handleUpdateTag(pkt.getTag());
    }
}
