package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.util.BlockPosInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.HeatExchangerConfig.HEAT_EXCHANGER_CONFIG;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class HeatExchangerMultiblock extends AbstractMultiblock {

    public int heatExchangerCount = 0;
    public int radiatorCount = 0;

    @Override
    public int maxHeight() {
        return HEAT_EXCHANGER_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxWidth() {
        return HEAT_EXCHANGER_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxDepth() {
        return HEAT_EXCHANGER_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minHeight() {
        return HEAT_EXCHANGER_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minWidth() {
        return HEAT_EXCHANGER_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minDepth() {
        return HEAT_EXCHANGER_CONFIG.MIN_SIZE.get();
    }

    public HeatExchangerMultiblock(HeatExchangerControllerBE controllerBE) {
        super(
                getBlocksByTagKey(CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(INNER_BLOCKS.location().toString()),
                new HeatExchangerController(controllerBE)
        );
        id = "hx_" + controllerBE.getBlockPos().toShortString();
        MultiblockHandler.get(controllerBE.getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public HashSet<Block> validCornerBlocks() {
        return new HashSet<>(List.of(HX_BLOCKS.get("heat_exchanger_casing").get()));
    }

    @Override
    public void validate() {
        heatExchangerCount = 0;
        radiatorCount = 0;
        super.validate();

        if (!validationResult.isValid) {
            debugLog("Heat exchanger validation failed with result: " + validationResult);
            clearStats();
            return;
        }
        controllerBE().topRight = topRight;
        controllerBE().bottomLeft = bottomLeft;
        controllerBE().heatExchangers = heatExchangerCount;
        controllerBE().radiators = radiatorCount;
        controllerBE().refresh();
    }

    @Override
    protected void processOuterBlock(BlockPos pos) {
        super.processOuterBlock(pos);
        if (getBlockState(pos).getBlock() == HX_BLOCKS.get("heat_exchanger_radiator").get()) {
            radiatorCount++;
        }
    }

    @Override
    public HeatExchangerController controller() {
        return (HeatExchangerController) controller;
    }

    @Override
    protected HeatExchangerControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (HeatExchangerControllerBE) controllerBe;
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        BlockState bs = getBlockState(toCheck);
        if (bs.isAir()) return true;
        super.processInnerBlock(new BlockPosInstance(toCheck));
        if (bs.getBlock() == FissionReactorRegistration.FISSION_BLOCKS.get("heat_exchanger").get()) {
            heatExchangerCount++;
        }
        return true;
    }

    public void clearStats() {
        controller().clearStats();
    }

    @Override
    protected Direction getControllerDirection() {
        return controller().controllerBE().getFacing();
    }
}
