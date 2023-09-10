package igentuman.nc.block.entity.fusion;

import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;

public class FusionCoreProxyBE extends FusionBE {



    public FusionCoreProxyBE(BlockPos pPos, BlockState pBlockState) {
        super(FUSION_CORE_PROXY_BE.get(), pPos, pBlockState);
    }
    protected int timer = 20;
    protected void validateCore()
    {
        if(core != null)
        {
            core = (FusionCoreBE) level.getBlockEntity(core.getBlockPos());
            corePos = core.getBlockPos();
        } else {
            core = (FusionCoreBE) level.getBlockEntity(corePos);
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
        }
    }

    public void setCore(FusionCoreBE core) {
        this.core = core;
        corePos = core.getBlockPos();
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

    public FusionCoreBE getCoreBE() {
        return core;
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler.fluidCapability;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(controller() == null) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.of(() -> fluidHandler()).cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return controller().getEnergy().cast();
        }
        if(isCcLoaded()) {
            return controller().getPeripheral(cap, side);
        }
        return super.getCapability(cap, side);
    }
}
