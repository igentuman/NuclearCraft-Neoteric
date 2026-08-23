package igentuman.nc.block_entity;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.item.HEVItem;
import igentuman.nc.item.Q36Item;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public class ChargingStationBE extends GlobalBlockEntity implements MenuProvider {

    public static final int CHARGE_PER_TICK = 500_000;
    public static final int FLUID_PER_TICK = 100;
    public static final int Q36_QC_PER_FLUID = 50;
    public static final int Q36_FLUID_PER_TICK = 20;

    @NBTField(syncToClient = true)
    public int redstoneMode = 0;

    private boolean validatorsInstalled = false;

    public ChargingStationBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public void toggleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % 2;
        setChanged();
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        installSlotValidators();
        if (redstoneMode == 1 && level != null && !level.hasNeighborSignal(worldPosition)) return;
        charge();
        contentHandler.tick();
        if (wasChanged) {
            setChanged();
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            wasChanged = false;
        }
    }

    @Override
    public void clientTick() {
        installSlotValidators();
    }

    private void installSlotValidators() {
        if (validatorsInstalled || level == null) return;
        if (contentHandler.hasItemCapability()) {
            ItemCapabilityHandler handler = contentHandler.getItemHandler();
            handler.setSlotValidator(0, this::isValidInput);
            handler.setSideInsertLocked(false);
        }
        validatorsInstalled = true;
    }

    private boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof Q36Item) return true;
        if (stack.getItem() instanceof HEVItem) return true;
        if (stack.getCapability(Capabilities.EnergyStorage.ITEM) != null) return true;
        IFluidHandlerItem fluidCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidCap != null) {
            net.minecraft.world.level.material.Fluid quantite = ModEntries.fluidOf("quantite_energy");
            if (quantite != null) {
                FluidStack probe = new FluidStack(quantite, 1);
                return fluidCap.fill(probe, IFluidHandler.FluidAction.SIMULATE) > 0;
            }
        }
        return false;
    }

    private void charge() {
        ItemCapabilityHandler items = contentHandler.getItemHandler();
        if (items == null) return;
        ItemStack stack = items.getStackInSlot(0);
        if (stack.isEmpty()) return;

        FluidCapabilityHandler tanks = contentHandler.getFluidHandler();
        boolean hasFluid = tanks != null && tanks.getFluidInTank(0).getAmount() > 0;
        boolean hasEnergy = energyStorage != null && energyStorage.getEnergyStored() > 0;
        if (!hasFluid && !hasEnergy) return;

        if (stack.getItem() instanceof Q36Item) {
            chargeQ36(stack, tanks);
            return;
        }

        if (stack.getItem() instanceof HEVItem) {
            chargeHEV(stack, tanks);
            chargeEnergy(stack);
            return;
        }

        IFluidHandlerItem itemFluid = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemFluid != null && tanks != null) {
            fillFluidItem(stack, itemFluid, tanks, items);
            return;
        }

        chargeEnergy(stack);
    }

    private void chargeEnergy(ItemStack stack) {
        IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (itemEnergy != null && energyStorage != null) {
            int possible = Math.min(CHARGE_PER_TICK, energyStorage.getEnergyStored());
            if (possible <= 0) return;
            int received = itemEnergy.receiveEnergy(possible, false);
            if (received > 0) {
                energyStorage.extractEnergy(received, false);
                setChanged();
            }
        }
    }

    private void chargeQ36(ItemStack stack, @Nullable FluidCapabilityHandler tanks) {
        if (tanks == null) return;
        int current = Q36Item.getCharge(stack);
        int space = Q36Item.MAX_CHARGE - current;
        if (space <= 0) return;
        int fluidAvail = tanks.getFluidInTank(0).getAmount();
        if (fluidAvail <= 0) return;
        int fluidNeeded = (int) Math.ceil((double) space / Q36_QC_PER_FLUID);
        int fluidToUse = Math.min(Q36_FLUID_PER_TICK, Math.min(fluidAvail, fluidNeeded));
        if (fluidToUse <= 0) return;
        int qcGained = Math.min(space, fluidToUse * Q36_QC_PER_FLUID);
        FluidStack drained = tanks.drainTank(0, fluidToUse, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            Q36Item.setCharge(stack, current + qcGained);
            contentHandler.getItemHandler().setStackInSlot(0, stack);
            setChanged();
        }
    }

    private void chargeHEV(ItemStack stack, @Nullable FluidCapabilityHandler tanks) {
        if (tanks == null) return;
        int missing = HEVItem.MAX_QE_CHARGE - HEVItem.getQeCharge(stack);
        if (missing <= 0) return;
        int fluidAvail = tanks.getFluidInTank(0).getAmount();
        if (fluidAvail <= 0) return;
        int fluidNeeded = (int) Math.ceil((double) missing / Q36_QC_PER_FLUID);
        int fluidToUse = Math.min(Q36_FLUID_PER_TICK, Math.min(fluidAvail, fluidNeeded));
        if (fluidToUse <= 0) return;
        int qeGained = Math.min(missing, fluidToUse * Q36_QC_PER_FLUID);
        FluidStack drained = tanks.drainTank(0, fluidToUse, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            HEVItem.setQeCharge(stack, HEVItem.getQeCharge(stack) + qeGained);
            contentHandler.getItemHandler().setStackInSlot(0, stack);
            setChanged();
        }
    }

    private void fillFluidItem(ItemStack stack, IFluidHandlerItem itemFluid,
                                FluidCapabilityHandler tanks, ItemCapabilityHandler items) {
        FluidStack drained = tanks.drainTank(0, FLUID_PER_TICK, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) return;
        int accepted = itemFluid.fill(drained, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;
        FluidStack actual = tanks.drainTank(0, accepted, IFluidHandler.FluidAction.EXECUTE);
        if (actual.isEmpty()) return;
        itemFluid.fill(actual, IFluidHandler.FluidAction.EXECUTE);
        items.setStackInSlot(0, itemFluid.getContainer());
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new igentuman.nc.container.UniversalProcessorContainer(containerId, playerInventory, this, containerData);
    }
}
