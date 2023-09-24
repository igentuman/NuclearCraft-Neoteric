package igentuman.nc.block.entity.fusion;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.compat.cc.NCFusionReactorPeripheral;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.multiblock.fusion.FusionReactorMultiblock;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.util.ModUtil.isCcLoaded;

public class FusionCoreBE <RECIPE extends FusionCoreBE.Recipe> extends FusionBE {

    @NBTField
    public double heat = 0;
    @NBTField
    public boolean isCasingValid = false;

    @NBTField
    public int size = 0;
    @NBTField
    public double heatSinkCooling = 0;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double heatMultiplier = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    public int inputRedstoneSignal = 0;
    @NBTField
    protected boolean forceShutdown = false;
    @NBTField
    public double magneticFieldStrength = 0;
    @NBTField
    public int magnetsPower = 0;
    @NBTField
    public int maxMagnetsTemp = 0;
    @NBTField
    public int rfAmplification = 0;
    @NBTField
    public int rfAmplifiersPower = 0;
    @NBTField
    public int minRFAmplifiersTemp = 0;

    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage = createEnergy();

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;

    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo recipeInfo = new RecipeInfo();
    public boolean controllerEnabled = false;
    public RECIPE recipe;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    protected boolean initialized = false;
    @NBTField
    private boolean isInternalValid = false;
    private int reValidateCounter = 40;

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public FusionCoreBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        multiblock = new FusionReactorMultiblock(this);
        contentHandler = new SidedContentHandler(
                0, 0,
                2, 4);
        contentHandler.setBlockEntity(this);
    }

    private LazyOptional<NCFusionReactorPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCFusionReactorPeripheral(this));
        }
        return peripheralCap.cast();
    }
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler.getFluidCapability(side);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        initialized = false;
    }

    public double getMaxHeat() {
        return 10000000;
    }
    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        if(!initialized) {
            initialized = true;
            FusionCoreBlock block = (FusionCoreBlock) getBlockState().getBlock();
            block.placeProxyBlocks(getBlockState(), level, worldPosition, this);
        }
        super.tickServer();
        multiblock().tick();
        boolean wasFormed = multiblock().isFormed();
        boolean wasPowered = powered;

        if (!wasFormed) {
            reValidateCounter++;
            if(reValidateCounter < 40) {
                return;
            }
            reValidateCounter = 0;
            multiblock().validate();
            powered = false;
        }
        isCasingValid = multiblock().isOuterValid();
        isInternalValid = multiblock().isInnerValid();

        size = multiblock().width();//todo remove
        boolean changed = wasPowered != powered || wasFormed != multiblock().isFormed();
        changed = updateCharacteristics() || changed;
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && multiblock().isReadyToProcess();
        controllerEnabled = !forceShutdown && controllerEnabled;
        if (multiblock().isFormed()) {
            size = multiblock().width();

            if(controllerEnabled) {
                powered = processReaction();
                changed = powered || changed;
            } else {
                powered = false;
            }
            changed = coolDown() || changed;
            handleMeltdown();
        }
        boolean refreshCacheFlag = !multiblock().isFormed();
        if(refreshCacheFlag || changed) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
        }
        controllerEnabled = false;
    }

    private boolean updateCharacteristics() {
        boolean hasChanges =
                magneticFieldStrength != multiblock().magneticFieldStrength
                || magnetsPower != multiblock().magnetsPower
                || maxMagnetsTemp != multiblock().maxMagnetsTemp
                || rfAmplification != multiblock().rfAmplification
                || rfAmplifiersPower != multiblock().rfAmplifiersPower
                || minRFAmplifiersTemp != multiblock().maxRFAmplifiersTemp;
        magneticFieldStrength = multiblock().magneticFieldStrength;
        magnetsPower = multiblock().magnetsPower;
        maxMagnetsTemp = multiblock().maxMagnetsTemp;
        rfAmplification = multiblock().rfAmplification;
        rfAmplifiersPower = multiblock().rfAmplifiersPower;
        minRFAmplifiersTemp = multiblock().maxRFAmplifiersTemp;
        return hasChanges;
    }

    private boolean coolDown() {
        double wasHeat = heat;
       // heat -= coolingPerTick();
        heat = Math.max(0, heat);
        return wasHeat != heat;
    }

    private boolean processReaction() {
        heatMultiplier = heatMultiplier() + collectedHeatMultiplier() - 1;
        if(recipeInfo.recipe != null && recipeInfo.isCompleted()) {
            if(contentHandler.itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
                recipeInfo.clear();
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
        recipeInfo.process( (heatMultiplier() + collectedHeatMultiplier() - 1));
        if(recipeInfo.radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo.radiation/1000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        if (!recipeInfo.isCompleted()) {
            energyStorage.addEnergy(calculateEnergy());
            heat += calculateHeat();
        }

        handleRecipeOutput();

        efficiency = calculateEfficiency();
        return true;
    }

    private double calculateEfficiency() {
        return 1;
    }

    private double calculateHeat() {
        return 0;
    }

    private int calculateEnergy() {
        energyPerTick = (int) ((recipeInfo.energy) * (heatMultiplier() + collectedHeatMultiplier() - 1));
        return energyPerTick;
    }

    private int heatMultiplier() {
        return 0;
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo.isCompleted()) {
            if(recipe == null) {
                recipe = (RECIPE) recipeInfo.recipe();
            }
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
                if(contentHandler.itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
                    recipe = null;
                }
            } else {
                recipeInfo.stuck = true;
            }
            setChanged();
        }
    }

    private int collectedHeatMultiplier() {
        return 0;
    }

    private void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = (int) (((RECIPE) recipeInfo.recipe()).getTimeModifier()*100);
            recipeInfo.energy = recipeInfo.recipe.getEnergy();
            recipeInfo.heat = ((RECIPE) recipeInfo.recipe()).getHeat();
            recipeInfo.radiation = recipeInfo.recipe().getRadiation();
            recipeInfo.be = this;
            recipe.extractInputs(contentHandler);
        }
    }

    private void addToCache(RECIPE recipe) {
        String key = contentHandler.getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }
    public RECIPE getRecipe() {
        if(contentHandler.itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) return null;
        RECIPE cachedRecipe = getCachedRecipe();
        if(cachedRecipe != null) return cachedRecipe;
        if(!NcRecipeType.ALL_RECIPES.containsKey(getName())) return null;
        for(AbstractRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
            if(recipe.test(contentHandler)) {
                addToCache((RECIPE)recipe);
                return (RECIPE)recipe;
            }
        }
        return null;
    }

    public RECIPE getCachedRecipe() {
        String key = contentHandler.getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            if(cachedRecipes.get(key).test(contentHandler)) {
                return cachedRecipes.get(key);
            }
        }
        return null;
    }

    public boolean recipeIsStuck() {
        return recipeInfo.isStuck();
    }

    private void handleMeltdown() {
    }

    public boolean hasRedstoneSignal() {
        return inputRedstoneSignal > 0;
    }

    //todo implement
    public double getNetHeat() {
        return 0;
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
    }

    public double getRecipeProgress() {
        return 0;
    }

    public void disableForceShutdown() {
    }

    public void forceShutdown() {
    }

    public void voidFuel() {
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if (!isCasingValid || !isInternalValid) {
                if(tag.contains("erroredBlock")) {
                    errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
                }
                validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            } else {
                validationResult = ValidationResult.VALID;
            }
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Energy", energyStorage.serializeNBT());
        tag.put("Content", contentHandler.serializeNBT());
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("validationId", validationResult.id);
        infoTag.putLong("erroredBlock", errorBlockPos.asLong());
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    private void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            energyStorage.setEnergy(infoTag.getInt("energy"));
            readTagData(infoTag);
            if (!isCasingValid || !isInternalValid) {
                errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
                validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            } else {
                validationResult = ValidationResult.VALID;
            }
            if (tag.contains("Content")) {
                contentHandler.deserializeNBT(tag.getCompound("Content"));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    private void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        saveTagData(infoTag);
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("validationId", validationResult.id);
        infoTag.putLong("erroredBlock", errorBlockPos.asLong());
        tag.put("Content", contentHandler.serializeNBT());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        int oldEnergy = energyStorage.getEnergyStored();

        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);

        if (oldEnergy != energyStorage.getEnergyStored()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void invalidateCache()
    {
        super.invalidateCache();
        isCasingValid = false;
        isInternalValid = false;
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler.fluidCapability.tanks.get(i);
    }


    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double radiation, double temperature) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, radiation, temperature);
        }

        @Override
        public @NotNull String getGroup() {
            return FusionReactor.FUSION_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FusionReactor.FUSION_BLOCKS.get(codeId).get());
        }

        public double getEnergy() {
            return powerModifier;
        }

        public double getHeat() {
            return powerModifier;
        }

        public double getRadiation() {
            return radiationModifier;
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            super.write(buffer);
            buffer.writeDouble(getOptimalTemperature());//in our case we use that as temperature
        }

        public double getOptimalTemperature() {
            return rarityModifier;
        }
    }
}
