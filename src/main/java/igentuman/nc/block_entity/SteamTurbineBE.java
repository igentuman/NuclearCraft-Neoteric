package igentuman.nc.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Converts steam recipes into FE and sends stored power to adjacent receivers. */
public class SteamTurbineBE extends UniversalProcessorBE {

    public SteamTurbineBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public boolean generatesRecipeEnergy() {
        return true;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (level == null || energyStorage == null || !energyStorage.canExtract()) return;
        int rate = energyStorage.getMaxEnergyStored();
        for (Direction direction : Direction.values()) {
            int available = energyStorage.extractEnergy(rate, true);
            if (available <= 0) return;
            IEnergyStorage neighbour = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;
            int accepted = neighbour.receiveEnergy(available, false);
            if (accepted > 0) energyStorage.extractEnergy(accepted, false);
        }
    }
}
