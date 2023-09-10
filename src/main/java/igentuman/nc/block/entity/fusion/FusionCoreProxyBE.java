package igentuman.nc.block.entity.fusion;

import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY_BE;

public class FusionCoreProxyBE extends FusionBE {
    @NBTField
    BlockPos corePos;

    protected FusionCoreBE core;
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
}
