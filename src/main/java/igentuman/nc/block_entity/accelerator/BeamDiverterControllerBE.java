package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.accelerator.BeamDiverterParticleProcessor;
import igentuman.nc.multiblock.accelerator.LinearParticleProcessor;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeamDiverterControllerBE extends AbstractAcceleratorControllerBE {

    @NBTField(syncToClient = true)
    public int selectedOutputChannel;
    @NBTField(syncToClient = true)
    public boolean incompatibleTurn;

    public BeamDiverterControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        if (mode == BeamPortMode.OUTPUT && channel != selectedOutputChannel) return null;
        return super.getParticleHandler(portPos, mode, channel);
    }

    @Override
    protected void tickBeam(ServerLevel serverLevel) {
        StructureRecord record = scheduledStructure()
                .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED).orElse(null);
        if (record == null) return;

        List<BlockPos> outputs = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
        if (outputs.isEmpty()) return;
        int selected = Math.clamp(selectedOutputChannel, 0, outputs.size() - 1);
        if (selected != selectedOutputChannel) {
            selectedOutputChannel = selected;
            setChanged();
        }
        BlockPos outputPort = outputs.get(selected);
        synchronizeOutputModes(serverLevel, outputs, selected);

        routeInput(serverLevel, record, outputPort);
        transferOutput(serverLevel, record, outputPort);
    }

    public boolean cycleSelectedOutputChannel() {
        StructureRecord record = scheduledStructure()
                .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED).orElse(null);
        if (record == null) return false;
        int outputCount = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of()).size();
        if (outputCount <= 0) return false;
        return setSelectedOutputChannel((selectedOutputChannel + 1) % outputCount);
    }

    public boolean setSelectedOutputChannel(int channel) {
        StructureRecord record = scheduledStructure()
                .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED).orElse(null);
        if (record == null) return false;
        int outputCount = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of()).size();
        if (channel < 0 || channel >= outputCount || channel == selectedOutputChannel) return false;
        if (!runtimeState.pendingOutput().snapshot(0).isEmpty()) return false;
        selectedOutputChannel = channel;
        if (level instanceof ServerLevel serverLevel) {
            synchronizeOutputModes(serverLevel,
                    record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of()), channel);
        }
        setChanged();
        return true;
    }

    private void routeInput(ServerLevel level, StructureRecord record, BlockPos outputPort) {
        ParticleStack input = runtimeState.input().snapshot(0);
        if (input.isEmpty() || energyStorage == null) {
            updateIncompatibleTurn(false);
            return;
        }
        List<BlockPos> inputs = record.roles().getOrDefault(StructureRole.BEAM_INPUT, List.of());
        if (inputs.size() != 1 || !level.hasChunkAt(inputs.getFirst()) || !level.hasChunkAt(outputPort)) return;
        Direction inputFacing = facing(level.getBlockState(inputs.getFirst()));
        Direction outputFacing = facing(level.getBlockState(outputPort));
        if (inputFacing == null || outputFacing == null) return;

        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(input.particleId());
        if (definition == null) return;
        boolean turn = BeamDiverterParticleProcessor.isTurn(inputFacing, outputFacing);
        boolean incompatible = turn && (definition.charge() == 0 || definition.massMeV() <= 0);
        updateIncompatibleTurn(incompatible);
        if (incompatible) return;

        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        long required;
        ParticleStack routed;
        try {
            required = BeamDiverterParticleProcessor.requiredEnergy(record.aggregates(), config);
            routed = BeamDiverterParticleProcessor.route(input, definition, record.aggregates(), config,
                    inputFacing, outputFacing);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return;
        }
        if (energyStorage.getEnergyStoredL() < required) return;

        ParticleStack remainder = runtimeState.pendingOutput().insert(0, routed, ParticleAction.SIMULATE);
        long accepted = routed.amount() - remainder.amount();
        if (accepted <= 0) return;
        ParticleStack acceptedStack = accepted == routed.amount() ? routed
                : new ParticleStack(routed.particleId(), accepted, routed.meanEnergyKeV(), routed.focus());
        ParticleStack commitRemainder = runtimeState.pendingOutput().insert(0, acceptedStack, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return;

        runtimeState.input().extract(0, committed, ParticleAction.EXECUTE);
        energyStorage.drainEnergy(required);
    }

    private void transferOutput(ServerLevel level, StructureRecord record, BlockPos outputPort) {
        ParticleStack pending = runtimeState.pendingOutput().snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(level, record, outputPort,
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }

    private void updateIncompatibleTurn(boolean value) {
        if (incompatibleTurn == value) return;
        incompatibleTurn = value;
        setChanged();
    }

    private void synchronizeOutputModes(ServerLevel level, List<BlockPos> outputs, int selected) {
        for (int channel = 0; channel < outputs.size(); channel++) {
            BlockPos pos = outputs.get(channel);
            if (!level.hasChunkAt(pos)) continue;
            if (level.getBlockEntity(pos) instanceof AcceleratorBeamPortBE port) {
                port.configureMode(channel == selected ? BeamPortMode.OUTPUT : BeamPortMode.DISABLED, channel);
            }
        }
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : null;
    }
}
