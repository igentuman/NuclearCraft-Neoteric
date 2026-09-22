package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.container.RingAcceleratorContainer;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.accelerator.LinearParticleProcessor;
import igentuman.nc.multiblock.accelerator.RingParticleProcessor;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RingAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static final int TANK_COOLANT_IN = 0;
    public static final int TANK_COOLANT_OUT = 1;

    @NBTField(syncToClient = true)
    public long maximumEnergyKeV;
    @NBTField(syncToClient = true)
    public boolean inputEnergyTooLow;
    @NBTField(syncToClient = true)
    public boolean inputEnergyTooHigh;
    @NBTField(syncToClient = true)
    public boolean incompatibleParticle;

    public RingAcceleratorControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RingAcceleratorContainer(containerId, playerInventory, this, containerData);
    }

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        if (mode == BeamPortMode.OUTPUT) {
            StructureRecord record = scheduledStructure()
                    .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED).orElse(null);
            List<BlockPos> outputs = record == null ? List.of()
                    : record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
            if (outputs.isEmpty() || channel != 0 || !outputs.getFirst().equals(portPos)) return null;
        }
        return super.getParticleHandler(portPos, mode, channel);
    }

    @Override
    protected void tickBeam(ServerLevel serverLevel) {
        StructureRecord record = scheduledStructure()
                .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED).orElse(null);
        if (record == null || !(record.footprint() instanceof SquareRingFootprint footprint)) return;

        int signal = updateControlSignal(serverLevel, record);
        tickThermal(serverLevel, record.aggregates(), TANK_COOLANT_IN, TANK_COOLANT_OUT);

        if (signal > 0 && runtimeState.overheatCooldown() <= 0) {
            accelerateInput(record, footprint, signal);
            transferOutput(serverLevel, record);
        }
    }

    private void accelerateInput(StructureRecord record, SquareRingFootprint footprint, int signal) {
        ParticleStack input = runtimeState.input().snapshot(0);
        if (input.isEmpty() || energyStorage == null) {
            updateDiagnostics(0, false, false, false);
            return;
        }
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(input.particleId());
        if (definition == null) {
            updateDiagnostics(0, false, false, true);
            return;
        }

        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        long maximum;
        try {
            maximum = RingParticleProcessor.maximumEnergyKeV(definition, record.aggregates(), footprint);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            updateDiagnostics(0, false, false, true);
            return;
        }
        boolean incompatible = definition.charge() == 0 || definition.massMeV() <= 0;
        boolean tooLow = input.meanEnergyKeV() < config.ringMinimumInputEnergyKeV();
        boolean tooHigh = input.meanEnergyKeV() > maximum;
        updateDiagnostics(maximum, tooLow, tooHigh, incompatible);
        if (incompatible || tooLow || tooHigh) return;

        long required;
        ParticleStack accelerated;
        try {
            required = RingParticleProcessor.requiredEnergy(record.aggregates(), config);
            accelerated = RingParticleProcessor.accelerate(input, definition, record.aggregates(), config,
                    footprint, signal);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return;
        }
        if (energyStorage.getEnergyStoredL() < required) return;

        ParticleStack remainder = runtimeState.pendingOutput().insert(0, accelerated, ParticleAction.SIMULATE);
        long accepted = accelerated.amount() - remainder.amount();
        if (accepted <= 0) return;
        ParticleStack acceptedStack = accepted == accelerated.amount() ? accelerated
                : new ParticleStack(accelerated.particleId(), accepted,
                accelerated.meanEnergyKeV(), accelerated.focus());
        ParticleStack commitRemainder = runtimeState.pendingOutput().insert(0, acceptedStack, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return;

        runtimeState.input().extract(0, committed, ParticleAction.EXECUTE);
        energyStorage.drainEnergy(required);
        addProcessingHeat(record.aggregates());
        syncParticleDisplay(committed == acceptedStack.amount() ? acceptedStack
                : new ParticleStack(acceptedStack.particleId(), committed,
                acceptedStack.meanEnergyKeV(), acceptedStack.focus()));
    }

    private void transferOutput(ServerLevel level, StructureRecord record) {
        List<BlockPos> outputs = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
        if (outputs.isEmpty()) return;
        ParticleStack pending = runtimeState.pendingOutput().snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(level, record, outputs.getFirst(),
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }

    private void updateDiagnostics(long maximum, boolean tooLow, boolean tooHigh, boolean incompatible) {
        if (maximumEnergyKeV == maximum && inputEnergyTooLow == tooLow && inputEnergyTooHigh == tooHigh
                && incompatibleParticle == incompatible) return;
        maximumEnergyKeV = maximum;
        inputEnergyTooLow = tooLow;
        inputEnergyTooHigh = tooHigh;
        incompatibleParticle = incompatible;
        setChanged();
    }
}
