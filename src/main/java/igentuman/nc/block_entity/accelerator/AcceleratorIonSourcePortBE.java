package igentuman.nc.block_entity.accelerator;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class AcceleratorIonSourcePortBE extends AcceleratorPortBE {

    public AcceleratorIonSourcePortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Nullable
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        LinearAcceleratorControllerBE controller = sourceController();
        IItemHandler delegate = controller == null ? null : controller.getItemHandler(null);
        return delegate == null ? null : new InsertOnlyItemHandler(delegate);
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        LinearAcceleratorControllerBE controller = sourceController();
        FluidCapabilityHandler delegate = controller == null ? null : controller.contentHandler.getFluidHandler();
        return delegate == null ? null : new FillOnlyFluidHandler(delegate);
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return null;
    }

    @Nullable
    private LinearAcceleratorControllerBE sourceController() {
        var controller = controller();
        return controller instanceof LinearAcceleratorControllerBE linear && linear.ownsIonSourcePort(worldPosition)
                ? linear : null;
    }

    private record InsertOnlyItemHandler(IItemHandler delegate) implements IItemHandler {

        @Override public int getSlots() { return delegate.getSlots(); }

        @Override public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }

        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return delegate.insertItem(slot, stack, simulate);
        }

        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

        @Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }

        @Override public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }
    }

    private record FillOnlyFluidHandler(FluidCapabilityHandler delegate) implements IFluidHandler {

        @Override public int getTanks() { return 1; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? delegate.getFluidInTank(0) : FluidStack.EMPTY;
        }

        @Override public int getTankCapacity(int tank) { return tank == 0 ? delegate.getTankCapacity(0) : 0; }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && delegate.isFluidValid(0, stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            return delegate.fillTank(0, resource, action);
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }

        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }
}
