package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.cc.NCSolidFissionReactorPeripheral;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.*;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.CommonConfig.FISSION_CONFIG;
import static igentuman.nc.util.ModUtil.isCcLoaded;

public class FissionControllerBE <RECIPE extends FissionControllerBE.Recipe> extends FissionBE  {

    public static String NAME = "fission_reactor_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage = createEnergy();

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;
    @NBTField
    public double heat = 0;
    @NBTField
    public int fuelCellsCount = 0;

    @NBTField
    public int irradiationHeat = 0;
    @NBTField
    public int moderatorsCount = 0;
    @NBTField
    public int heatSinksCount = 0;
    @NBTField
    public int moderatorAttacmentsCount = 0;
    @NBTField
    public boolean isCasingValid = false;
    @NBTField
    public boolean isInternalValid = false;
    @NBTField
    public int height = 1;
    @NBTField
    public int width = 1;
    @NBTField
    public int depth = 1;
    @NBTField
    public double heatSinkCooling = 0;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double heatMultiplier = 0;
    @NBTField
    public int irradiationConnections = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    protected boolean forceShutdown = false;
    public int fuelCellMultiplier = 1;
    public int moderatorCellMultiplier = 1;

    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo recipeInfo = new RecipeInfo();
    public boolean controllerEnabled = false;
    private Direction facing;
    public RECIPE recipe;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    @Override
    public String getName() {
        return NAME;
    }

