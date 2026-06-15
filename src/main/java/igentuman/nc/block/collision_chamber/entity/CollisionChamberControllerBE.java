package igentuman.nc.block.collision_chamber.entity;

import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.multiblock.particle_chamber.CollisionChamberMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.CollisionChamberRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.block.collision_chamber.CollisionChamberControllerBlock.POWERED;
import static igentuman.nc.handler.config.AcceleratorConfig.COLLISION_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;

public class CollisionChamberControllerBE extends ParticleChamberControllerBE {

    public static final String NAME = "collision_chamber_controller";

    @NBTField
    public int connectedPorts = 0;

    public CollisionChamberControllerBE(BlockPos pPos, BlockState pBlockState) {
        this(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    public CollisionChamberControllerBE(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        energyPerTick = COLLISION_CHAMBER_CONFIG.BASE_POWER.get();
        particleStorage.doNotMergeParticles();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CollisionChamberMultiblock getMultiblock() {
        if (getLevel() == null || getLevel().isClientSide()) {
            return null;
        }
        if (multiblock == null) {
            multiblock = new CollisionChamberMultiblock(this);
            validationsCounter = 0;
        }
        return (CollisionChamberMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    protected void processChamberTick() {
        boolean wasPowered = powered;
        changed = false;

        handleValidation();
        hasParticle = particleStorage.getParticleStack() != null && particleStorage.getParticleStackB() != null;
        trackChanges(hasParticle);
        controllerEnabled = hasRedstoneSignal() && getMultiblock() != null && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        if (getMultiblock() != null && getMultiblock().isFormed() && controllerEnabled) {
            powered = processCollision();
        } else {
            powered = false;
        }
        changed = powered != wasPowered || changed;

        refreshCacheFlag = getMultiblock() == null || !getMultiblock().isFormed();
        if (refreshCacheFlag || changed || currentTick % 40 == 0) {
            try {
                setChanged();
                if (powered != wasPowered) {
                    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) { }
        }
    }

    private boolean processCollision() {
        if (energyStorage().getEnergyStored() < energyPerTick) return false;
        ParticleStack inA = particleStorage.getParticleStack();
        ParticleStack inB = particleStorage.getParticleStackB();
        if (inA == null || inB == null) return false;

        if (!hasRecipe()) {
            updateRecipe();
        }
        if (!hasRecipe()) return false;

        Recipe r = (Recipe) recipeInfo().recipe();
        long collisionEnergy = Math.round(2 * Math.sqrt((double) inA.getMeanEnergy() * (double) inB.getMeanEnergy()));
        double symmetry = 1 - Math.abs(inA.getMeanEnergy() - inB.getMeanEnergy())
                / (double) Math.max(1, inA.getMeanEnergy() + inB.getMeanEnergy());
        double outputFactor = Math.min(1D, r.crossSection * (efficiency/100D) * symmetry);
        int inputAmount = Math.min(inA.getAmount(), inB.getAmount());

        recipeInfo().process(inputAmount * outputFactor);
        energyStorage().consumeEnergy(energyPerTick);
        emitOutputs(r, collisionEnergy, outputFactor, inputAmount, Math.min(inA.getFocus(), inB.getFocus()));
        if (recipeInfo().isCompleted()) {
            updateRecipe();
        }
        return true;
    }

    private void emitOutputs(Recipe r, long collisionEnergy, double outputFactor, int inputAmount, double inputFocus) {
        int totalParticles = 0;
        for (ParticleStack out : r.outputParticles) {
            if (out != null) totalParticles += out.getAmount();
        }
        if (totalParticles <= 0) totalParticles = 1;

        int idx = 0;
        for (ParticleStack out : r.outputParticles) {
            if (out == null || out.getAmount() <= 0) {
                idx++;
                continue;
            }
            ParticleStack copy = out.copy();
            copy.setMeanEnergy(Math.round((collisionEnergy + r.energyReleased) / (double) totalParticles));
            copy.setAmount((int) Math.max(1, Math.round(out.getAmount() * outputFactor * inputAmount)));
            copy.setFocus(inputFocus);
            particleStorage.outputParticles.add(copy);
            getMultiblock().extractParticle(idx, copy);
            idx++;
        }
    }

    public Recipe getRecipe() {
        if (particleStorage.getParticleStack() == null || particleStorage.getParticleStackB() == null) return null;
        Recipe cached = getCachedRecipe();
        if (cached != null) return cached;
        if (!NcRecipeType.ALL_RECIPES.containsKey(CollisionChamberRecipe.CODE_ID)) return null;
        for (NcRecipe recipe : NcRecipeType.getAllRecipesFor(CollisionChamberRecipe.CODE_ID, getLevel())) {
            if (((Recipe) recipe).test(particleStorage)) {
                addToCache(recipe);
                return (Recipe) recipe;
            }
        }
        return null;
    }

    public Recipe getCachedRecipe() {
        String key = particleStorage.getCacheKey();
        if (cachedRecipes.containsKey(key) && cachedRecipes.get(key) instanceof Recipe r) {
            if (r.test(particleStorage)) return r;
        }
        return null;
    }

    protected void addToCache(NcRecipe recipe) {
        String key = particleStorage.getCacheKey();
        cachedRecipes.put(key, recipe);
    }

    protected void updateRecipe() {
        if (recipe instanceof Recipe current && current.test(particleStorage)) {
            recipeInfo().ticksProcessed = 0;
            return;
        }
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe) recipe).getAmount()*10000;
            recipeInfo().energy = energyPerTick;
            recipeInfo().radiation = recipe.getRadiation();
            recipeInfo().be = this;
        } else {
            recipeInfo().clear();
        }
    }

    @Override
    public ParticleStack getOutputParticle(int i) {
        if (!hasRecipe()) return null;
        Recipe r = (Recipe) recipeInfo().recipe();
        return r.outputParticles.length > i ? r.outputParticles[i] : null;
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.1.collision_chamber.connected_ports", String.valueOf(connectedPorts));
        report.put("report.nc.2.collision_chamber.efficiency", String.format("%.2f%%", efficiency));
        return report;
    }

    public static class Recipe extends CollisionChamberRecipe {

        public Recipe(ResourceLocation id,
                      ParticleStack[] inputParticles,
                      ParticleStack[] outputParticles,
                      long minEnergy,
                      long maxEnergy,
                      long energyReleased,
                      double crossSection) {
            super(id, inputParticles, outputParticles, minEnergy, maxEnergy, energyReleased, crossSection);
        }

        @Override
        public @NotNull String getGroup() {
            return PARTICLE_CHAMBER_BLOCKS.containsKey(NAME) ? PARTICLE_CHAMBER_BLOCKS.get(NAME).get().getName().getString() : CODE_ID;
        }

        public boolean test(ParticleStorage storage) {
            if (inputParticles == null || inputParticles.length < 2) return false;
            ParticleStack stackA = storage.getParticleStack();
            ParticleStack stackB = storage.getParticleStackB();
            if (stackA == null || stackB == null) return false;
            return matchesPair(inputParticles[0], stackA, inputParticles[1], stackB)
                    || matchesPair(inputParticles[0], stackB, inputParticles[1], stackA);
        }

        private boolean matchesPair(ParticleStack wantA, ParticleStack haveA, ParticleStack wantB, ParticleStack haveB) {
            if (!wantA.getParticle().equals(haveA.getParticle())) return false;
            if (!wantB.getParticle().equals(haveB.getParticle())) return false;
            if (haveA.getMeanEnergy() < minEnergy || haveB.getMeanEnergy() < minEnergy) return false;
            if (maxEnergy > 0 && (haveA.getMeanEnergy() > maxEnergy || haveB.getMeanEnergy() > maxEnergy)) return false;
            if (haveA.getAmount() < wantA.getAmount()) return false;
            if (haveB.getAmount() < wantB.getAmount()) return false;
            return true;
        }
    }

    /* helper for KubeJS-style recipe enumeration */
    public List<NcRecipe> getRecipes() {
        return List.copyOf(NcRecipeType.ALL_RECIPES.get(CollisionChamberRecipe.CODE_ID).getRecipeType().getRecipes(getLevel()));
    }
}
