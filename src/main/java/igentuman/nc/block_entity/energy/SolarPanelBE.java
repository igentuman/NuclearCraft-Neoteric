package igentuman.nc.block_entity.energy;

import igentuman.nc.content.energy.EnergyGenDefs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SolarPanelBE extends AbstractEnergyBE {

    private final int generation;

    public SolarPanelBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name, EnergyGenDefs.solarGeneration(name) * 32);
        this.generation = EnergyGenDefs.solarGeneration(name);
    }

    @Override
    protected int generate() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        if (level.isDay() && level.canSeeSky(above) && !level.isRainingAt(above)) {
            return generation;
        }
        return 0;
    }
}
