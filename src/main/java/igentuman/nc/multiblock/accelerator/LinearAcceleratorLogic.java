package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.api.particle.ParticleSourceData;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Converts local sources to particle packets and accelerates incoming beams. */
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
        ParticleSourceData source = ParticleSourceCatalog.sourceData(sourceStack);
        if (source == null || source.isDepleted()) return false;

        try (var transaction = Transaction.openRoot()) {
            ParticleStack proposed = source.emit(SOURCE_BATCH).stack();
            ParticleStack remainder = input.insert(0, proposed, transaction);
            long accepted = proposed.amount() - remainder.amount();
            if (accepted <= 0) return false;

            ParticleSourceData remaining = source.emit(accepted).remaining();
            ItemStack updated = sourceStack.copy();
            if (remaining.isDepleted() && !(updated.getItem() instanceof ParticleSourceItem)) {
                updated.shrink(1);
                updated.remove(ParticleSourceData.componentType());
            } else {
                ParticleSourceItem.setData(updated, remaining);
            }
            if (handler.extract(0, ItemResource.of(sourceStack), sourceStack.getCount(), transaction)
                    != sourceStack.getCount()) return false;
            if (!updated.isEmpty() && handler.insert(0, ItemResource.of(updated), updated.getCount(), transaction)
                    != updated.getCount()) return false;
            transaction.commit();
        }
        return true;
    }

    private boolean injectFluidSource(LinearAcceleratorControllerBE controller) {
        var handler = controller.contentHandler.getFluidHandler();
        if (handler == null) return false;
        int sourceTank = LinearAcceleratorControllerBE.TANK_SOURCE;
        FluidStack stored = handler.getFluidInTank(sourceTank);
        if (stored.isEmpty()) return false;
        ParticleSourceData source = ParticleSourceCatalog.fluidSource(stored);
        if (source == null) return false;

        ParticleStack candidate = source.emit(source.capacity()).stack();
        try (var transaction = Transaction.openRoot()) {
            if (handler.extract(sourceTank, FluidResource.of(stored), 1, transaction) != 1
                    || !input.insert(0, candidate, transaction).isEmpty()) return false;
            transaction.commit();
        }
        return true;
    }

    private void accelerate(LinearAcceleratorControllerBE controller, AcceleratorStats stats, int signal) {
        ParticleStack stack = input.snapshot(0);
        if (stack.isEmpty() || controller.energyStorage == null) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.getValue(stack.particleId());
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
        processedThisTick = true;
        input.extract(0, committed, ParticleAction.EXECUTE);
        controller.energyStorage.drainEnergy(required);
        addProcessingHeat(stats);
        controller.syncParticleDisplay(committed == acceptedStack.amount() ? acceptedStack
                : new ParticleStack(acceptedStack.particleId(), committed,
                acceptedStack.meanEnergyKeV(), acceptedStack.focus()));
    }

    private void transferOutput(ServerLevel level, BlockPos controllerPos, LinearAcceleratorCache cache) {
        BlockPos outputPort = activeOutputPort(level, cache);
        if (outputPort == null) return;
        ParticleStack pending = pendingOutput.snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.getValue(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(level, controllerPos, outputPort,
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }
}
