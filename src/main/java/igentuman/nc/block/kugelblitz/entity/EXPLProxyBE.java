package igentuman.nc.block.kugelblitz.entity;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_PROXY_BE;

public class EXPLProxyBE extends NuclearCraftBE {

    @NBTField
    public BlockPos corePos = BlockPos.ZERO;
    private EXPLBE core;

    public EXPLProxyBE(BlockPos pPos, BlockState pBlockState) {
        super(EXPL_PROXY_BE.get(), pPos, pBlockState);
    }

    public EXPLProxyBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        this(pPos, pBlockState);
    }

    public void setCore(EXPLBE core) {
        this.core = core;
        this.corePos = core.getBlockPos();
        setChanged();
    }

    public int getAnalogSignal() {
        return 0;
    }

    public BlockPos getCorePos() {
        if (corePos == BlockPos.ZERO) {
            if (core != null) {
                corePos = core.getBlockPos();
            }
        }
        return corePos;
    }

    public EXPLBE getCoreBE() {
        if (core == null) {
            BlockEntity be = blockEntity(corePos);
            if (be instanceof EXPLBE) {
                core = (EXPLBE) be;
            }
        }
        return core;
    }

    public void destroyCore() {
        if(getCoreBE() instanceof EXPLBE) {
            BlockState st = level.getBlockState(getCoreBE().getBlockPos());
            if(st.equals(Blocks.AIR.defaultBlockState())) return;
            ItemStack core = new ItemStack(st.getBlock().asItem());
            level.removeBlock(getCoreBE().getBlockPos(), false);
            Block.popResource(level, getCoreBE().getBlockPos(), core);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(getCoreBE() == null) return super.getCapability(cap, side);
        return getCoreBE().getCapability(cap, side);
    }

    public void forceTickServer(EXPLBE core) {
        //Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        this.core = core;
        core.inputRedstoneSignal =  Math.max(getLevel().getBestNeighborSignal(getBlockPos()), core.inputRedstoneSignal);
    }
}
