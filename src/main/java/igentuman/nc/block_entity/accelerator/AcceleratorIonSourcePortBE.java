package igentuman.nc.block_entity.accelerator;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class AcceleratorIonSourcePortBE extends AcceleratorPortBE {

    public AcceleratorIonSourcePortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Nullable
    @Override
    protected ResourceHandler<ItemResource> resolveItemHandler(@Nullable Direction side) {
        if (isRemoved()) return null;
        LinearAcceleratorControllerBE controller = sourceController();
        ResourceHandler<ItemResource> delegate = controller == null ? null : controller.getItemHandler(null);
        return delegate == null ? null : new InsertOnlyItemHandler(delegate);
    }

    @Nullable
    @Override
    protected ResourceHandler<FluidResource> resolveFluidHandler(@Nullable Direction side) {
        if (isRemoved()) return null;
        LinearAcceleratorControllerBE controller = sourceController();
        FluidCapabilityHandler delegate = controller == null ? null : controller.contentHandler.getFluidHandler();
        return delegate == null ? null : new FillOnlyFluidHandler(delegate);
    }

    @Nullable
    @Override
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return null;
    }

    @Nullable
    private LinearAcceleratorControllerBE sourceController() {
        var controller = controller();
        return controller instanceof LinearAcceleratorControllerBE linear && linear.ownsIonSourcePort(worldPosition)
                ? linear : null;
    }

    private record InsertOnlyItemHandler(ResourceHandler<ItemResource> delegate) implements ResourceHandler<ItemResource> {
        @Override public int size() { return delegate.size(); }
        @Override public ItemResource getResource(int index) { return delegate.getResource(index); }
        @Override public long getAmountAsLong(int index) { return delegate.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return delegate.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return delegate.isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return delegate.insert(index, resource, amount, transaction);
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            java.util.Objects.checkIndex(index, size());
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return 0;
        }
    }

    private record FillOnlyFluidHandler(FluidCapabilityHandler delegate) implements ResourceHandler<FluidResource> {
        private void checkIndex(int index) { java.util.Objects.checkIndex(index, 1); }
        @Override public int size() { return 1; }
        @Override public FluidResource getResource(int index) {
            checkIndex(index);
            return delegate.getInternalHandler().getResource(0);
        }
        @Override public long getAmountAsLong(int index) {
            checkIndex(index);
            return delegate.getInternalHandler().getAmountAsLong(0);
        }
        @Override public long getCapacityAsLong(int index, FluidResource resource) {
            checkIndex(index);
            return delegate.getInternalHandler().getCapacityAsLong(0, resource);
        }
        @Override public boolean isValid(int index, FluidResource resource) {
            checkIndex(index);
            return delegate.getInternalHandler().isValid(0, resource);
        }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            checkIndex(index);
            return delegate.insert(0, resource, amount, transaction);
        }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            checkIndex(index);
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return 0;
        }
    }
}
