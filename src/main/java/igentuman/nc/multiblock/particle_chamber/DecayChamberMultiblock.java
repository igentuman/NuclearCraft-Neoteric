package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.DECAY_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class DecayChamberMultiblock extends ParticleChamberMultiblock {

    public final List<BlockPos> outputPorts = new ArrayList<>();
    public BlockPos inputPort = null;

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
    public void validateInner() {
        debugLog("Decay chamber inner validation");
        if (!outerValid) {
            clearStats();
            return;
        }
        outputPorts.clear();
        inputPort = null;
        // tally chamber ports — must have at least 1 input + 1 output
        int inputs = 0;
        for (long packedPos : allBlocks) {
            BlockPos pos = BlockPos.of(packedPos);
            BlockEntity be = getBlockEntity(pos);
            if (be instanceof TargetChamberBeamPortBE port) {
                beamPorts.put(packedPos, be);
                if (inputPort == null && port.isInput()) {
                    inputPort = pos;
                    inputs++;
                } else if(port.isOutput()) {
                    outputPorts.add(pos);
                }
            }
        }
        if (inputs < 1) {
            validationResult = ValidationResult.NO_PORT;
            errorBlockPos = controllerBE().getBlockPos();
            clearStats();
            return;
        }
        if (outputPorts.isEmpty()) {
            validationResult = ValidationResult.NO_PORT;
            errorBlockPos = controllerBE().getBlockPos();
            clearStats();
            return;
        }
        DecayChamberControllerBE ctrl = (DecayChamberControllerBE) controllerBE();
        ctrl.connectedPorts = outputPorts.size() + 1;
        ctrl.efficiency = 1D;
        ctrl.height = height;
        ctrl.width = width;
        ctrl.depth = depth;
        validationResult = ValidationResult.VALID;
        innerValid = true;
    }

    @Override
    public void extractParticle(int outputIndex, ParticleStack stack) {
        if (outputPorts.isEmpty()) return;
        int idx = outputIndex % outputPorts.size();
        BlockPos pos = outputPorts.get(idx);
        BlockEntity be = beamPorts.get(pos.asLong());
        if (be instanceof DecayChamberPortBE port) {
            // store on controller particle output list; ports just provide a network-visible read-out
            controllerBE().particleStorage.outputParticles.add(stack);
        }
    }

    @Override
    protected DecayChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (DecayChamberControllerBE) controllerBe;
    }

    @Override
    public void clearStats() {
        super.clearStats();
        outputPorts.clear();
        inputPort = null;
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().setChanged();
    }
}
