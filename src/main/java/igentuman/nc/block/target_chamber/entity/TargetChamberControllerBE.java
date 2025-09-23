package igentuman.nc.block.target_chamber.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.TargetChamberPeripheral;
import igentuman.nc.compat.oc2.TargetChamberDevice;
import igentuman.nc.content.particles.CapabilityParticleStackHandler;
import igentuman.nc.content.particles.IParticleStackHandler;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.particle_chamber.TargetChamberMultiblock;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.TargetChamberRecipe;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.target_chamber.TargetChamberControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.compat.oc2.TargetChamberDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.*;

public class TargetChamberControllerBE extends MultiblockControllerBE {

    public static final String NAME = "target_chamber_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;
    protected final LazyOptional<IParticleStackHandler> particleHandler;
    public final ParticleStorage particleStorage;

    @NBTField
    public int detectorsCount = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    public int connectedPorts = 0;
    protected boolean forceShutdown = false;
    @NBTField
    public boolean enabledByController = false;
    public boolean controllerEnabled = false;
    private Direction facing;
    private List<ItemStack> allowedInputs;
    @NBTField
    public boolean hasRedstoneSignal = false;
    @NBTField
    public int allDetectors = 0;
    @NBTField
    public boolean hasParticle = false;

    public TargetChamberControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(),pPos, pBlockState);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 1);
        contentHandler().setBlockEntity(this);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        energyStorage = createEnergy();
        energyStorage
                .setInputEnergyTier(getBaseGTEnergyTier())
                .setOutputEnergyTier(0)
                .setInputAmperage(16)
                .setOutputAmperage(0);
        energy = LazyOptional.of(() -> energyStorage);
        particleStorage = new ParticleStorage();
        particleStorage.setTileEntity(this);
        particleHandler = CapabilityParticleStackHandler.createHandler(particleStorage);
    }

    @Override
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal();
    }

    @Override
    public String getName() {
        return NAME;
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("target_chamber", getLevel())) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    allowedInputs.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
        }
    }
    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public ItemCapabilityHandler getItemInventory()
    {
        return contentHandler().itemHandler;
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 100000000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }


    private LazyOptional<TargetChamberPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
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

        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return particleHandler.cast();
        }

        if (cap == FLUID_HANDLER) {
            return contentHandler().getFluidCapability(side);
        }

        if (cap == ITEM_HANDLER) {
            return contentHandler().getItemCapability(side);
        }

        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(this, side).cast();
                }
            }
        }
        if (cap == ENERGY) {
            if(!isOnlyGTCEUCapEnabled()) {
                return getEnergy().cast();
            } else {
                return LazyOptional.empty();
            }
        }


        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
            }
        }


        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    public void tickServer() {

        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            controllerEnabled = false;
            return;
        }

        changed = false;
        super.tickServer();
        boolean wasFormed = getMultiblock().isFormed();
        boolean wasEnabled = controllerEnabled;
        boolean wasPowered = powered;

        handleValidation();
        hasParticle = particleStorage.getParticleStack() != null;
        trackChanges(hasParticle);
        controllerEnabled = hasRedstoneSignal() && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        if (getMultiblock().isFormed()) {
            trackChanges(contentHandler().tick());
            if(controllerEnabled) {
                powered = processReaction();
            } else {
                powered = false;
            }
        }
        changed = powered != wasPowered || changed;
        refreshCacheFlag = !getMultiblock().isFormed();
        if(refreshCacheFlag || changed || currentTick % 40 == 0) {
            try {
                assert level != null;
                setChanged();
                if(powered != wasPowered) {
                    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);

            } catch (NullPointerException ignored) {}
        }
        particleStorage.setParticleStack(null);
        particleStorage.outputParticles.clear();
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.1.target_chamber.all_detectors", String.valueOf(allDetectors));
        report.put("report.nc.2.target_chamber.valid_detectors", String.valueOf(detectorsCount));
        return report;
    }

    protected void handleValidation() {
        super.handleValidation();
    }

    @Override
    public TargetChamberMultiblock getMultiblock() {
        if(getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if(multiblock == null) {
            multiblock = new TargetChamberMultiblock(this);
        }
        return (TargetChamberMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    public float speed = 0.001f;

    private boolean processReaction() {
        if(recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if(contentHandler().itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
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

        if(recipeInfo().be == null) {
            recipeInfo().be = this;
        }
        if(particleStorage.getParticle() == null) {
            return false;
        }
        recipeInfo().process(particleStorage.getParticle().getAmount()*((Recipe)recipe).crossSection * efficiency / 100D);
        extractParticles();
        if(recipeInfo().radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo().radiation/10000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        energyStorage().consumeEnergy(energyPerTick);
        handleRecipeOutput();

        return true;
    }

    private void extractParticles() {
        int id = 0;
        for (ParticleStack outputParticle : ((Recipe)recipeInfo().recipe()).outputParticles) {
            if (outputParticle != null && outputParticle.getAmount() > 0) {
                getMultiblock().extractParticle(id, outputParticle);
                particleStorage.outputParticles.add(outputParticle);
            }
        }
    }


    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if(recipe == null) {
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

    @Override
    public Recipe getRecipe() {
        if(contentHandler().itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) return null;
        Recipe cachedRecipe = getCachedRecipe();
        if(cachedRecipe != null) return cachedRecipe;
        if(!NcRecipeType.ALL_RECIPES.containsKey("target_chamber")) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("target_chamber", getLevel())) {
            if(((Recipe)recipe).test(contentHandler(), particleStorage)) {
                addToCache(recipe);
                return (Recipe) recipe;
            }
        }
        return null;
    }

    public Recipe getCachedRecipe() {
        String key = contentHandler().getCacheKey()+particleStorage.getCacheKey();
        if(cachedRecipes.containsKey(key) && cachedRecipes.get(key) instanceof Recipe recipeTest) {
            if(recipeTest.test(contentHandler(), particleStorage)) {
                return recipeTest;
            }
        }
        return null;
    }

    protected void addToCache(NcRecipe recipe) {
        String key = contentHandler().getCacheKey()+particleStorage.getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }


    protected void updateRecipe() {
        //check if last recipe is still valid
        if(recipe != null) {
            if(((Recipe)recipe).test(contentHandler(), particleStorage)) {
                recipeInfo().ticksProcessed = 0;
                if (recipeInfo().consumeInputs(contentHandler())) {
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
            recipeInfo().ticks = ((Recipe)recipe).getAmount();
            recipeInfo().energy = recipeInfo().recipe().getEnergy();
            recipeInfo().radiation = recipeInfo().recipe().getRadiation();
            recipeInfo().be = this;
            if(!recipe.consumeInputs(contentHandler, 1)) {
                recipe = null;
                recipeInfo().clear();
            }
        } else {
            recipeInfo().clear();
        }
    }

    public boolean recipeIsStuck() {
        return recipeInfo().isStuck();
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe() != null;
    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public double getRecipeProgress() {
        return recipeInfo().getProgress();
    }


    public boolean hasRedstoneSignal() {
        if(currentTick % 10 == 0) {
            hasRedstoneSignal = getLevel().hasNeighborSignal(getBlockPos());
        }
        return enabledByController || hasRedstoneSignal;
    }

    public Object[] getFuel() {
        return contentHandler().itemHandler.getSlotContent(0);
    }

    public void voidFuel() {
        contentHandler().voidSlot(0);
        contentHandler().itemHandler.holdedInputs.clear();
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    public ItemStack getCurrentFuel() {
        if(!hasRecipe()) return ItemStack.EMPTY;
        return recipeInfo().recipe().getFirstItemStackIngredient(0);
    }

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo().ticksProcessed > 0 && !recipeInfo().isCompleted();
    }

    public void enableReactor() {
        toggleReactor(true);
    }

    public void toggleReactor(boolean mode) {
        controllerEnabled = mode || getRedstoneSignal() > 0;
        enabledByController = mode;
    }

    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
    }


    public void refresh() {
        setChanged();
    }

    public ParticleStack getParticleStack() {
        return particleStorage.getParticle();
    }

    public ParticleStack getOutputParticle(int i) {
        if (!hasRecipe()) {
            return null;
        }
        return ((Recipe)recipeInfo().recipe).outputParticles.length > i ? ((Recipe)recipeInfo().recipe).outputParticles[i] : null;
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
            return TARGET_CHAMBER_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(TARGET_CHAMBER_BLOCKS.get(NAME).get());
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
                    if (
                            inputParticle.getMeanEnergy() <= particleStack.getMeanEnergy()
                            && maxEnergy >= particleStack.getMeanEnergy()
                            && inputParticle.getFocus() <= particleStack.getFocus()
                    ) {
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
