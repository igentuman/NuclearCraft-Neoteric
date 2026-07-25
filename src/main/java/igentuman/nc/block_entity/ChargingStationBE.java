package igentuman.nc.block_entity;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.item.Q36Item;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class ChargingStationBE extends UniversalProcessorBE {

    public static final int CHARGE_PER_TICK = 500_000;
    public static final int FLUID_PER_TICK = 100;
    public static final int Q36_QC_PER_FLUID = 50;
    public static final int Q36_FLUID_PER_TICK = 20;

    public ChargingStationBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        if (level == null || level.isClientSide) return;
        charge();
        super.serverTick();
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
            if (tanks != null) {
                int current = Q36Item.getCharge(stack);
                int space = Q36Item.MAX_CHARGE - current;
                if (space > 0) {
                    int maxMb = Math.min(Q36_FLUID_PER_TICK, space / Q36_QC_PER_FLUID);
                    if (maxMb > 0) {
                        FluidStack drained = tanks.drainTank(0, maxMb, IFluidHandler.FluidAction.EXECUTE);
                        if (!drained.isEmpty()) {
                            Q36Item.setCharge(stack, current + drained.getAmount() * Q36_QC_PER_FLUID);
                            items.setStackInSlot(0, stack);
                            setChanged();
                        }
                    }
                }
            }
            return;
        }

        IFluidHandlerItem itemFluid = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemFluid != null && tanks != null) {
            FluidStack drained = tanks.drainTank(0, FLUID_PER_TICK, IFluidHandler.FluidAction.SIMULATE);
            if (!drained.isEmpty()) {
                int accepted = itemFluid.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) {
                    FluidStack actual = tanks.drainTank(0, accepted, IFluidHandler.FluidAction.EXECUTE);
                    if (!actual.isEmpty()) {
                        itemFluid.fill(actual, IFluidHandler.FluidAction.EXECUTE);
                        items.setStackInSlot(0, itemFluid.getContainer());
                        setChanged();
                    }
                }
            }
        }

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
}
