package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberRuntimeState;
import igentuman.nc.multiblock.particle_chamber.TargetChamberProcessor;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.recipe.particle.DecayChamberRecipe;
import igentuman.nc.recipe.particle.ParticleRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DecayChamberControllerBE extends ParticleChamberControllerBE {

    public DecayChamberControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!formed || !(level instanceof ServerLevel serverLevel) || !runtimeState.enabled()) return;
        scheduledStructure().filter(record -> record.state() == StructureLifecycleState.FORMED)
                .ifPresent(record -> processReactions(serverLevel, record));
    }

    private void processReactions(ServerLevel level, StructureRecord record) {
        maxProgress = 100;
        if (runtimeState.inputs().isEmpty()) {
            progress = 0;
            return;
        }
        ParticleStorage inputStorage = runtimeState.inputs().get(0);
        ParticleStack incoming = inputStorage.snapshot(0);
        if (incoming.isEmpty()) {
            progress = 0;
            return;
        }

        RecipeHolder<DecayChamberRecipe> holder = findRecipe(level, incoming);
        if (holder == null) {
            progress = 0;
            return;
        }
        DecayChamberRecipe recipe = holder.value();

        long required = recipe.particleInput().amount();
        if (required <= 0) {
            progress = 0;
            return;
        }
        progress = (int) Math.min(100L, incoming.amount() * 100L / required);
        long maxReactions = incoming.amount() / required;
        if (maxReactions <= 0) return;

        long requiredFe = record.aggregates().energyPerTick();
        if (energyStorage != null && energyStorage.getEnergyStoredL() < requiredFe) return;

        double p = TargetChamberProcessor.yieldFactor(recipe.crossSection(), record.aggregates().efficiency());
        double previousCarry = runtimeState.fractionalYields().isEmpty() ? 0D : runtimeState.fractionalYields().get(0);
        TargetChamberProcessor.ByproductBatch reactionBatch =
                TargetChamberProcessor.nextByproduct(1L, p, maxReactions, previousCarry);
        long successfulReactions = reactionBatch.amount();
        if (successfulReactions <= 0) {
            runtimeState = new ParticleChamberRuntimeState(runtimeState.inputs(), runtimeState.pendingOutputs(),
                    null, 0D, List.of(reactionBatch.carry()), runtimeState.enabled());
            setChanged();
            return;
        }

        ParticleStack extracted = inputStorage.extract(0, maxReactions * required, ParticleAction.EXECUTE);
        if (extracted.isEmpty()) return;
        if (energyStorage != null && requiredFe > 0) energyStorage.drainEnergy(requiredFe);

        List<ParticleStack> outputTemplates = recipe.particleOutputs();
        List<BlockPos> outputPorts = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
        long[] emitAmounts = new long[outputTemplates.size()];
        long totalEmitted = 0;
        for (int i = 0; i < outputTemplates.size(); i++) {
            emitAmounts[i] = outputTemplates.get(i).amount() * successfulReactions;
            totalEmitted += emitAmounts[i];
        }
        long outputEnergy = totalEmitted > 0
                ? Math.max(0L, (extracted.meanEnergyKeV() + recipe.releasedEnergyKeV()) / totalEmitted) : 0;
        for (int i = 0; i < emitAmounts.length; i++) {
            if (emitAmounts[i] <= 0 || i >= outputPorts.size() || i >= runtimeState.pendingOutputs().size()) continue;
            ParticleStack template = outputTemplates.get(i);
            ParticleStack toEmit = new ParticleStack(template.particleId(), emitAmounts[i], outputEnergy, template.focus());
            runtimeState.pendingOutputs().get(i).insert(0, toEmit, ParticleAction.EXECUTE);
        }

        runtimeState = new ParticleChamberRuntimeState(runtimeState.inputs(), runtimeState.pendingOutputs(),
                null, 0D, List.of(reactionBatch.carry()), runtimeState.enabled());
        setChanged();
    }

    private RecipeHolder<DecayChamberRecipe> findRecipe(ServerLevel level, ParticleStack incoming) {
        RecipeHolder<DecayChamberRecipe> best = null;
        for (RecipeHolder<DecayChamberRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.DECAY_CHAMBER_TYPE.get())) {
            DecayChamberRecipe recipe = holder.value();
            if (!recipe.particleInput().test(incoming)) continue;
            if (best == null || recipe.priority() > best.value().priority()
                    || (recipe.priority() == best.value().priority() && holder.id().compareTo(best.id()) < 0)) {
                best = holder;
            }
        }
        return best;
    }
}
