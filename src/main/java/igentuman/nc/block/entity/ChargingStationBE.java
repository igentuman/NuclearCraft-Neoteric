package igentuman.nc.block.entity;

import igentuman.nc.item.Q36Item;
import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.content.materials.Materials.quantite_energy;
import static igentuman.nc.setup.registration.NCBlocks.CHARGING_STATION_BE;
import static igentuman.nc.setup.registration.NCItems.Q36;

public class ChargingStationBE extends BlockEntity {

    public static final int FLUID_CAPACITY = 32_000;
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int FLUID_PER_TICK = 100;
    public static final int CHARGE_PER_TICK = 500000;
    public static final int Q36_QC_PER_FLUID = 50;
    public static final int Q36_FLUID_PER_TICK = 20;

    public final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY, this::isValidFluid) {
        @Override
        protected void onContentsChanged() {
            ChargingStationBE.this.markUpdated();
        }
    };

    public final EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int r = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && r > 0) markUpdated();
            return r;
        }
    };

    public final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.getItem() instanceof Q36Item) return true;
            if (stack.getCapability(ForgeCapabilities.ENERGY).isPresent()) return true;
            return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                    .map(h -> {
                        NCFluids.FluidEntry entry = NCFluids.NC_GASES.get(quantite_energy);
                        if (entry == null) return false;
                        FluidStack probe = new FluidStack(entry.still().get(), 1);
                        return h.fill(probe, IFluidHandler.FluidAction.SIMULATE) > 0;
                    })
                    .orElse(false)
                    || stack.is(Q36.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            ChargingStationBE.this.markUpdated();
        }
    };

    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> items);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fluidTank);
    private final LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    public ChargingStationBE(BlockPos pos, BlockState state) {
        super(CHARGING_STATION_BE.get(), pos, state);
    }

    private boolean isValidFluid(FluidStack stack) {
        NCFluids.FluidEntry entry = NCFluids.NC_GASES.get(quantite_energy);
        return entry != null && stack.getFluid() == entry.still().get();
    }

    public void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void tickServer() {
        ItemStack stack = items.getStackInSlot(0);
        if (stack.isEmpty()) return;
        if (fluidTank.getFluidAmount() <= 0 && energy.getEnergyStored() <= 0) return;

        if (stack.getItem() instanceof Q36Item) {
            int missing = Q36Item.MAX_CHARGE - Q36Item.getCharge(stack);
            if (missing <= 0) return;
            int fluidAvail = fluidTank.getFluidAmount();
            if (fluidAvail <= 0) return;
            int fluidNeededForMissing = (int) Math.ceil((double) missing / Q36_QC_PER_FLUID);
            int fluidToUse = Math.min(Q36_FLUID_PER_TICK, Math.min(fluidAvail, fluidNeededForMissing));
            if (fluidToUse <= 0) return;
            int qcGained = Math.min(missing, fluidToUse * Q36_QC_PER_FLUID);
            fluidTank.drain(fluidToUse, IFluidHandler.FluidAction.EXECUTE);
            Q36Item.setCharge(stack, Q36Item.getCharge(stack) + qcGained);
            markUpdated();
            return;
        }

        var fluidCap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (fluidCap.isPresent()) {
            fluidCap.ifPresent(itemTank -> {
                FluidStack drained = fluidTank.drain(FLUID_PER_TICK, IFluidHandler.FluidAction.SIMULATE);
                if (drained.isEmpty()) return;
                int accepted = itemTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                if (accepted <= 0) return;
                FluidStack actual = fluidTank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                if (actual.isEmpty()) return;
                itemTank.fill(actual, IFluidHandler.FluidAction.EXECUTE);
                items.setStackInSlot(0, itemTank.getContainer());
                markUpdated();
            });
            return;
        }

        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            int possible = Math.min(CHARGE_PER_TICK, energy.getEnergyStored());
            if (possible <= 0) return;
            int received = storage.receiveEnergy(possible, false);
            if (received <= 0) return;
            energy.extractEnergy(received, false);
            markUpdated();
        });
    }

    public void tickClient() {
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
        energyCap.invalidate();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("Fluid")) fluidTank.readFromNBT(tag.getCompound("Fluid"));
        if (tag.contains("Energy")) energy.deserializeNBT(tag.get("Energy"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.writeToNBT(fluidTag);
        tag.put("Fluid", fluidTag);
        tag.put("Energy", energy.serializeNBT());
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
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
