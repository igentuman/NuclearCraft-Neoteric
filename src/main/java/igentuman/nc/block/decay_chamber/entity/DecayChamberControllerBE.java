package igentuman.nc.block.decay_chamber.entity;

import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.multiblock.particle_chamber.DecayChamberMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.DecayChamberRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.block.decay_chamber.DecayChamberControllerBlock.POWERED;
import static igentuman.nc.handler.config.AcceleratorConfig.DECAY_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;

public class DecayChamberControllerBE extends ParticleChamberControllerBE {

    public static final String NAME = "decay_chamber_controller";

    @NBTField
    public double efficiency = 1D;
    public int connectedPorts = 0;

    public DecayChamberControllerBE(BlockPos pPos, BlockState pBlockState) {
        this(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    public DecayChamberControllerBE(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        energyPerTick = DECAY_CHAMBER_CONFIG.BASE_POWER.get();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public DecayChamberMultiblock getMultiblock() {
        if (getLevel() == null || getLevel().isClientSide()) {
            return null;
        }
        if (multiblock == null) {
            multiblock = new DecayChamberMultiblock(this);
            validationsCounter = 0;
        }
        return (DecayChamberMultiblock) multiblock;
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
        hasParticle = particleStorage.getParticleStack() != null;
        trackChanges(hasParticle);
        controllerEnabled = hasRedstoneSignal() && getMultiblock() != null && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        if (getMultiblock() != null && getMultiblock().isFormed() && controllerEnabled) {
            powered = processDecay();
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

    private boolean processDecay() {
        if (energyStorage().getEnergyStored() < energyPerTick) return false;
        if (particleStorage.getParticle() == null) return false;

        if (!hasRecipe()) {
            updateRecipe();
        }
        if (!hasRecipe()) return false;

        Recipe r = (Recipe) recipeInfo().recipe();
        recipeInfo().process(particleStorage.getParticle().getAmount() * r.crossSection * efficiency);
        energyStorage().consumeEnergy(energyPerTick);
        emitOutputs(r);
        if (recipeInfo().isCompleted()) {
            updateRecipe();
        }
        return true;
    }

    private void emitOutputs(Recipe r) {
        int idx = 0;
        for (ParticleStack out : r.outputParticles) {
            if (out != null && out.getAmount() > 0) {
                ParticleStack copy = out.copy();
                copy.setMeanEnergy(particleStorage.getParticle().getMeanEnergy() + r.energyReleased / Math.max(1, r.outputParticles.length));
                copy.setAmount(Math.max(1, out.getAmount()));
                copy.setFocus(particleStorage.getParticle().getFocus());
                particleStorage.outputParticles.add(copy);
                getMultiblock().extractParticle(idx, copy);
                idx++;
            }
        }
    }

    public Recipe getRecipe() {
        if (particleStorage.getParticle() == null) return null;
        Recipe cached = getCachedRecipe();
        if (cached != null) return cached;
        if (!NcRecipeType.ALL_RECIPES.containsKey(DecayChamberRecipe.CODE_ID)) return null;
        for (NcRecipe recipe : NcRecipeType.getAllRecipesFor(DecayChamberRecipe.CODE_ID, getLevel())) {
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
        cachedRecipes.put(particleStorage.getCacheKey(), recipe);
    }

    protected void updateRecipe() {
        if (recipe instanceof Recipe current && current.test(particleStorage)) {
            recipeInfo().ticksProcessed = 0;
            return;
        }
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe) recipe).getAmount();
            recipeInfo().energy = energyPerTick;
            recipeInfo().radiation = recipe.getRadiation();
            recipeInfo().be = this;
        } else {
            recipeInfo().clear();
        }
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.decay_chamber.connected_ports", String.valueOf(connectedPorts));
        report.put("report.nc.decay_chamber.efficiency", String.format("%.2f", efficiency));
        return report;
    }

    public static class Recipe extends DecayChamberRecipe {

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

        public boolean test(ParticleStorage particleStorage) {
            if (inputParticles == null || inputParticles.length == 0) return false;
            ParticleStack stack = particleStorage.getParticle();
            if (stack == null) return false;
            ParticleStack want = inputParticles[0];
            if (!want.getParticle().equals(stack.getParticle())) return false;
            if (want.getMeanEnergy() > stack.getMeanEnergy()) return false;
            if (maxEnergy < stack.getMeanEnergy()) return false;
            if (want.getFocus() > stack.getFocus()) return false;
            return stack.getAmount() >= want.getAmount();
        }
    }
}