    private List<ItemStack> allowedInputs;

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(AbstractRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    allowedInputs.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
    }

    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
        multiblock = new FissionReactorMultiblock(this);
        contentHandler = new SidedContentHandler(
                1, 1,
                0, 0);
        contentHandler.setBlockEntity(this);
    }

    @Override
    public ItemCapabilityHandler getItemInventory()
    {
        return contentHandler.itemHandler;
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
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

    public void updateEnergyStorage() {
        energyStorage.setMaxCapacity(Math.max(fuelCellsCount, 1) * 1000000);
        energyStorage.setMaxExtract(Math.max(fuelCellsCount, 1) * 1000000);
    }

    private LazyOptional<NCSolidFissionReactorPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCSolidFissionReactorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return contentHandler.getItemCapability(side);
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

    public void tickClient() {
    }
    protected int reValidateCounter = 0;



    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) {
            irradiationHeat = 0;
            return;
        }
        changed = false;
        super.tickServer();
        boolean wasPowered = powered;
        handleValidation();
        trackChanges(wasPowered, powered);
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && multiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;

        if (multiblock().isFormed()) {
            trackChanges(contentHandler.tick());
            if(controllerEnabled) {
                powered = processReaction();
                trackChanges(powered);
            } else {
                powered = false;
            }
            trackChanges(coolDown());
            handleMeltdown();
            contentHandler.setAllowedInputItems(getAllowedInputItems());
        }
        refreshCacheFlag = !multiblock().isFormed();
        if(refreshCacheFlag || changed) {
            try {
                assert level != null;
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {}
        }
        irradiationHeat = 0;

        controllerEnabled = false;
    }

    private void handleValidation() {
        multiblock().tick();
        boolean wasFormed = multiblock().isFormed();
        if (!wasFormed) {
            reValidateCounter++;
            if(reValidateCounter < 40) {
                return;
            }
            reValidateCounter = 0;
            multiblock().validate();
            isCasingValid = multiblock().isOuterValid();
            if(isCasingValid) {
                isInternalValid = multiblock().isInnerValid();
            }
            powered = false;
            changed = true;
        }
        height = multiblock().height();
        width = multiblock().width();
        depth = multiblock().depth();
        trackChanges(wasFormed, multiblock().isFormed());
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {
        if (heat >= getMaxHeat()) {
            BlockPos explosionPos = getBlockPos().relative(getFacing(), 2);
            if (FISSION_CONFIG.EXPLOSION_RADIUS.get() == 0) {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 2F, true, Explosion.BlockInteraction.NONE);
                for (BlockPos pos : multiblock.fuelCells) {
                    getLevel().explode(null, pos.getX(), pos.getY(), pos.getZ(), 1, true, Explosion.BlockInteraction.NONE);
                }
            } else {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), FISSION_CONFIG.EXPLOSION_RADIUS.get().floatValue(), true, Explosion.BlockInteraction.DESTROY);
                for (BlockPos pos : multiblock.fuelCells) {
                    getLevel().explode(null, pos.getX(), pos.getY(), pos.getZ(), 2, true, Explosion.BlockInteraction.DESTROY);
                }
            }
            for (BlockPos pos : multiblock.fuelCells) {
                getLevel().setBlock(pos, NCFluids.getBlock("corium"), 1);
            }
            getLevel().setBlock(getBlockPos(), NCFluids.getBlock("corium"), 1);

            //1 mRad per fuel cell
            RadiationManager.get(getLevel()).addRadiation(getLevel(), 1000000*fuelCellsCount, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            setRemoved();
            //at any case if reactor still works we punish player
            //heat = getMaxHeat();
            //energyStorage.setEnergy((int) (energyStorage.getEnergyStored() - calculateEnergy()));
        }

    }

    public void setRemoved() {
        super.setRemoved();
        if(multiblock() != null) {
            multiblock().onControllerRemoved();
        }
    }

    private boolean coolDown() {
        double wasHeat = heat;
        heat -= coolingPerTick();
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
        recipeInfo.process(fuelCellsCount * (heatMultiplier() + collectedHeatMultiplier() - 1));
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

    public double heatMultiplier() {
        double h = heatPerTick();
        double c = Math.max(1, coolingPerTick());
        return Math.log10(h / c) / (1 + Math.exp(h / c * FISSION_CONFIG.HEAT_MULTIPLIER.get())) + 1;
    }

    public double collectedHeatMultiplier() {
        return Math.min(FISSION_CONFIG.HEAT_MULTIPLIER_CAP.get(), Math.pow((heat + getMaxHeat() / 8) / getMaxHeat(), 5) + 0.9999694824);
    }

    public double coolingPerTick() {
        return heatSinksCooling() + environmentCooling();
    }

    public double environmentCooling() {
        return getLevel().getBiome(getBlockPos()).get().getBaseTemperature() * 10;
    }


    public double heatSinksCooling() {
        heatSinkCooling = multiblock().getHeatSinkCooling(refreshCacheFlag);
        return heatSinkCooling;
    }

    public double heatPerTick() {
        heatPerTick = recipeInfo.heat * Math.max(fuelCellsCount, fuelCellMultiplier) + moderatorsHeat() + irradiationHeat;
        return heatPerTick;
    }

    private double calculateHeat() {
        return heatPerTick();
    }

    private int calculateEnergy() {
        energyPerTick = (int) ((recipeInfo.energy * Math.abs(fuelCellMultiplier-fuelCellsCount) + moderatorsFE()) * (heatMultiplier() + collectedHeatMultiplier() - 1));
        return energyPerTick;
    }

    public double moderatorsHeat() {
        return recipeInfo.heat * moderatorCellMultiplier * (FISSION_CONFIG.MODERATOR_HEAT_MULTIPLIER.get() / 100);
    }

    public double moderatorsFE() {
        return recipeInfo.energy * moderatorCellMultiplier * (FISSION_CONFIG.MODERATOR_FE_MULTIPLIER.get() / 100);
    }


    private void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = ((RECIPE) recipeInfo.recipe()).getDepletionTime();
            recipeInfo.energy = recipeInfo.recipe.getEnergy();
            recipeInfo.heat = ((RECIPE) recipeInfo.recipe()).getHeat();
            recipeInfo.radiation = recipeInfo.recipe().getRadiation();
            recipeInfo.be = this;
            recipe.consumeInputs(contentHandler);
        }
    }

    public boolean recipeIsStuck() {
        return recipeInfo.isStuck();
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
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
                errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
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

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
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

    public double getDepletionProgress() {
        return recipeInfo.getProgress();
    }

    public double getMaxHeat() {
        return FISSION_CONFIG.HEAT_CAPACITY.get();
    }

    public double calculateEfficiency() {
        double mult = fuelCellsCount;
        if(fuelCellMultiplier > fuelCellsCount) {
            mult = (double) fuelCellMultiplier / fuelCellsCount;
        }
        return (double) calculateEnergy() / (recipeInfo.energy * mult / 100);
    }

    public double getNetHeat() {
        return heatPerTick - heatSinkCooling;
    }

    public int getDepth() {
        return depth;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    public Object[] getFuel() {
        if(hasRecipe()) {
            return contentHandler.getSlotContent(0);
        }
        return new Object[]{};
    }

    public void voidFuel() {
        contentHandler.voidSlot(0);
        contentHandler.itemHandler.holdedInputs.clear();
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    public ItemStack getCurrentFuel() {
        if(!hasRecipe()) return ItemStack.EMPTY;
        return recipeInfo.recipe().getFirstItemStackIngredient(0);
    }

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo.ticksProcessed > 0 && !recipeInfo.isCompleted();
    }

    public void addIrradiationHeat() {
        irradiationHeat += irradiationConnections * 15;
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(codeId, List.of(getToastSymbol()));
        }

        protected ItemFuel fuelItem;

        public ItemFuel getFuelItem() {
            if(fuelItem == null) {
                fuelItem = (ItemFuel) getFirstItemStackIngredient(0).getItem();
            }
            return fuelItem;
        }

        @Override
        public @NotNull String getGroup() {
            return FissionReactor.MULTI_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FissionReactor.MULTI_BLOCKS.get(codeId).get());
        }

        public int getDepletionTime() {
            return (int) (getFuelItem().depletion*20*timeModifier);
        }

        public double getEnergy() {
            return getFuelItem().forge_energy;
        }

        public double getHeat() {
            return getFuelItem().heat;
        }

        public double getRadiation() {
            return ItemRadiation.byItem(getFuelItem())/10;
        }
    }
}
