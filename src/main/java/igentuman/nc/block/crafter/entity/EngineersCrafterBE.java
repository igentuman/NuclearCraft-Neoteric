package igentuman.nc.block.crafter.entity;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.AutoCraftSolver;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.crafter.AutoCraftSolver.Plan;
import igentuman.nc.handler.crafter.CraftingJob;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_BE;
import static igentuman.nc.util.TextUtils.__;

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

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            ListTag items = nbt.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTags = items.getCompound(i);
                int slot = itemTags.getInt("Slot");
                if (slot >= 0 && slot < getSlots()) {
                    setStackInSlot(slot, ItemStack.of(itemTags));
                }
            }
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

    private CraftingJob job;
    public int craftOpIndex = 0;
    public int craftOpTotal = 0;

    public EngineersCrafterBE(BlockPos pos, BlockState state) {
        super(ENGINEERS_CRAFTING_TABLE_BE.get(), pos, state);
    }

    public void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
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
            return;
        }

        if (energy.getEnergyStored() >= PASSIVE_FE) {
            energy.consume(PASSIVE_FE);
            setChanged();
        }
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

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Energy")) energy.deserializeNBT(tag.get("Energy"));
        if (tag.contains("Patterns")) patterns.deserializeNBT(tag.getCompound("Patterns"));
        if (tag.contains("EncoderBlanks")) encoderBlanks.deserializeNBT(tag.getCompound("EncoderBlanks"));
        if (tag.contains("Containers")) containerSlots.deserializeNBT(tag.getCompound("Containers"));
        if (tag.contains("Job")) job = CraftingJob.load(tag.getCompound("Job"));
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
