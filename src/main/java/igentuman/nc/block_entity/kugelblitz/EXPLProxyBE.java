package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.DelegatingEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EmptyEnergyHandler;
import org.jetbrains.annotations.Nullable;

public class EXPLProxyBE extends GlobalBlockEntity {

    @NBTField public BlockPos corePos = BlockPos.ZERO;

    private EXPLBE core;

    public EXPLProxyBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        destroyCore();
    }

    public void setCore(EXPLBE core) {
        this.core = core;
        this.corePos = core.getBlockPos();
        setChanged();
        if (level != null) level.invalidateCapabilities(worldPosition);
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
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        if (resolveEnergyHandler(side) == null) return null;
        return new DelegatingEnergyHandler(() -> {
            EnergyHandler target = resolveEnergyHandler(side);
            return target != null ? target : EmptyEnergyHandler.INSTANCE;
        });
    }

    @Nullable
    private EnergyHandler resolveEnergyHandler(@Nullable Direction side) {
        if (isRemoved() || level == null || !level.hasChunkAt(corePos)) return null;
        if (!(level.getBlockEntity(corePos) instanceof EXPLBE current) || current.isRemoved()) return null;
        return current.getEnergyHandler(side);
    }
}
