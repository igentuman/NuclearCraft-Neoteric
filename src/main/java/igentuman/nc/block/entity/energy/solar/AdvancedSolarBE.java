package igentuman.nc.block.entity.energy.solar;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.setup.energy.SolarPanels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedSolarBE extends NCEnergy {
    public static String NAME = "solar_panel/advanced";
    public AdvancedSolarBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected int getEnergyMaxStorage() {
        return SolarPanels.all().get(NAME.split("/")[1]).getGeneration();
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(SolarPanels.all().get(NAME.split("/")[1]).getGeneration(), energyStorage.getEnergyStored());
    }
    @Override
    public void tickServer() {
        super.tickServer();
        sendOutPower();
        if(energyStorage.getEnergyStored()>=energyStorage.getMaxEnergyStored()) {
            return;
        }
        if(getLevel().canSeeSky(getBlockPos().offset(0, 1, 0)) && !getLevel().isRainingAt(getBlockPos().offset(0, 1, 0)) && getLevel().isDay()) {
            energyStorage.addEnergy(energyStorage.getMaxEnergyStored());//panels do not have internal buffer
        }
    }
}
