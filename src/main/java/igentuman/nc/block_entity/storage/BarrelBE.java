package igentuman.nc.block_entity.storage;

import igentuman.nc.content.storage.StorageDefs;
import igentuman.nc.handler.fluid.CustomFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class BarrelBE extends AbstractStorageBE {

    public final CustomFluidTank fluidTank;

    public BarrelBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
        this.fluidTank = CustomFluidTank.create(StorageDefs.barrelCapacityMb(name), this::setChanged);
    }

    public int getCapacity() {
        return fluidTank.getTankCapacity(0);
    }

    @Nullable
    @Override
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (isRemoved()) return null;
        if (side != null && getSideMode(side) == SideMode.DISABLED) return null;
        if (side == null) return fluidTank;
        // Recheck the mode even when automation retains this capability view.
        return new ResourceHandler<>() {
            private boolean enabled() { return !isRemoved() && getSideMode(side) != SideMode.DISABLED; }
            @Override public int size() { return fluidTank.size(); }
            @Override public FluidResource getResource(int index) { return fluidTank.getResource(index); }
            @Override public long getAmountAsLong(int index) { return fluidTank.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, FluidResource resource) {
                return enabled() ? fluidTank.getCapacityAsLong(index, resource) : 0;
            }
            @Override public boolean isValid(int index, FluidResource resource) {
                return enabled() && fluidTank.isValid(index, resource);
            }
            @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
                TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                return enabled() ? fluidTank.insert(index, resource, amount, transaction) : 0;
            }
            @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
                TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                return enabled() ? fluidTank.extract(index, resource, amount, transaction) : 0;
            }
        };
    }

    @Override
    protected boolean transfer() {
        if (level == null) return false;
        boolean changed = false;
        for (Direction direction : Direction.values()) {
            SideMode mode = getSideMode(direction);
            if (mode == SideMode.DEFAULT || mode == SideMode.DISABLED) continue;
            ResourceHandler<FluidResource> neighbour = level.getCapability(
                    Capabilities.Fluid.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;
            try (var transaction = Transaction.openRoot()) {
                int moved = mode == SideMode.OUT
                        ? ResourceHandlerUtil.move(fluidTank, neighbour, resource -> true, getCapacity(), transaction)
                        : ResourceHandlerUtil.move(neighbour, fluidTank, resource -> true, getCapacity(), transaction);
                transaction.commit();
                changed |= moved > 0;
            }
        }
        return changed;
    }

    @Override
    public int getComparatorSignal() {
        int cap = getCapacity();
        if (cap <= 0) return 0;
        return (int) ((fluidTank.getFluidInTank(0).getAmount() / (double) cap) * 15);
    }

    public boolean hasContent() {
        return !fluidTank.getFluidInTank(0).isEmpty();
    }

    @Override
    protected void saveTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveTagData(tag, registries);
        tag.put("FluidTank", fluidTank.serializeNBT(registries));
    }

    @Override
    protected void loadTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTagData(tag, registries);
        if (tag.contains("FluidTank")) {
            fluidTank.deserializeNBT(registries, tag.getCompoundOrEmpty("FluidTank"));
        }
    }
}
