package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import igentuman.nc.particle.ParticlePhysics;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.recipe.particle.CollisionChamberRecipe;
import igentuman.nc.recipe.particle.ParticleRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class CollisionChamberLogic extends ParticleChamberLogic {

    @Override
    protected void process(ServerLevel level, ParticleChamberControllerBE controller, ParticleChamberCache cache) {
        controller.maxProgress = 100;
        controller.progress = 0;
        if (inputs.size() < 2) return;
        ParticleStorage inputA = inputs.get(0);
        ParticleStorage inputB = inputs.get(1);
        ParticleStack a = inputA.snapshot(0);
        ParticleStack b = inputB.snapshot(0);
        if (a.isEmpty() || b.isEmpty()) return;

        RecipeHolder<CollisionChamberRecipe> holder = bestRecipe(level, ParticleRecipes.COLLISION_CHAMBER_TYPE.get(),
                candidate -> candidate.matchesPair(a, b), CollisionChamberRecipe::priority);
        if (holder == null) return;
        CollisionChamberRecipe recipe = holder.value();

        boolean swapped = !(recipe.particleInputs().get(0).test(a) && recipe.particleInputs().get(1).test(b));
        long requiredA = recipe.particleInputs().get(swapped ? 1 : 0).amount();
        long requiredB = recipe.particleInputs().get(swapped ? 0 : 1).amount();
        if (requiredA <= 0 || requiredB <= 0) return;
        controller.progress = (int) Math.min(Math.min(100L, a.amount() * 100L / requiredA),
                Math.min(100L, b.amount() * 100L / requiredB));

        long maxReactions = Math.min(a.amount() / requiredA, b.amount() / requiredB);
        if (maxReactions <= 0) return;

        ParticleChamberStats stats = cache.stats;
        long requiredFe = stats.energyPerTick();
        if (controller.energyStorage != null && controller.energyStorage.getEnergyStoredL() < requiredFe) return;

        long collisionEnergy = ParticlePhysics.collisionEnergyKeV(a.meanEnergyKeV(), b.meanEnergyKeV());
        double symmetry = ParticlePhysics.collisionSymmetry(a.meanEnergyKeV(), b.meanEnergyKeV());
        double p = ParticlePhysics.collectionFactor(recipe.crossSection(), stats.efficiency(), symmetry);
        double previousCarry = fractionalYields.isEmpty() ? 0D : fractionalYields.getFirst();
        TargetChamberProcessor.ByproductBatch reactionBatch =
                TargetChamberProcessor.nextByproduct(1L, p, maxReactions, previousCarry);
        long successfulReactions = reactionBatch.amount();
        if (successfulReactions <= 0) {
            finishCycle(null, 0D, List.of(reactionBatch.carry()));
            return;
        }

        List<ParticleStack> templates = recipe.particleOutputs();
        long[] amounts = new long[templates.size()];
        long totalEmitted = 0;
        for (int i = 0; i < templates.size(); i++) {
            amounts[i] = templates.get(i).amount() * successfulReactions;
            totalEmitted += amounts[i];
        }
        long outputEnergy = totalEmitted > 0
                ? Math.max(0L, (collisionEnergy + recipe.releasedEnergyKeV()) / totalEmitted) : 0;
        ParticleStack[] candidates = outputCandidates(templates, amounts, outputEnergy,
                Math.min(a.focus(), b.focus()), cache.beamOutputs.size());
        if (!outputsFit(candidates)) {
            finishCycle(null, 0D, List.of(reactionBatch.carry()));
            return;
        }

        ParticleStack extractedA = inputA.extract(0, maxReactions * requiredA, ParticleAction.EXECUTE);
        ParticleStack extractedB = inputB.extract(0, maxReactions * requiredB, ParticleAction.EXECUTE);
        if (extractedA.isEmpty() || extractedB.isEmpty()) return;
        if (controller.energyStorage != null && requiredFe > 0) controller.energyStorage.drainEnergy(requiredFe);
        commitOutputs(candidates);
        finishCycle(null, 0D, List.of(reactionBatch.carry()));
    }
}
