package igentuman.nc.block.entity.energy.solar;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.setup.energy.SolarPanels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AbstractSolarPanel extends NCEnergy {
    public AbstractSolarPanel(BlockPos pPos, BlockState pBlockState, String name) {
        super(pPos, pBlockState, name);
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

    @Override
    protected int getEnergyMaxStorage() {
        return SolarPanels.all().get(getName().split("/")[1]).getGeneration();
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(SolarPanels.all().get(getName().split("/")[1]).getGeneration(), energyStorage.getEnergyStored());
    }
}
