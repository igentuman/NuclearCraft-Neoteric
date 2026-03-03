package igentuman.nc.block.entity.energy;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;



public class NCEnergy extends NuclearCraftBE {

    protected String name;
    public static String NAME;
    public final CustomEnergyStorage energyStorage;

    public NCEnergy(BlockPos pPos, BlockState pBlockState, String name) {
        super(NCEnergyBlocks.ENERGY_BE.get(name).get(), pPos, pBlockState);
        this.name = name;
        energyStorage = createEnergy();
        energyStorage.setOutputEnergyTier(getOutputEnergyTier())
                .setInputEnergyTier(getInputEnergyTier());
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    protected int getEnergyMaxStorage() {
        return 100;
    }

    protected int getEnergyTransferPerTick() {
        return Math.min(getEnergyMaxStorage(), energyStorage().getEnergyStored());
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(getEnergyMaxStorage(), getMaxTransfer(), getEnergyMaxStorage()) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public int getMaxTransfer() {
        return 0;
    }


    public String getName() {
        return name;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }
}
