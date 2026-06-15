package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import static igentuman.nc.handler.config.AcceleratorConfig.DECAY_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class DecayChamberMultiblock extends ParticleChamberMultiblock {

    @Override public int maxHeight() { return DECAY_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int maxWidth()  { return DECAY_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int maxDepth()  { return DECAY_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int minHeight() { return DECAY_CHAMBER_CONFIG.MIN_SIZE.get(); }
    @Override public int minWidth()  { return DECAY_CHAMBER_CONFIG.MIN_SIZE.get(); }
    @Override public int minDepth()  { return DECAY_CHAMBER_CONFIG.MIN_SIZE.get(); }

    public DecayChamberMultiblock(DecayChamberControllerBE controllerBE) {
        super(
                getBlocksByTagKey(DECAY_CHAMBER_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(TARGET_CHAMBER_INNER_BLOCKS.location().toString()),
                new ParticleChamberController(controllerBE)
        );
        id = "decay_chamber_" + controllerBE.getBlockPos().toShortString();
        controllerBe = controllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    protected DecayChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (DecayChamberControllerBE) controllerBe;
    }
}
