package igentuman.nc.multiblock.fission;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import net.minecraft.core.BlockPos;

public class FissionReactorController implements MultiblockController {

    protected final FissionControllerBE controllerBE;

    public FissionReactorController(FissionControllerBE fissionControllerBE) {
        controllerBE = fissionControllerBE;
    }

    @Override
    public FissionControllerBE controllerBE() {
        return controllerBE;
    }

    @Override
    public void clearStats() {
        controllerBE().moderatorAttachments = 0;
        controllerBE().moderatorsCount = 0;
        controllerBE().heatSinkCooling = 0;
        controllerBE().fuelCellsCount = 0;
        controllerBE().fuelCellMultiplier = 0;
        controllerBE().moderatorCellMultiplier = 0;
        controllerBE().irradiationConnections = 0;
    }

    @Override
    public void addErroredBlock(BlockPos relative) {
        controllerBE().errorBlockPos = relative;
    }
}
