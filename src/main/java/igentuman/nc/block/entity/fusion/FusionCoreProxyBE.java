package igentuman.nc.block.entity.fusion;

import net.minecraft.block.Blocks;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isMekanismLoadeed;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class FusionCoreProxyBE extends FusionBE implements ITickableTileEntity {

    public FusionCoreProxyBE(BlockPos pPos, BlockState pBlockState) {
        super(null);
    }

    public FusionCoreProxyBE() {
        super(null);
    }
    protected int timer = 20;
    protected void validateCore()
    {
        if(core != null)
        {
            if(!level.isLoaded(core.getBlockPos())) {
                return;
            }
            core = (FusionCoreBE<?>) level.getBlockEntity(core.getBlockPos());
            corePos = core.getBlockPos();
        } else {
            core = (FusionCoreBE<?>) level.getBlockEntity(corePos);
        }
    }

    public void tickServer()
    {
        timer--;
        if(timer <= 0)
        {
            timer = 20;
            validateCore();
        }
        if(!(core instanceof FusionCoreBE)) {
            level.removeBlock(worldPosition, false);
            return;
        }
        core.inputRedstoneSignal = getLevel().hasNeighborSignal(worldPosition) ? 1: core.inputRedstoneSignal;
    }

    public void setCore(FusionCoreBE<?> core) {
        FusionCoreBE<?> wasCore = this.core;
        this.core = core;
        corePos = core.getBlockPos();
        if(wasCore != core) {
            setChanged();
           // level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void destroyCore() {
        if(corePos != null) {
            BlockState st = level.getBlockState(corePos);
            if(st.equals(Blocks.AIR.defaultBlockState())) return;
            ItemStack core = new ItemStack(st.getBlock().asItem());
            level.removeBlock(corePos, false);
            Block.popResource(level, corePos, core);
        }
    }

    public FusionCoreBE<?> getCoreBE() {
        return core;
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    protected <T> LazyOptional<T> fluidHandler(@Nullable Direction side)
    {
        return controller().contentHandler.getFluidCapability(side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(controller() == null) return super.getCapability(cap, side);
        if(side == null || side.getAxis().isHorizontal()) {
            return LazyOptional.empty();
        }
        if (cap == ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
        }
        if (cap == FLUID_HANDLER_CAPABILITY) {
            return fluidHandler(side).cast();
        }
        if (cap == ENERGY) {
            return controller().getEnergy().cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }

        /*if(isMekanismLoadeed()) {
            if(cap == Capabilities.GAS_HANDLER_CAPABILITY) {
                if(controller().contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler.gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == Capabilities.SLURRY_HANDLER_CAPABILITY) {
                if(controller().contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler.getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }*/
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canInvalidateCache()
    {
        return false;
    }

    public void sendOutEnergy() {
        int required = getCoreBE().rfAmplifiersPower + getCoreBE().magnetsPower;

        for(Direction side: Arrays.asList(Direction.UP, Direction.DOWN)) {
            if(getCoreBE().energyStorage.getEnergyStored() > required) {
                TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(side));
                if(be instanceof TileEntity && !(be instanceof FusionBE)) {
                    IEnergyStorage r = be.getCapability(ENERGY, side.getOpposite()).orElse(null);
                    if(r == null) break;
                    if(r.canReceive()) {
                        int recieved = r.receiveEnergy(getCoreBE().energyStorage.getEnergyStored()-required, false);
                        getCoreBE().energyStorage.setEnergy(getCoreBE().energyStorage.getEnergyStored()-recieved);
                    }
                }
            }
        }
    }
}
