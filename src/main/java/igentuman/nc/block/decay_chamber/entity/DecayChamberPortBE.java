package igentuman.nc.block.decay_chamber.entity;

import igentuman.nc.block.entity.ParticleChamberPortBE;
import igentuman.nc.multiblock.particle_chamber.DecayChamberMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;

public class DecayChamberPortBE extends ParticleChamberPortBE<DecayChamberControllerBE, DecayChamberMultiblock> {

    public static final String NAME = "decay_chamber_port";

    public DecayChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    protected Class<DecayChamberControllerBE> controllerClass() {
        return DecayChamberControllerBE.class;
    }
}
