package igentuman.nc.block.entity.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.radiation.data.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DecayGeneratorBE extends NCEnergy {
    public DecayGeneratorBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }

    @Override
    public String getName() {
        return getBlockState().getBlock().asItem().toString();
    }

    protected int radiationTimer = 40;
    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        energyStorage.setEnergy(getEnergyMaxStorage());
        sendOutPower();
        radiationTimer--;
        if(radiationTimer <= 0) {
            radiationTimer = 40;
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (double) RTGs.all().get(getName()).config().getRadiation() /500000000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
    }

    @Override
    protected int getEnergyMaxStorage() {
        return DECAY_GENERATOR.get();
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(RTGs.all().get(getName()).config().getGeneration(), energyStorage.getEnergyStored());
    }
}
