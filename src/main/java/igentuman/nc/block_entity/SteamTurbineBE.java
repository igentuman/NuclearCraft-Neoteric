package igentuman.nc.block_entity;

import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

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
            if (energyStorage.getAmountAsLong() <= 0) return;
            EnergyHandler neighbour = level.getCapability(
                    Capabilities.Energy.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;
            EnergyHandlerUtil.move(energyStorage, neighbour, rate, null);
        }
    }
}
