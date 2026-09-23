package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.accelerator.BeamDiverterControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class BeamDiverterLogic
        extends AbstractAcceleratorLogic<BeamDiverterCache, BeamDiverterControllerBE> {

    @Override
    protected Class<BeamDiverterControllerBE> controllerClass() {
        return BeamDiverterControllerBE.class;
    }

    @Override
    protected void tickBeam(ServerLevel level, BlockPos controllerPos, BeamDiverterCache cache,
                            BeamDiverterControllerBE controller) {
        List<BlockPos> outputs = cache.beamOutputs;
        if (outputs.isEmpty()) return;
        int selected = controller.clampSelectedOutputChannel(outputs.size());
        BlockPos outputPort = outputs.get(selected);
        controller.synchronizeOutputModes(level, outputs, selected);

        routeInput(level, cache, controller, outputPort);
        transferOutput(level, controllerPos, outputPort);
    }

    private void routeInput(ServerLevel level, BeamDiverterCache cache, BeamDiverterControllerBE controller,
                            BlockPos outputPort) {
        ParticleStack stack = input.snapshot(0);
        if (stack.isEmpty() || controller.energyStorage == null) {
            controller.updateIncompatibleTurn(false);
            return;
        }
        List<BlockPos> inputs = cache.beamInputs;
        if (inputs.size() != 1 || !level.hasChunkAt(inputs.getFirst()) || !level.hasChunkAt(outputPort)) return;
        Direction inputFacing = facing(level.getBlockState(inputs.getFirst()));
        Direction outputFacing = facing(level.getBlockState(outputPort));
        if (inputFacing == null || outputFacing == null) return;

        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(stack.particleId());
        if (definition == null) return;
        boolean turn = BeamDiverterParticleProcessor.isTurn(inputFacing, outputFacing);
        boolean incompatible = turn && (definition.charge() == 0 || definition.massMeV() <= 0);
        controller.updateIncompatibleTurn(incompatible);
        if (incompatible) return;

        AcceleratorStats stats = cache.stats;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        long required;
        ParticleStack routed;
        try {
            required = BeamDiverterParticleProcessor.requiredEnergy(stats, config);
            routed = BeamDiverterParticleProcessor.route(stack, definition, stats, config, inputFacing, outputFacing);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return;
        }
        if (controller.energyStorage.getEnergyStoredL() < required) return;

        ParticleStack remainder = pendingOutput.insert(0, routed, ParticleAction.SIMULATE);
        long accepted = routed.amount() - remainder.amount();
        if (accepted <= 0) return;
        ParticleStack acceptedStack = accepted == routed.amount() ? routed
                : new ParticleStack(routed.particleId(), accepted, routed.meanEnergyKeV(), routed.focus());
        ParticleStack commitRemainder = pendingOutput.insert(0, acceptedStack, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return;

        input.extract(0, committed, ParticleAction.EXECUTE);
        controller.energyStorage.drainEnergy(required);
    }

    private void transferOutput(ServerLevel level, BlockPos controllerPos, BlockPos outputPort) {
        ParticleStack pending = pendingOutput.snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(level, controllerPos, outputPort,
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : null;
    }
}
