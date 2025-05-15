package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.core.Direction;

import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_CASING_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class LinearAcceleratorMultiblock extends AbstractNCMultiblock {

    private LinearAcceleratorControllerBE controllerBe;

    public LinearAcceleratorMultiblock(LinearAcceleratorControllerBE controller) {
        super(
                getBlocksByTagKey(ACCELERATOR_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(ACCELERATOR_INNER_BLOCKS.location().toString()),
                new LinearAcceleratorController(controller)
        );
        id = "linear_accelerator_"+controller.getBlockPos().toShortString();
        controllerBe = controller;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public int maxHeight() {
        return 24;
    }
    @Override
    public int minHeight() {
        return 3;
    }
    @Override
    public int maxWidth() {
        return 24;
    }
    @Override
    public int minWidth() {
        return 3;
    }
    @Override
    public int maxDepth() {
        return 24;
    }
    @Override
    public int minDepth() {
        return 3;
    }

    private LinearAcceleratorControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = (LinearAcceleratorControllerBE) controller().controllerBE();
        }
        return controllerBe;
    }

    @Override
    protected Direction getFacing() {
        return controllerBE().getFacing();
    }

    @Override
    public void invalidateStats() {
        controller().clearStats();
    }
}
