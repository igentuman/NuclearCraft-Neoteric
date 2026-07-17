package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.target_chamber.DetectorBlock;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.PortMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.PARTICLE_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.*;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TargetChamberMultiblock extends ParticleChamberMultiblock {

    @Override
    public int maxHeight() { return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get(); }

    @Override
    public int maxWidth() { return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get(); }

    @Override
    public int maxDepth() { return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get(); }

    @Override
    public int minHeight() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minWidth() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minDepth() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    public TargetChamberMultiblock(TargetChamberControllerBE controllerBE) {
        super(
                getBlocksByTagKey(TARGET_CHAMBER_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(TARGET_CHAMBER_INNER_BLOCKS.location().toString()),
                new TargetChamberController(controllerBE)
        );
        id = "target_chamber_" + controllerBE.getBlockPos().toShortString();
        controllerBe = controllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public void validate() {
        validDetectors.clear();
        allDetectors.clear();
        super.validate();
        if (validationResult.isValid) {
            debugLog("Target chamber validation OK. detectors=" + validDetectors.size() + "/" + allDetectors.size());
        }
    }

    @Override
    protected TargetChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (TargetChamberControllerBE) controllerBe;
    }


    @Override
    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }


}
