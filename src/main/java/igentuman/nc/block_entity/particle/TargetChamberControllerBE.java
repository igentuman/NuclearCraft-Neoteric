package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberRuntimeState;
import igentuman.nc.multiblock.particle_chamber.TargetChamberProcessor;
import igentuman.nc.particle.ParticleReactionPlan;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.recipe.particle.ParticleRecipes;
import igentuman.nc.recipe.particle.TargetChamberRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class TargetChamberControllerBE extends ParticleChamberControllerBE {

    public TargetChamberControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!formed || !(level instanceof ServerLevel serverLevel) || !runtimeState.enabled()) return;
        scheduledStructure().filter(record -> record.state() == StructureLifecycleState.FORMED)
                .ifPresent(record -> processReaction(serverLevel, record));
    }

    private void processReaction(ServerLevel level, StructureRecord record) {
        if (runtimeState.inputs().isEmpty()) return;
        ParticleStorage inputStorage = runtimeState.inputs().get(0);
        ParticleStack incoming = inputStorage.snapshot(0);
        if (incoming.isEmpty()) return;

        TargetChamberRecipe recipe;
        ResourceLocation recipeId;
        boolean startingNewReaction = runtimeState.activeReaction() == null;
        if (startingNewReaction) {
            RecipeHolder<TargetChamberRecipe> holder = findRecipe(level, incoming);
            if (holder == null) return;
            if (!reserveTargetInput(holder.value())) return;
            recipe = holder.value();
            recipeId = holder.id();
        } else {
            recipeId = runtimeState.activeReaction().recipeId();
            recipe = resolveRecipe(level, recipeId);
            if (recipe == null) {
                clearReaction();
                return;
            }
            if (!TargetChamberProcessor.matchesSpecies(recipe.particleInput(), incoming)) return;
        }

        long required = record.aggregates().energyPerTick();
        if (energyStorage != null && energyStorage.getEnergyStoredL() < required) return;

        ParticleStack extracted = inputStorage.extract(0, incoming.amount(), ParticleAction.EXECUTE);
        if (extracted.isEmpty()) return;
        if (energyStorage != null && required > 0) energyStorage.drainEnergy(required);

        double p = TargetChamberProcessor.yieldFactor(recipe.crossSection(), record.aggregates().efficiency());
        double work = runtimeState.fractionalWork() + TargetChamberProcessor.workAdded(extracted.amount(), p);

        List<ParticleStack> outputTemplates = recipe.particleOutputs();
        List<BlockPos> outputPorts = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
        List<Double> carry = runtimeState.fractionalYields();
        List<Double> nextCarry = new ArrayList<>();
        long[] emitAmounts = new long[outputTemplates.size()];
        long totalEmitted = 0;
        for (int i = 0; i < outputTemplates.size(); i++) {
            double previous = i < carry.size() ? carry.get(i) : 0D;
            TargetChamberProcessor.ByproductBatch batch = TargetChamberProcessor.nextByproduct(
                    outputTemplates.get(i).amount(), p, extracted.amount(), previous);
            emitAmounts[i] = batch.amount();
            nextCarry.add(batch.carry());
            totalEmitted += batch.amount();
        }
        long outputEnergy = totalEmitted > 0
                ? (extracted.meanEnergyKeV() + recipe.releasedEnergyKeV()) / totalEmitted : 0;
        for (int i = 0; i < emitAmounts.length; i++) {
            if (emitAmounts[i] <= 0 || i >= outputPorts.size() || i >= runtimeState.pendingOutputs().size()) continue;
            ParticleStack template = outputTemplates.get(i);
            ParticleStack toEmit = new ParticleStack(template.particleId(), emitAmounts[i], outputEnergy, template.focus());
            runtimeState.pendingOutputs().get(i).insert(0, toEmit, ParticleAction.EXECUTE);
        }

        long requiredWork = recipe.particleInput().amount();
        ParticleReactionPlan reaction = new ParticleReactionPlan(recipeId, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), 0L, 0D);
        boolean completed = false;
        if (requiredWork > 0 && work >= requiredWork && emitMaterialOutput(recipe)) {
            work -= requiredWork;
            completed = true;
        }

        runtimeState = new ParticleChamberRuntimeState(
                runtimeState.inputs(), runtimeState.pendingOutputs(), completed ? null : reaction,
                Math.max(0, work), List.copyOf(nextCarry), runtimeState.enabled());
        maxProgress = 100;
        progress = completed || requiredWork <= 0 ? 0 : (int) Math.min(100L, Math.max(0, work) * 100L / requiredWork);
        setChanged();
    }

    private void clearReaction() {
        runtimeState = new ParticleChamberRuntimeState(
                runtimeState.inputs(), runtimeState.pendingOutputs(), null, 0D, List.of(), runtimeState.enabled());
        progress = 0;
        setChanged();
    }

    private RecipeHolder<TargetChamberRecipe> findRecipe(ServerLevel level, ParticleStack incoming) {
        RecipeHolder<TargetChamberRecipe> best = null;
        for (RecipeHolder<TargetChamberRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.TARGET_CHAMBER_TYPE.get())) {
            TargetChamberRecipe recipe = holder.value();
            if (!TargetChamberProcessor.matchesSpecies(recipe.particleInput(), incoming)) continue;
            if (!hasTargetInputAvailable(recipe)) continue;
            if (best == null || recipe.priority() > best.value().priority()
                    || (recipe.priority() == best.value().priority() && holder.id().compareTo(best.id()) < 0)) {
                best = holder;
            }
        }
        return best;
    }

    private TargetChamberRecipe resolveRecipe(ServerLevel level, ResourceLocation recipeId) {
        return level.getRecipeManager().getAllRecipesFor(ParticleRecipes.TARGET_CHAMBER_TYPE.get()).stream()
                .filter(holder -> holder.id().equals(recipeId))
                .map(RecipeHolder::value)
                .findFirst().orElse(null);
    }

    private boolean hasTargetInputAvailable(TargetChamberRecipe recipe) {
        if (!recipe.itemInputs().isEmpty()) {
            var items = contentHandler.getItemHandler();
            if (items == null) return false;
            ItemStack existing = items.getStackInSlot(0);
            for (SizedIngredient ingredient : recipe.itemInputs()) {
                if (ingredient.test(existing) && existing.getCount() >= ingredient.count()) return true;
            }
            return false;
        }
        if (!recipe.fluidInputs().isEmpty()) {
            var fluids = contentHandler.getFluidHandler();
            if (fluids == null) return false;
            FluidStack existing = fluids.getFluidInTank(0);
            for (SizedFluidIngredient ingredient : recipe.fluidInputs()) {
                if (ingredient.test(existing) && existing.getAmount() >= ingredient.amount()) return true;
            }
            return false;
        }
        return false;
    }

    private boolean reserveTargetInput(TargetChamberRecipe recipe) {
        if (!recipe.itemInputs().isEmpty()) {
            var items = contentHandler.getItemHandler();
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
            var fluids = contentHandler.getFluidHandler();
            if (fluids == null) return false;
            FluidStack existing = fluids.getFluidInTank(0);
            for (SizedFluidIngredient ingredient : recipe.fluidInputs()) {
                if (ingredient.test(existing) && existing.getAmount() >= ingredient.amount()) {
                    FluidStack drained = fluids.drainTank(0, ingredient.amount(), IFluidHandler.FluidAction.EXECUTE);
                    if (drained.getAmount() == ingredient.amount()) return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean emitMaterialOutput(TargetChamberRecipe recipe) {
        if (!recipe.itemOutputs().isEmpty()) {
            var items = contentHandler.getItemHandler();
            if (items == null) return false;
            ItemStack out = recipe.itemOutputs().get(0).resolve();
            if (out.isEmpty()) return false;
            ItemStack remainder = items.insertItem(1, out.copy(), true);
            if (!remainder.isEmpty()) return false;
            items.insertItem(1, out.copy(), false);
            return true;
        }
        if (!recipe.fluidOutputs().isEmpty()) {
            var fluids = contentHandler.getFluidHandler();
            if (fluids == null) return false;
            FluidStack out = recipe.fluidOutputs().get(0).resolve();
            if (out.isEmpty()) return false;
            int filled = fluids.fillTank(1, out.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (filled != out.getAmount()) return false;
            fluids.fillTank(1, out.copy(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
}
