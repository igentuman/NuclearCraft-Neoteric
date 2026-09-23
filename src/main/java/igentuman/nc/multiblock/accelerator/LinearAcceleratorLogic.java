package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.particle.ParticleSourceData;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class LinearAcceleratorLogic
        extends AbstractAcceleratorLogic<LinearAcceleratorCache, LinearAcceleratorControllerBE> {

    private static final long SOURCE_BATCH = 10_000;

    @Override
    protected Class<LinearAcceleratorControllerBE> controllerClass() {
        return LinearAcceleratorControllerBE.class;
    }

    @Override
    protected void tickBeam(ServerLevel level, BlockPos controllerPos, LinearAcceleratorCache cache,
                            LinearAcceleratorControllerBE controller) {
        int signal = updateControlSignal(level, controllerPos, cache, controller);
        tickThermal(level, controller, cache.stats, LinearAcceleratorControllerBE.TANK_COOLANT_IN,
                LinearAcceleratorControllerBE.TANK_COOLANT_OUT);
        if (signal <= 0 || overheatCooldown > 0) return;
        injectSource(controller, cache);
        accelerate(controller, cache.stats, signal);
        transferOutput(level, controllerPos, cache);
    }

    private void injectSource(LinearAcceleratorControllerBE controller, LinearAcceleratorCache cache) {
        if (cache.ionSources.isEmpty()) return;
        controller.installSourceValidators();
        if (injectItemSource(controller)) return;
        injectFluidSource(controller);
    }

    private boolean injectItemSource(LinearAcceleratorControllerBE controller) {
        var handler = controller.contentHandler.getItemHandler();
        if (handler == null) return false;
        ItemStack sourceStack = handler.getStackInSlot(0);
        if (!(sourceStack.getItem() instanceof ParticleSourceItem)) return false;
        ParticleSourceData source = ParticleSourceItem.data(sourceStack);
        if (source == null || source.isDepleted()) return false;

        ParticleSourceData.EmitResult proposed = source.emit(SOURCE_BATCH);
        ParticleStack remainder = input.insert(0, proposed.stack(), ParticleAction.SIMULATE);
        long accepted = proposed.stack().amount() - remainder.amount();
        if (accepted <= 0) return false;
        ParticleStack candidate = accepted == proposed.stack().amount() ? proposed.stack()
                : new ParticleStack(proposed.stack().particleId(), accepted,
                proposed.stack().meanEnergyKeV(), proposed.stack().focus());
        ParticleStack commitRemainder = input.insert(0, candidate, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return false;

        ParticleSourceData.EmitResult committedEmission = source.emit(committed);
        ParticleSourceItem.setData(sourceStack, committedEmission.remaining());
        handler.setStackInSlot(0, sourceStack);
        return true;
    }

    private boolean injectFluidSource(LinearAcceleratorControllerBE controller) {
        var handler = controller.contentHandler.getFluidHandler();
        if (handler == null) return false;
        int sourceTank = LinearAcceleratorControllerBE.TANK_SOURCE;
        FluidStack stored = handler.getFluidInTank(sourceTank);
        if (stored.isEmpty()) return false;
        ParticleSourceData source = ParticleSourceCatalog.builtin().fluidSources()
                .get(BuiltInRegistries.FLUID.getKey(stored.getFluid()));
        if (source == null) return false;

        ParticleStack candidate = source.emit(source.capacity()).stack();
        ParticleStack remainder = input.insert(0, candidate, ParticleAction.SIMULATE);
        if (!remainder.isEmpty()) return false;
        FluidStack drained = handler.drainTank(sourceTank, 1, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() != 1 || drained.getFluid() != stored.getFluid()) return false;
        FluidStack committedDrain = handler.drainTank(sourceTank, 1, IFluidHandler.FluidAction.EXECUTE);
        if (committedDrain.getAmount() != 1 || committedDrain.getFluid() != stored.getFluid()) return false;
        ParticleStack commitRemainder = input.insert(0, candidate, ParticleAction.EXECUTE);
        if (!commitRemainder.isEmpty()) {
            handler.fillTank(sourceTank, committedDrain, IFluidHandler.FluidAction.EXECUTE);
            return false;
        }
        return true;
    }

    private void accelerate(LinearAcceleratorControllerBE controller, AcceleratorStats stats, int signal) {
        ParticleStack stack = input.snapshot(0);
        if (stack.isEmpty() || controller.energyStorage == null) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(stack.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        long required;
        ParticleStack accelerated;
        try {
            required = LinearParticleProcessor.requiredEnergy(stats, config);
            accelerated = LinearParticleProcessor.accelerate(stack, definition, stats, config, signal);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return;
        }
        if (controller.energyStorage.getEnergyStoredL() < required) return;
        ParticleStack remainder = pendingOutput.insert(0, accelerated, ParticleAction.SIMULATE);
        long accepted = accelerated.amount() - remainder.amount();
        if (accepted <= 0) return;
        ParticleStack acceptedStack = accepted == accelerated.amount() ? accelerated
                : new ParticleStack(accelerated.particleId(), accepted, accelerated.meanEnergyKeV(),
                accelerated.focus());
        ParticleStack commitRemainder = pendingOutput.insert(0, acceptedStack, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return;
        input.extract(0, committed, ParticleAction.EXECUTE);
        controller.energyStorage.drainEnergy(required);
        addProcessingHeat(stats);
        controller.syncParticleDisplay(committed == acceptedStack.amount() ? acceptedStack
                : new ParticleStack(acceptedStack.particleId(), committed,
                acceptedStack.meanEnergyKeV(), acceptedStack.focus()));
    }

    private void transferOutput(ServerLevel level, BlockPos controllerPos, LinearAcceleratorCache cache) {
        if (cache.beamOutputs.isEmpty()) return;
        ParticleStack pending = pendingOutput.snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(level, controllerPos, cache.beamOutputs.getFirst(),
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }
}
