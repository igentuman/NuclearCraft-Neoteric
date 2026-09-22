package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberRuntimeState;
import igentuman.nc.multiblock.particle_chamber.TargetChamberProcessor;
import igentuman.nc.particle.ParticlePhysics;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.recipe.particle.CollisionChamberRecipe;
import igentuman.nc.recipe.particle.ParticleRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CollisionChamberControllerBE extends ParticleChamberControllerBE {

    public CollisionChamberControllerBE(BlockPos pos, BlockState state, String name) {
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
        if (runtimeState.inputs().size() < 2) {
            progress = 0;
            return;
        }
        ParticleStorage inputA = runtimeState.inputs().get(0);
        ParticleStorage inputB = runtimeState.inputs().get(1);
        ParticleStack a = inputA.snapshot(0);
        ParticleStack b = inputB.snapshot(0);
        if (a.isEmpty() || b.isEmpty()) {
            progress = 0;
            return;
        }

        RecipeHolder<CollisionChamberRecipe> holder = findRecipe(level, a, b);
        if (holder == null) {
            progress = 0;
            return;
        }
        CollisionChamberRecipe recipe = holder.value();

        boolean swapped = !(recipe.particleInputs().get(0).test(a) && recipe.particleInputs().get(1).test(b));
        long requiredA = (swapped ? recipe.particleInputs().get(1) : recipe.particleInputs().get(0)).amount();
        long requiredB = (swapped ? recipe.particleInputs().get(0) : recipe.particleInputs().get(1)).amount();
        if (requiredA <= 0 || requiredB <= 0) {
            progress = 0;
            return;
        }

        int fillA = (int) Math.min(100L, a.amount() * 100L / requiredA);
        int fillB = (int) Math.min(100L, b.amount() * 100L / requiredB);
        progress = Math.min(fillA, fillB);

        long maxReactions = Math.min(a.amount() / requiredA, b.amount() / requiredB);
        if (maxReactions <= 0) return;

        long requiredFe = record.aggregates().energyPerTick();
        if (energyStorage != null && energyStorage.getEnergyStoredL() < requiredFe) return;

        long collisionEnergy = ParticlePhysics.collisionEnergyKeV(a.meanEnergyKeV(), b.meanEnergyKeV());
        double symmetry = ParticlePhysics.collisionSymmetry(a.meanEnergyKeV(), b.meanEnergyKeV());
        double p = ParticlePhysics.collectionFactor(recipe.crossSection(), record.aggregates().efficiency(), symmetry);

        double previousCarry = runtimeState.fractionalYields().isEmpty() ? 0D : runtimeState.fractionalYields().get(0);
        TargetChamberProcessor.ByproductBatch reactionBatch =
                TargetChamberProcessor.nextByproduct(1L, p, maxReactions, previousCarry);
        long successfulReactions = reactionBatch.amount();
        if (successfulReactions <= 0) {
            retainCarry(reactionBatch.carry());
            return;
        }

        List<ParticleStack> outputTemplates = recipe.particleOutputs();
        List<BlockPos> outputPorts = record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of());
        double outputFocus = Math.min(a.focus(), b.focus());
        long[] emitAmounts = new long[outputTemplates.size()];
        long totalEmitted = 0;
        for (int i = 0; i < outputTemplates.size(); i++) {
            emitAmounts[i] = outputTemplates.get(i).amount() * successfulReactions;
            totalEmitted += emitAmounts[i];
        }
        long outputEnergy = totalEmitted > 0
                ? Math.max(0L, (collisionEnergy + recipe.releasedEnergyKeV()) / totalEmitted) : 0;

        ParticleStack[] candidates = new ParticleStack[emitAmounts.length];
        for (int i = 0; i < emitAmounts.length; i++) {
            if (emitAmounts[i] <= 0 || i >= outputPorts.size() || i >= runtimeState.pendingOutputs().size()) continue;
            candidates[i] = new ParticleStack(outputTemplates.get(i).particleId(), emitAmounts[i], outputEnergy, outputFocus);
            if (!runtimeState.pendingOutputs().get(i).insert(0, candidates[i], ParticleAction.SIMULATE).isEmpty()) {
                retainCarry(reactionBatch.carry());
                return;
            }
        }

        ParticleStack extractedA = inputA.extract(0, maxReactions * requiredA, ParticleAction.EXECUTE);
        ParticleStack extractedB = inputB.extract(0, maxReactions * requiredB, ParticleAction.EXECUTE);
        if (extractedA.isEmpty() || extractedB.isEmpty()) return;
        if (energyStorage != null && requiredFe > 0) energyStorage.drainEnergy(requiredFe);

        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] == null) continue;
            runtimeState.pendingOutputs().get(i).insert(0, candidates[i], ParticleAction.EXECUTE);
        }

        runtimeState = new ParticleChamberRuntimeState(runtimeState.inputs(), runtimeState.pendingOutputs(),
                null, 0D, List.of(reactionBatch.carry()), runtimeState.enabled());
        setChanged();
    }

    private void retainCarry(double carry) {
        runtimeState = new ParticleChamberRuntimeState(runtimeState.inputs(), runtimeState.pendingOutputs(),
                null, 0D, List.of(carry), runtimeState.enabled());
        setChanged();
    }

    private RecipeHolder<CollisionChamberRecipe> findRecipe(ServerLevel level, ParticleStack a, ParticleStack b) {
        RecipeHolder<CollisionChamberRecipe> best = null;
        for (RecipeHolder<CollisionChamberRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.COLLISION_CHAMBER_TYPE.get())) {
            CollisionChamberRecipe recipe = holder.value();
            if (!recipe.matchesPair(a, b)) continue;
            if (best == null || recipe.priority() > best.value().priority()
                    || (recipe.priority() == best.value().priority() && holder.id().compareTo(best.id()) < 0)) {
                best = holder;
            }
        }
        return best;
    }
}
