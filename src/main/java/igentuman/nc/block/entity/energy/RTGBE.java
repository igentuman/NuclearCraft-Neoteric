package igentuman.nc.block.entity.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.content.energy.RTGs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RTGBE extends NCEnergy {

    public RTGBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }

    @Override
    public String getName() {
        return getBlockState().getBlock().asItem().toString();
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        super.tickServer();
        energyStorage.addEnergy(getEnergyTransferPerTick());
        sendOutPower();
        if(getLevel().getGameTime() % 40 == 0) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (double) RTGs.all().get(getName()).config().getRadiation() / 500000000, worldPosition);
        }
    }

    public long getInputEnergyTier() {
        return RTGs.all().get(getName()).getEnergyTier().ordinal();
    }

    public long getOutputEnergyTier() {
        return RTGs.all().get(getName()).getEnergyTier().ordinal();
    }

    @Override
    protected int getEnergyMaxStorage() {
        return RTGs.all().get(getName()).config().getActualGeneration()*32;
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return RTGs.all().get(getName()).config().getActualGeneration();
    }
}
