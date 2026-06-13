package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.COLLISION_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.*;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

/**
 * Collision chamber: asymmetric port layout — exactly 2 input ports and 4 output ports.
 */
public class CollisionChamberMultiblock extends ParticleChamberMultiblock {

    public static final int REQUIRED_INPUTS = 2;
    public static final int REQUIRED_OUTPUTS = 4;

    public final List<BlockPos> inputPorts = new ArrayList<>(REQUIRED_INPUTS);
    public final List<BlockPos> outputPorts = new ArrayList<>(REQUIRED_OUTPUTS);

    @Override public int maxHeight() { return COLLISION_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int maxWidth()  { return COLLISION_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int maxDepth()  { return COLLISION_CHAMBER_CONFIG.MAX_SIZE.get(); }
    @Override public int minHeight() { return COLLISION_CHAMBER_CONFIG.MIN_SIZE.get(); }
    @Override public int minWidth()  { return COLLISION_CHAMBER_CONFIG.MIN_SIZE.get(); }
    @Override public int minDepth()  { return COLLISION_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    protected boolean requireCubeShape() {
        // collision chambers allow rectangular ratios in addition to cubes
        return false;
    }

    public CollisionChamberMultiblock(CollisionChamberControllerBE controllerBE) {
        super(
                getBlocksByTagKey(TARGET_CHAMBER_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(TARGET_CHAMBER_INNER_BLOCKS.location().toString()),
                new ParticleChamberController(controllerBE)
        );
        id = "collision_chamber_" + controllerBE.getBlockPos().toShortString();
        controllerBe = controllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    @Override
    public void validateInner() {
        debugLog("Collision chamber inner validation");
        if (!outerValid) {
            clearStats();
            return;
        }
        inputPorts.clear();
        outputPorts.clear();
        for (long packedPos : allBlocks) {
            BlockPos pos = BlockPos.of(packedPos);
            BlockEntity be = getBlockEntity(pos);
            if (be instanceof TargetChamberBeamPortBE port) {
                beamPorts.put(packedPos, be);
                if (port.isInput()) {
                    inputPorts.add(pos);
                } else {
                    outputPorts.add(pos);
                }
            }
        }
        if (inputPorts.size() != REQUIRED_INPUTS) {
            validationResult = ValidationResult.NO_PORT;
            errorBlockPos = controllerBE().getBlockPos();
            debugLog("Collision chamber needs exactly " + REQUIRED_INPUTS + " input ports, found " + inputPorts.size());
            clearStats();
            return;
        }
        if (outputPorts.size() != REQUIRED_OUTPUTS) {
            validationResult = ValidationResult.NO_PORT;
            errorBlockPos = controllerBE().getBlockPos();
            debugLog("Collision chamber needs exactly " + REQUIRED_OUTPUTS + " output ports, found " + outputPorts.size());
            clearStats();
            return;
        }

        CollisionChamberControllerBE ctrl = (CollisionChamberControllerBE) controllerBE();
        ctrl.connectedPorts = inputPorts.size() + outputPorts.size();
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
        if (be instanceof CollisionChamberPortBE) {
            controllerBE().particleStorage.outputParticles.add(stack);
        }
    }

    @Override
    protected CollisionChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (CollisionChamberControllerBE) controllerBe;
    }

    @Override
    public void clearStats() {
        super.clearStats();
        inputPorts.clear();
        outputPorts.clear();
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().setChanged();
    }
}
