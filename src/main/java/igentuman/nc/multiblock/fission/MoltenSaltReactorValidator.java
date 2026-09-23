package igentuman.nc.multiblock.fission;

import igentuman.nc.api.multiblock.AbstractCuboidValidator;
import igentuman.nc.config.Multiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.rl;

public class MoltenSaltReactorValidator extends AbstractCuboidValidator<MsrCache> {

    private Block controller;
    private Block fuelCell;

    @Override
    protected boolean resolveBlocks() {
        if (controller == null) controller = blockOf("msr_controller");
        if (fuelCell == null) fuelCell = blockOf("msr_fuel_cell");
        return controller != null && fuelCell != null;
    }

    @Override
    protected int minSize() {
        return Multiblocks.msrMinSize;
    }

    @Override
    protected int maxSize() {
        return Multiblocks.msrMaxSize;
    }

    @Override
    protected boolean isController(BlockState state) {
        return state.is(controller);
    }

    @Override
    protected boolean isShell(BlockState state) {
        return state.is(FissionTags.CASING);
    }

    @Override
    protected boolean acceptShell(MsrCache cache, BlockPos pos, BlockState state, boolean corner) {
        if (!isShell(state)) return fail("multiblock.validation.wrong_outer", pos, rl("multiblock_shell"), state);
        return true;
    }

    @Override
    protected boolean acceptInterior(MsrCache cache, BlockPos pos, BlockState state) {
        if (state.is(fuelCell)) {
            cache.addFuelCell(pos);
            return true;
        }
        if (!state.isAir()) return fail("multiblock.validation.wrong_inner", pos, rl("multiblock_interior"), state);
        return true;
    }
}
