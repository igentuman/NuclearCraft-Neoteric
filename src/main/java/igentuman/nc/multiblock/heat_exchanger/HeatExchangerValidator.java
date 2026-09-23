package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.api.multiblock.AbstractCuboidValidator;
import igentuman.nc.config.Multiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.rl;

public class HeatExchangerValidator extends AbstractCuboidValidator<HeatExchangerCache> {

    private Block controller;
    private Block casing;
    private Block radiator;
    private Block interior;
    private Block hotPort;
    private Block coldPort;
    private boolean resolved;

    @Override
    protected boolean resolveBlocks() {
        if (resolved) return true;
        controller = blockOf("heat_exchanger_controller");
        casing = blockOf("heat_exchanger_casing");
        radiator = blockOf("heat_exchanger_radiator");
        interior = blockOf("heat_exchanger");
        hotPort = blockOf("heat_exchanger_hot_coolant_port");
        coldPort = blockOf("heat_exchanger_cold_coolant_port");
        resolved = controller != null && casing != null && radiator != null && interior != null
                && hotPort != null && coldPort != null;
        return resolved;
    }

    @Override
    protected int minSize() {
        return Multiblocks.hxMinSize;
    }

    @Override
    protected int maxSize() {
        return Multiblocks.hxMaxSize;
    }

    @Override
    protected boolean isController(BlockState state) {
        return state.is(controller);
    }

    @Override
    protected boolean isShell(BlockState state) {
        return state.is(controller) || state.is(casing) || state.is(radiator)
                || state.is(hotPort) || state.is(coldPort);
    }

    @Override
    protected boolean acceptShell(HeatExchangerCache cache, BlockPos pos, BlockState state, boolean corner) {
        if (corner) {
            if (!state.is(casing)) {
                return fail("multiblock.heat_exchanger.wrong_corner", pos, rl("heat_exchanger_casing"), state);
            }
            return true;
        }
        if (!isShell(state)) {
            return fail("multiblock.validation.wrong_outer", pos, rl("multiblock_shell"), state);
        }
        if (state.is(radiator)) cache.countRadiator();
        return true;
    }

    @Override
    protected boolean acceptInterior(HeatExchangerCache cache, BlockPos pos, BlockState state) {
        if (state.is(interior)) {
            cache.countHeatExchanger();
            return true;
        }
        if (!state.isAir()) {
            return fail("multiblock.validation.wrong_inner", pos, rl("multiblock_interior"), state);
        }
        return true;
    }
}
