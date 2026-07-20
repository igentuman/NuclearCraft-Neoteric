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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side != null && getSideMode(side) == SideMode.DISABLED) return null;
        return fluidTank;
    }

    @Override
    protected boolean transfer() {
        if (level == null) return false;
        boolean changed = false;
        for (Direction direction : Direction.values()) {
            SideMode mode = getSideMode(direction);
            if (mode == SideMode.DEFAULT || mode == SideMode.DISABLED) continue;
            IFluidHandler neighbour = level.getCapability(
                    Capabilities.FluidHandler.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;

            if (mode == SideMode.OUT) {
                FluidStack drained = fluidTank.drainTank(0, getCapacity(), IFluidHandler.FluidAction.SIMULATE);
                if (drained.isEmpty()) continue;
                int filled = neighbour.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    fluidTank.drainTank(0, filled, IFluidHandler.FluidAction.EXECUTE);
                    changed = true;
                }
            } else if (mode == SideMode.IN) {
                FluidStack available = neighbour.drain(getCapacity(), IFluidHandler.FluidAction.SIMULATE);
                if (available.isEmpty()) continue;
                int accepted = fluidTank.fillTank(0, available, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) {
                    FluidStack toMove = neighbour.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                    if (!toMove.isEmpty()) {
                        fluidTank.fillTank(0, toMove, IFluidHandler.FluidAction.EXECUTE);
                        changed = true;
                    }
                }
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("FluidTank", fluidTank.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("FluidTank")) {
            fluidTank.deserializeNBT(registries, tag.getCompound("FluidTank"));
        }
    }
}
