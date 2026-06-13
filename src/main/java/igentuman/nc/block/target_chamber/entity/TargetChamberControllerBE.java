package igentuman.nc.block.target_chamber.entity;

import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.compat.cc.TargetChamberPeripheral;
import igentuman.nc.compat.oc2.TargetChamberDevice;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.multiblock.particle_chamber.TargetChamberMultiblock;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.TargetChamberRecipe;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.target_chamber.TargetChamberControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.oc2.TargetChamberDevice.DEVICE_CAPABILITY;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class TargetChamberControllerBE extends ParticleChamberControllerBE {

    public static final String NAME = "target_chamber_controller";

    @NBTField
    public int detectorsCount = 0;
    @NBTField
    public double efficiency = 0;
    public int connectedPorts = 0;
    @NBTField
    public int allDetectors = 0;
    public float speed = 0.001f;

    private List<ItemStack> allowedInputs;

    public TargetChamberControllerBE(BlockPos pPos, BlockState pBlockState) {
        this(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    public TargetChamberControllerBE(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public List<ItemStack> getAllowedInputItems() {
        if (allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for (NcRecipe recipe : NcRecipeType.getAllRecipesFor("target_chamber", getLevel())) {
                for (Ingredient ingredient : recipe.getItemIngredients()) {
                    allowedInputs.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
    }

    private LazyOptional<TargetChamberPeripheral> peripheralCap;

    public <T> LazyOptional<T> getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new TargetChamberPeripheral(this));
        }
        return peripheralCap.cast();
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> TargetChamberDevice.createDevice(this)).cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (isOC2Loaded() && cap == DEVICE_CAPABILITY) {
            return getOCDevice(cap, side);
        }
        if (isCcLoaded() && cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
            return getPeripheral(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void processChamberTick() {
        changed = false;
        boolean wasPowered = powered;

        handleValidation();
        hasParticle = particleStorage.getParticleStack() != null;
        trackChanges(hasParticle);
        controllerEnabled = hasRedstoneSignal() && getMultiblock() != null && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        if (getMultiblock() != null && getMultiblock().isFormed()) {
            trackChanges(contentHandler.tick());
            if (controllerEnabled) {
                powered = processReaction();
            } else {
                powered = false;
            }
        }
        changed = powered != wasPowered || changed;
        refreshCacheFlag = getMultiblock() == null || !getMultiblock().isFormed();
        if (refreshCacheFlag || changed || currentTick % 40 == 0) {
            try {
                assert level != null;
                setChanged();
                if (powered != wasPowered) {
                    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) { }
        }
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.1.target_chamber.all_detectors", String.valueOf(allDetectors));
        report.put("report.nc.2.target_chamber.valid_detectors", String.valueOf(detectorsCount));
        return report;
    }

    @Override
    public TargetChamberMultiblock getMultiblock() {
        if (getLevel() == null || getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if (multiblock == null) {
            multiblock = new TargetChamberMultiblock(this);
            validationsCounter = 0;
        }
        return (TargetChamberMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private boolean processReaction() {
        if (recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if (contentHandler.itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
                recipeInfo().clear();
            }
        }
        if (!hasRecipe()) {
            updateRecipe();
        }
        if (hasRecipe()) {
            return process();
        }
        return false;
    }

    private boolean process() {
        if (recipeInfo().be == null) {
            recipeInfo().be = this;
        }
        if (particleStorage.getParticle() == null) {
            return false;
        }
        recipeInfo().process(particleStorage.getParticle().getAmount() * ((Recipe) recipe).crossSection * efficiency / 100D);
        extractParticles();
        if (recipeInfo().radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo().radiation / 10000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        energyStorage().consumeEnergy(energyPerTick);
        handleRecipeOutput();
        return true;
    }

    private void extractParticles() {
        int id = 0;
        for (ParticleStack outputParticle : ((Recipe) recipeInfo().recipe()).outputParticles) {
            if (outputParticle != null && outputParticle.getAmount() > 0) {
                getMultiblock().extractParticle(id, outputParticle);
                particleStorage.outputParticles.add(outputParticle);
            }
        }
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if (recipe == null) {
                recipe = recipeInfo().recipe();
            }
            if (recipe.handleOutputs(contentHandler)) {
                updateRecipe();
            } else {
                recipeInfo().stuck = true;
            }
            setChanged();
        }
    }

    public Recipe getRecipe() {
        if (contentHandler.itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) return null;
        Recipe cachedRecipe = getCachedRecipe();
        if (cachedRecipe != null) return cachedRecipe;
        if (!NcRecipeType.ALL_RECIPES.containsKey("target_chamber")) return null;
        for (NcRecipe recipe : NcRecipeType.getAllRecipesFor("target_chamber", getLevel())) {
            if (((Recipe) recipe).test(contentHandler, particleStorage)) {
                addToCache(recipe);
                return (Recipe) recipe;
            }
        }
        return null;
    }

    public Recipe getCachedRecipe() {
        String key = contentHandler.getCacheKey() + particleStorage.getCacheKey();
        if (cachedRecipes.containsKey(key) && cachedRecipes.get(key) instanceof Recipe recipeTest) {
            if (recipeTest.test(contentHandler, particleStorage)) {
                return recipeTest;
            }
        }
        return null;
    }

    protected void addToCache(NcRecipe recipe) {
        String key = contentHandler.getCacheKey() + particleStorage.getCacheKey();
        if (cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }

    protected void updateRecipe() {
        if (recipe != null) {
            if (((Recipe) recipe).test(contentHandler, particleStorage)) {
                recipeInfo().ticksProcessed = 0;
                if (recipeInfo().consumeInputs(contentHandler)) {
                    return;
                }
                recipe = null;
                recipeInfo().clear();
            } else {
                recipeInfo().clear();
            }
        }
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe) recipe).getAmount();
            recipeInfo().energy = recipeInfo().recipe().getEnergy();
            recipeInfo().radiation = recipeInfo().recipe().getRadiation();
            recipeInfo().be = this;
            if (!recipe.consumeInputs(contentHandler, 1)) {
                recipe = null;
                recipeInfo().clear();
            }
        } else {
            recipeInfo().clear();
        }
    }

    public Object[] getFuel() {
        return contentHandler.itemHandler.getSlotContent(0);
    }

    public void voidFuel() {
        contentHandler.voidSlot(0);
        contentHandler.itemHandler.holdedInputs.clear();
    }

    public ItemStack getCurrentFuel() {
        if (!hasRecipe()) return ItemStack.EMPTY;
        return recipeInfo().recipe().getFirstItemStackIngredient(0);
    }

    public ParticleStack getOutputParticle(int i) {
        if (!hasRecipe()) {
            return null;
        }
        return ((Recipe) recipeInfo().recipe).outputParticles.length > i ? ((Recipe) recipeInfo().recipe).outputParticles[i] : null;
    }

    public static class Recipe extends TargetChamberRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, ParticleStack[] inputParticles, ParticleStack[] outputParticles, long maxEnergy, double crossSection) {
            super(id, input, output, inputFluids, outputFluids, inputParticles, outputParticles, maxEnergy, crossSection);
            CATALYSTS.put(NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return "target_chamber";
        }

        @Override
        public @NotNull String getGroup() {
            return PARTICLE_CHAMBER_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(PARTICLE_CHAMBER_BLOCKS.get(NAME).get());
        }

        public ParticleStack getOutputParticle(int i) {
            return i < outputParticles.length ? outputParticles[i] : null;
        }

        public boolean test(SidedContentHandler sidedContentHandler, ParticleStorage particleStorage) {
            return super.test(sidedContentHandler) && testParticle(particleStorage);
        }

        private boolean testParticle(ParticleStorage particleStorage) {
            if (inputParticles == null || inputParticles.length == 0) {
                return true;
            }
            ParticleStack particleStack = particleStorage.getParticle();
            if (particleStack == null) {
                return false;
            }
            for (ParticleStack inputParticle : inputParticles) {
                if (inputParticle.getParticle().equals(particleStack.getParticle())) {
                    if (inputParticle.getMeanEnergy() <= particleStack.getMeanEnergy()
                            && maxEnergy >= particleStack.getMeanEnergy()
                            && inputParticle.getFocus() <= particleStack.getFocus()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public int getAmount() {
            if (inputParticles == null || inputParticles.length == 0) {
                return 10000;
            }
            return inputParticles[0].getAmount();
        }
    }
}
