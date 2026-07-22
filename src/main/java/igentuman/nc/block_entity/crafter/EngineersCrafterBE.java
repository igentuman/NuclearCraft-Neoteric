package igentuman.nc.block_entity.crafter;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.AggregatedItemHandler;
import igentuman.nc.handler.crafter.AutoCraftSolver;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.CraftingJob;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.item.ContainerBlockItem;
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
    public static final int PATTERNS_SIZE = 36;
    public static final int CONTAINER_SLOTS = 6;
    public static final int OPEN_ENCODER_BTN = 191;

    public final ItemStackHandler containerSlots = new ItemStackHandler(CONTAINER_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markUpdated();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ContainerBlockItem;
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

    public boolean startJob(ItemStack target, int qty, Plan<ItemKey> plan) {
        if (hasActiveJob() || plan.operations().isEmpty()) return false;
        job = new CraftingJob(target, qty, plan);
        markUpdated();
        return true;
    }

    public void cancelJob() {
        if (hasActiveJob()) {
            job.cancel(this);
            job = null;
            craftOpIndex = 0;
            craftOpTotal = 0;
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

    public AutoCraftSolver.Result<ItemKey> planCraft(ItemStack target, int qty) {
        AggregatedInventory agg = new AggregatedInventory(containerSlots);
        Map<ItemKey, Integer> available = new LinkedHashMap<>();
        for (AggregatedInventory.Entry e : agg.entries()) {
            available.merge(e.key(), e.count(), Integer::sum);
        }
        List<PatternDef<ItemKey>> defs = new ArrayList<>();
        for (int i = 0; i < patterns.getSlots(); i++) {
            CraftingPattern p = CraftingPattern.from(patterns.getStackInSlot(i));
            if (p == null) continue;
            ItemStack out = p.output();
            if (out.isEmpty()) continue;
            Map<ItemKey, Integer> inputs = new LinkedHashMap<>();
            for (ItemStack in : p.inputs()) {
                if (in.isEmpty()) continue;
                inputs.merge(ItemKey.of(in), 1, Integer::sum);
            }
            if (inputs.isEmpty()) continue;
            defs.add(new PatternDef<>(ItemKey.of(out), out.getCount(), inputs));
        }
        return new AutoCraftSolver<>(defs).solve(ItemKey.of(target), qty, available);
    }

    public String getName() {
        return "engineers_crafter";
    }

    private static String itemKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
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

    public Object[] getPatternsInfo() {
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < patterns.getSlots(); i++) {
            CraftingPattern p = CraftingPattern.from(patterns.getStackInSlot(i));
            if (p == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", i);
            Map<String, Integer> agg = new LinkedHashMap<>();
            for (ItemStack in : p.inputs()) {
                if (in.isEmpty()) continue;
                agg.merge(itemKey(in), 1, Integer::sum);
            }
            List<Object> inputs = new ArrayList<>();
            for (Map.Entry<String, Integer> e : agg.entrySet()) {
                Map<String, Object> im = new HashMap<>();
                im.put("item", e.getKey());
                im.put("qty", e.getValue());
                inputs.add(im);
            }
            m.put("input", inputs.toArray());
            m.put("output", itemKey(p.output()));
            m.put("outputQty", p.output().getCount());
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
        AutoCraftSolver.Result<ItemKey> res = planCraft(target, qty);
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
        if (tag.contains("ContainerSlots")) containerSlots.deserializeNBT(registries, tag.getCompound("ContainerSlots"));
        if (tag.contains("Patterns")) patterns.deserializeNBT(registries, tag.getCompound("Patterns"));
        if (tag.contains("EncoderBlanks")) encoderBlanks.deserializeNBT(registries, tag.getCompound("EncoderBlanks"));
        if (tag.contains("Energy")) energy.deserializeNBT(registries, tag.get("Energy"));
        job = tag.contains("Job") ? CraftingJob.load(tag.getCompound("Job"), registries) : null;
        craftOpIndex = tag.getInt("CraftOpIndex");
        craftOpTotal = tag.getInt("CraftOpTotal");
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
