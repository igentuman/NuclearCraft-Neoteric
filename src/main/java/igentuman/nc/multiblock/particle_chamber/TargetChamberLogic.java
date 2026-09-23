package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.recipe.particle.ParticleRecipes;
import igentuman.nc.recipe.particle.TargetChamberRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class TargetChamberLogic extends ParticleChamberLogic {

    @Override
    protected void process(ServerLevel level, ParticleChamberControllerBE controller, ParticleChamberCache cache) {
        if (inputs.isEmpty()) return;
        ParticleStorage inputStorage = inputs.getFirst();
        ParticleStack incoming = inputStorage.snapshot(0);
        if (incoming.isEmpty()) return;

        TargetChamberRecipe recipe;
        ResourceLocation recipeId;
        if (activeReactionId == null) {
            RecipeHolder<TargetChamberRecipe> holder = bestRecipe(level, ParticleRecipes.TARGET_CHAMBER_TYPE.get(),
                    candidate -> TargetChamberProcessor.matchesSpecies(candidate.particleInput(), incoming)
                            && hasTargetInput(controller, candidate),
                    TargetChamberRecipe::priority);
            if (holder == null || !reserveTargetInput(controller, holder.value())) return;
            recipe = holder.value();
            recipeId = holder.id();
        } else {
            recipeId = activeReactionId;
            recipe = resolveRecipe(level, recipeId);
            if (recipe == null) {
                finishCycle(null, 0D, List.of());
                controller.progress = 0;
                return;
            }
            if (!TargetChamberProcessor.matchesSpecies(recipe.particleInput(), incoming)) return;
        }

        ParticleChamberStats stats = cache.stats;
        long required = stats.energyPerTick();
        if (controller.energyStorage != null && controller.energyStorage.getEnergyStoredL() < required) return;

        ParticleStack extracted = inputStorage.extract(0, incoming.amount(), ParticleAction.EXECUTE);
        if (extracted.isEmpty()) return;
        if (controller.energyStorage != null && required > 0) controller.energyStorage.drainEnergy(required);

        double p = TargetChamberProcessor.yieldFactor(recipe.crossSection(), stats.efficiency());
        double work = fractionalWork + TargetChamberProcessor.workAdded(extracted.amount(), p);

        List<ParticleStack> templates = recipe.particleOutputs();
        List<Double> nextCarry = new ArrayList<>();
        long[] amounts = new long[templates.size()];
        long totalEmitted = 0;
        for (int i = 0; i < templates.size(); i++) {
            double previous = i < fractionalYields.size() ? fractionalYields.get(i) : 0D;
            TargetChamberProcessor.ByproductBatch batch = TargetChamberProcessor.nextByproduct(
                    templates.get(i).amount(), p, extracted.amount(), previous);
            amounts[i] = batch.amount();
            nextCarry.add(batch.carry());
            totalEmitted += batch.amount();
        }
        long outputEnergy = totalEmitted > 0
                ? (extracted.meanEnergyKeV() + recipe.releasedEnergyKeV()) / totalEmitted : 0;
        commitOutputs(outputCandidates(templates, amounts, outputEnergy, null, cache.beamOutputs.size()));

        long requiredWork = recipe.particleInput().amount();
        boolean completed = false;
        if (requiredWork > 0 && work >= requiredWork && emitMaterialOutput(controller, recipe)) {
            work -= requiredWork;
            completed = true;
        }
        finishCycle(completed ? null : recipeId, work, nextCarry);
        controller.maxProgress = 100;
        controller.progress = completed || requiredWork <= 0 ? 0
                : (int) Math.min(100L, (long) (Math.max(0, work) * 100L / requiredWork));
    }

    private static TargetChamberRecipe resolveRecipe(ServerLevel level, ResourceLocation recipeId) {
        for (RecipeHolder<TargetChamberRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.TARGET_CHAMBER_TYPE.get())) {
            if (holder.id().equals(recipeId)) return holder.value();
        }
        return null;
    }

    private static boolean hasTargetInput(ParticleChamberControllerBE controller, TargetChamberRecipe recipe) {
        if (!recipe.itemInputs().isEmpty()) {
            var items = controller.contentHandler.getItemHandler();
            if (items == null) return false;
            ItemStack existing = items.getStackInSlot(0);
            for (SizedIngredient ingredient : recipe.itemInputs()) {
                if (ingredient.test(existing) && existing.getCount() >= ingredient.count()) return true;
            }
            return false;
        }
        if (!recipe.fluidInputs().isEmpty()) {
            var fluids = controller.contentHandler.getFluidHandler();
            if (fluids == null) return false;
            FluidStack existing = fluids.getFluidInTank(0);
            for (SizedFluidIngredient ingredient : recipe.fluidInputs()) {
                if (ingredient.test(existing) && existing.getAmount() >= ingredient.amount()) return true;
            }
        }
        return false;
    }

    private static boolean reserveTargetInput(ParticleChamberControllerBE controller, TargetChamberRecipe recipe) {
        if (!recipe.itemInputs().isEmpty()) {
            var items = controller.contentHandler.getItemHandler();
            if (items == null) return false;
            ItemStack existing = items.getStackInSlot(0);
            for (SizedIngredient ingredient : recipe.itemInputs()) {
                if (ingredient.test(existing) && existing.getCount() >= ingredient.count()) {
                    items.extractItem(0, ingredient.count(), false);
                    return true;
                }
            }
            return false;
        }
        if (!recipe.fluidInputs().isEmpty()) {
            var fluids = controller.contentHandler.getFluidHandler();
            if (fluids == null) return false;
            FluidStack existing = fluids.getFluidInTank(0);
            for (SizedFluidIngredient ingredient : recipe.fluidInputs()) {
                if (ingredient.test(existing) && existing.getAmount() >= ingredient.amount()) {
                    FluidStack drained = fluids.drainTank(0, ingredient.amount(), IFluidHandler.FluidAction.EXECUTE);
                    if (drained.getAmount() == ingredient.amount()) return true;
                }
            }
        }
        return false;
    }

    private static boolean emitMaterialOutput(ParticleChamberControllerBE controller, TargetChamberRecipe recipe) {
        if (!recipe.itemOutputs().isEmpty()) {
            var items = controller.contentHandler.getItemHandler();
            if (items == null) return false;
            ItemStack out = recipe.itemOutputs().getFirst().resolve();
            if (out.isEmpty() || !items.insertItem(1, out.copy(), true).isEmpty()) return false;
            items.insertItem(1, out.copy(), false);
            return true;
        }
        if (!recipe.fluidOutputs().isEmpty()) {
            var fluids = controller.contentHandler.getFluidHandler();
            if (fluids == null) return false;
            FluidStack out = recipe.fluidOutputs().getFirst().resolve();
            if (out.isEmpty() || fluids.fillTank(1, out.copy(), IFluidHandler.FluidAction.SIMULATE) != out.getAmount()) {
                return false;
            }
            fluids.fillTank(1, out.copy(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
}
