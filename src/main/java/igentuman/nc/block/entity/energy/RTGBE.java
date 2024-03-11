package igentuman.nc.block.entity.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.content.energy.RTGs;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;

import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BE;

public class RTGBE extends NCEnergy {
    public RTGBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    public RTGBE(String name) {
        super(ENERGY_BE.get(name).get(), name);
    }

    public RTGBE(TileEntityType<? extends NCEnergy> tileEntityType, String code, BlockState state, IBlockReader world) {
        super(tileEntityType, code);
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }

    @Override
    public String getName() {
        if(!name.isEmpty()) {
            return name;
        }
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
//            RadiationManager.get(getLevel()).addRadiation(getLevel(), (double) RTGs.all().get(getName()).config().getRadiation() /500000000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
    }

    @Override
    protected int getEnergyMaxStorage() {
        return RTGs.all().get(getName()).config().getGeneration();
    }
    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(RTGs.all().get(getName()).config().getGeneration(), energyStorage.getEnergyStored());
    }


}
