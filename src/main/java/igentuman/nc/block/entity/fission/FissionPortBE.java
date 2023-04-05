package igentuman.nc.block.entity.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class FissionPortBE extends FissionBE {
    public static String NAME = "fission_reactor_port";

    public FissionPortBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public void tickClient() {
    }

    public void tickServer() {

        if(controller == null) {
            return;
        }
        sendOutPower();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(controller == null) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return controller.handler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return  controller.energy.cast();
        }
        return super.getCapability(cap, side);
    }

    protected void sendOutPower() {
        AtomicInteger capacity = new AtomicInteger(controller.energyStorage.getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = getLevel().getBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), controller.energyStorage.getMaxEnergyStored()), false);
                                    capacity.addAndGet(-received);
                                    controller.energyStorage.consumeEnergy(received);
                                    setChanged();
                                    return capacity.get() > 0;
                                } else {
                                    return true;
                                }
                            }
                    ).orElse(true);
                    if (!doContinue) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

}
