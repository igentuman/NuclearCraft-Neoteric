package igentuman.nc.block_entity.energy;

import igentuman.nc.content.energy.EnergyGenDefs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RtgBE extends AbstractEnergyBE {

    private final int generation;
    private final int radiation;
    private final String isotope;

    public RtgBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name, EnergyGenDefs.rtgGeneration(name) * 8);
        this.generation = EnergyGenDefs.rtgGeneration(name);
        this.radiation = EnergyGenDefs.rtgRadiation(name);
        this.isotope = EnergyGenDefs.rtgIsotope(name);
    }

    @Override
    protected int generate() {
        return generation;
    }

    @Override
    protected void emitRadiation() {
        if (isotope == null || radiation <= 0) return;
        if (!(level instanceof ServerLevel server)) return;
        if (server.getGameTime() % 200 != 0) return;
        double targetBq = Math.max(2_000_000.0, radiation * 4_000.0);
        assertRadiationSource(isotopeProfile(isotope, targetBq, server.getGameTime()));
    }
}
