package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.accelerator.LinearParticleProcessor;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.particle.ParticleSourceData;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class LinearAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static final int TANK_SOURCE = 0;
    public static final int TANK_COOLANT_IN = 1;
    public static final int TANK_COOLANT_OUT = 2;

    private static final long SOURCE_BATCH = 10_000;

    private boolean sourceValidatorsInstalled;

    public LinearAcceleratorControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
        installSourceValidators();
    }

    @Override
    protected void tickBeam(ServerLevel serverLevel) {
        StructureRecord record = scheduledStructure()
                .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED).orElse(null);
        if (record == null) return;

        int signal = updateControlSignal(serverLevel, record);

        tickThermal(serverLevel, record.aggregates(), TANK_COOLANT_IN, TANK_COOLANT_OUT);

        if (signal > 0 && runtimeState.overheatCooldown() <= 0) {
            injectSource(record);
            accelerateInput(record, signal);
            transferOutput(serverLevel, record);
        }
    }

    public boolean ownsIonSourcePort(BlockPos portPos) {
        return scheduledStructure().filter(record -> record.state() == StructureLifecycleState.FORMED)
                .map(record -> record.roles().getOrDefault(StructureRole.ION_SOURCE, List.of()).contains(portPos))
                .orElse(false);
    }

    private void injectSource(StructureRecord record) {
        if (record.roles().getOrDefault(StructureRole.ION_SOURCE, List.of()).isEmpty()) return;
        installSourceValidators();
        if (injectItemSource()) return;
        injectFluidSource();
    }

    private void installSourceValidators() {
        if (sourceValidatorsInstalled) return;
        ParticleSourceCatalog catalog = ParticleSourceCatalog.builtin();
        if (contentHandler.getItemHandler() != null) {
            contentHandler.getItemHandler().setSlotValidator(0, stack -> stack.getItem() instanceof ParticleSourceItem
                    && catalog.itemSources().containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                    && ParticleSourceItem.data(stack) != null);
        }
        if (contentHandler.getFluidHandler() != null) {
            contentHandler.getFluidHandler().getInternalHandler().setTankValidator(TANK_SOURCE,
                    stack -> catalog.fluidSources().containsKey(BuiltInRegistries.FLUID.getKey(stack.getFluid())));
        }
        sourceValidatorsInstalled = true;
    }

    private boolean injectItemSource() {
        var handler = contentHandler.getItemHandler();
        if (handler == null) return false;
        ItemStack sourceStack = handler.getStackInSlot(0);
        if (!(sourceStack.getItem() instanceof ParticleSourceItem)) return false;
        ParticleSourceData source = ParticleSourceItem.data(sourceStack);
        if (source == null || source.isDepleted()) return false;

        ParticleSourceData.EmitResult proposed = source.emit(SOURCE_BATCH);
        ParticleStack remainder = runtimeState.input().insert(0, proposed.stack(), ParticleAction.SIMULATE);
        long accepted = proposed.stack().amount() - remainder.amount();
        if (accepted <= 0) return false;
        ParticleStack candidate = accepted == proposed.stack().amount() ? proposed.stack()
                : new ParticleStack(proposed.stack().particleId(), accepted,
                proposed.stack().meanEnergyKeV(), proposed.stack().focus());
        ParticleStack commitRemainder = runtimeState.input().insert(0, candidate, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return false;

        ParticleSourceData.EmitResult committedEmission = source.emit(committed);
        ParticleSourceItem.setData(sourceStack, committedEmission.remaining());
        handler.setStackInSlot(0, sourceStack);
        return true;
    }

    private boolean injectFluidSource() {
        var handler = contentHandler.getFluidHandler();
        if (handler == null) return false;
        FluidStack stored = handler.getFluidInTank(TANK_SOURCE);
        if (stored.isEmpty()) return false;
        ParticleSourceData source = ParticleSourceCatalog.builtin().fluidSources()
                .get(BuiltInRegistries.FLUID.getKey(stored.getFluid()));
        if (source == null) return false;

        ParticleStack candidate = source.emit(source.capacity()).stack();
        ParticleStack remainder = runtimeState.input().insert(0, candidate, ParticleAction.SIMULATE);
        if (!remainder.isEmpty()) return false;
        FluidStack drained = handler.drainTank(TANK_SOURCE, 1, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() != 1 || drained.getFluid() != stored.getFluid()) return false;
        FluidStack committedDrain = handler.drainTank(TANK_SOURCE, 1, IFluidHandler.FluidAction.EXECUTE);
        if (committedDrain.getAmount() != 1 || committedDrain.getFluid() != stored.getFluid()) return false;
        ParticleStack commitRemainder = runtimeState.input().insert(0, candidate, ParticleAction.EXECUTE);
        if (!commitRemainder.isEmpty()) {
            handler.fillTank(TANK_SOURCE, committedDrain, IFluidHandler.FluidAction.EXECUTE);
            return false;
        }
        return true;
    }

    private void accelerateInput(StructureRecord record, int signal) {
        ParticleStack input = runtimeState.input().snapshot(0);
        if (input.isEmpty() || energyStorage == null) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(input.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        long required;
        ParticleStack accelerated;
        try {
            required = LinearParticleProcessor.requiredEnergy(record.aggregates(), config);
            accelerated = LinearParticleProcessor.accelerate(input, definition, record.aggregates(), config, signal);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return;
        }
        if (energyStorage.getEnergyStoredL() < required) return;
        ParticleStack remainder = runtimeState.pendingOutput().insert(0, accelerated, ParticleAction.SIMULATE);
        long accepted = accelerated.amount() - remainder.amount();
        if (accepted <= 0) return;
        ParticleStack acceptedStack = accepted == accelerated.amount() ? accelerated
                : new ParticleStack(accelerated.particleId(), accepted, accelerated.meanEnergyKeV(), accelerated.focus());
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

    private void transferOutput(ServerLevel serverLevel, StructureRecord record) {
        List<BlockPos> outputs = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
        if (outputs.isEmpty()) return;
        BlockPos outputPort = outputs.getFirst();
        ParticleStack pending = runtimeState.pendingOutput().snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(serverLevel, record, outputPort,
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }
}
