package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EXPLProxyBE extends GlobalBlockEntity {

    @NBTField public BlockPos corePos = BlockPos.ZERO;

    private EXPLBE core;

    public EXPLProxyBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public void setCore(EXPLBE core) {
        this.core = core;
        this.corePos = core.getBlockPos();
        setChanged();
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public EXPLBE getCoreBE() {
        if (core == null && level != null && !corePos.equals(BlockPos.ZERO)
                && level.getBlockEntity(corePos) instanceof EXPLBE be) {
            core = be;
        }
        return core;
    }

    public void destroyCore() {
        EXPLBE c = getCoreBE();
        if (c == null || level == null) return;
        BlockPos cp = c.getBlockPos();
        BlockState st = level.getBlockState(cp);
        if (st.isAir()) return;
        level.removeBlock(cp, false);
        Block.popResource(level, cp, new ItemStack(st.getBlock().asItem()));
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        EXPLBE c = getCoreBE();
        return c != null ? c.getEnergyHandler(side) : null;
    }
}
