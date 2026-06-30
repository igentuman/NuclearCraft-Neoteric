package igentuman.nc.block_entity;

import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class IrradiatorBE extends UniversalProcessorBE {

    @NBTField(syncToClient = true)
    public int irradiativeFlux = 0;

    @NBTField
    public double fuelMultiplier = 0.0;

    public IrradiatorBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
        assert energyStorage != null;
        energyStorage.setCapacity(0);
    }

    public double speedMultiplier() {
        return (double) irradiativeFlux / 10.0 * fuelMultiplier;
    }

    @Override
    public void serverTick() {
        int wasFlux = irradiativeFlux;
        double wasFuel = fuelMultiplier;
        irradiativeFlux = 0;
        fuelMultiplier = 0.0;
        if (level instanceof ServerLevel serverLevel) {
            BlockPos controllerPos = MultiblockHandler.getControllerForPos(serverLevel, worldPosition);
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof FissionReactorControllerBE controller) {
                irradiativeFlux = controller.getIrradiativeFlux();
                fuelMultiplier = controller.getFuelMultiplier();
            }
        }

        if (irradiativeFlux > 0 && fuelMultiplier > 0) {
            recipeInfo.multiplier = Math.max(1, (int) Math.ceil(speedMultiplier()));
            recipeInfo.energyPerTick = 0;
            contentHandler.tick();
            recipeInfo.tick();
        } else {
            contentHandler.tick();
        }

        if (wasFlux != irradiativeFlux || Double.compare(wasFuel, fuelMultiplier) != 0) {
            updatePoweredState();
            setChanged();
        }
    }
}
