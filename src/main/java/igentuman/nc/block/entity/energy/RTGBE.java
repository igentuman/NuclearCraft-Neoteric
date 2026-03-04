package igentuman.nc.block.entity.energy;

import igentuman.api.platform.NCNames;
import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.content.energy.RTGs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.currentTick;

public class RTGBE extends NCEnergy {

    public RTGBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    public static String getName(BlockState pBlockState) {
        return NCNames.of(pBlockState.getBlock().asItem());
    }

    @Override
    public String getName() {
        return NCNames.of(getBlockState().getBlock().asItem());
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        super.tickServer();
        energyStorage.addEnergy(getEnergyTransferPerTick());
        sendOutPower();
        if(currentTick % 40 == 0) {
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
