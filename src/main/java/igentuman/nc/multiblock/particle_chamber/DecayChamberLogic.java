package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.recipe.particle.DecayChamberRecipe;
import igentuman.nc.recipe.particle.ParticleRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class DecayChamberLogic extends ParticleChamberLogic {

    @Override
    protected void process(ServerLevel level, ParticleChamberControllerBE controller, ParticleChamberCache cache) {
        controller.maxProgress = 100;
        controller.progress = 0;
        if (inputs.isEmpty()) return;
        ParticleStorage inputStorage = inputs.getFirst();
        ParticleStack incoming = inputStorage.snapshot(0);
        if (incoming.isEmpty()) return;

        RecipeHolder<DecayChamberRecipe> holder = bestRecipe(level, ParticleRecipes.DECAY_CHAMBER_TYPE.get(),
                candidate -> candidate.particleInput().test(incoming), DecayChamberRecipe::priority);
        if (holder == null) return;
        DecayChamberRecipe recipe = holder.value();

        long required = recipe.particleInput().amount();
        if (required <= 0) return;
        controller.progress = (int) Math.min(100L, incoming.amount() * 100L / required);
        long maxReactions = incoming.amount() / required;
        if (maxReactions <= 0) return;

        ParticleChamberStats stats = cache.stats;
        long requiredFe = stats.energyPerTick();
        if (controller.energyStorage != null && controller.energyStorage.getEnergyStoredL() < requiredFe) return;

        double p = TargetChamberProcessor.yieldFactor(recipe.crossSection(), stats.efficiency());
        double previousCarry = fractionalYields.isEmpty() ? 0D : fractionalYields.getFirst();
        TargetChamberProcessor.ByproductBatch reactionBatch =
                TargetChamberProcessor.nextByproduct(1L, p, maxReactions, previousCarry);
        long successfulReactions = reactionBatch.amount();
        if (successfulReactions <= 0) {
            finishCycle(null, 0D, List.of(reactionBatch.carry()));
            return;
        }

        ParticleStack extracted = inputStorage.extract(0, maxReactions * required, ParticleAction.EXECUTE);
        if (extracted.isEmpty()) return;
        if (controller.energyStorage != null && requiredFe > 0) controller.energyStorage.drainEnergy(requiredFe);

        List<ParticleStack> templates = recipe.particleOutputs();
        long[] amounts = new long[templates.size()];
        long totalEmitted = 0;
        for (int i = 0; i < templates.size(); i++) {
            amounts[i] = templates.get(i).amount() * successfulReactions;
            totalEmitted += amounts[i];
        }
        long outputEnergy = totalEmitted > 0
                ? Math.max(0L, (extracted.meanEnergyKeV() + recipe.releasedEnergyKeV()) / totalEmitted) : 0;
        commitOutputs(outputCandidates(templates, amounts, outputEnergy, null, cache.beamOutputs.size()));
        finishCycle(null, 0D, List.of(reactionBatch.carry()));
    }
}
