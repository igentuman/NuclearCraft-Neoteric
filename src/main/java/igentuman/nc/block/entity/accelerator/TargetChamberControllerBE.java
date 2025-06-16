package igentuman.nc.block.entity.accelerator;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.TargetChamberPeripheral;
import igentuman.nc.compat.oc2.TargetChamberDevice;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.accelerator.TargetChamberMultiblock;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import static igentuman.nc.block.accelerator.TargetChamberControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.compat.oc2.FissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static igentuman.nc.setup.registration.NCSounds.FISSION_REACTOR;
import static igentuman.nc.setup.registration.NcParticleTypes.RADIATION;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class TargetChamberControllerBE extends MultiblockControllerBE {

    public static final String NAME = "target_chamber_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

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

    public TargetChamberControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(),pPos, pBlockState);
        contentHandler = new SidedContentHandler(
                1, 1,
                0, 0);
        contentHandler().setBlockEntity(this);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        energyStorage = createEnergy();
        energyStorage
                .setInputEnergyTier(GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal())
                .setOutputEnergyTier(0)
                .setInputAmperage(16)
                .setOutputAmperage(0);
        energy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    allowedInputs.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
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
        return new CustomEnergyStorage(100000000, 0, 100000000) {
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

        controllerEnabled = hasRedstoneSignal() && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        if(controllerEnabled != wasEnabled) {
            controllerEnabled = wasEnabled;
        }
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
        if(refreshCacheFlag || changed || getLevel().getGameTime() % 40 == 0) {
            try {
                assert level != null;
                setChanged();
                if(powered != wasPowered) {
                    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);

            } catch (NullPointerException ignored) {}
        }
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.1.target_chamber.all_detectors", String.valueOf(allDetectors));
        report.put("report.nc.2.target_chamber.valid_detectors", String.valueOf(detectorsCount));
        report.put("report.nc.11.has_recipe", String.valueOf(recipeInfo().recipe != null));
        return report;
    }

    protected void handleValidation() {
        super.handleValidation();
    }

    @Override
    public TargetChamberMultiblock getMultiblock() {
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

    private void spawnParticles() {
        if(getMultiblock() == null || efficiency <= 0) {
            return;
        }

        if(level.getGameTime()  % (level.random.nextInt(5)+1) != 0) {
            return;
        }
        BlockPos topRightInner = topRight.relative(getFacing(), -1).below().relative(getFacing().getClockWise(),1);
        BlockPos bottomLeftInner = bottomLeft.relative(getFacing(), 1).above().relative(getFacing().getCounterClockWise(),1);
        int minX = Math.min(topRightInner.getX(), bottomLeftInner.getX());
        int minY = Math.min(topRightInner.getY(), bottomLeftInner.getY());
        int minZ = Math.min(topRightInner.getZ(), bottomLeftInner.getZ());
        int maxX = Math.max(topRightInner.getX(), bottomLeftInner.getX());
        int maxY = Math.max(topRightInner.getY(), bottomLeftInner.getY());
        int maxZ = Math.max(topRightInner.getZ(), bottomLeftInner.getZ());
        for(BlockPos blockPos: BlockPos.randomBetweenClosed(level.random, width+height+depth, minX, minY, minZ, maxX, maxY, maxZ)) {
            level.addParticle(RADIATION.get(), true, blockPos.getX()+level.random.nextFloat(), blockPos.getY()+level.random.nextFloat(), blockPos.getZ()+level.random.nextFloat(), 0, -0.05f, 0);
        }
    }

    private boolean process() {

        if(recipeInfo().be == null) {
            recipeInfo().be = this;
        }
        recipeInfo().process(1);
        if(recipeInfo().radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo().radiation/10000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }

        handleRecipeOutput();

        efficiency = calculateEfficiency();
        return true;
    }

    private double calculateEfficiency() {
        return 0;
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
        return (Recipe) super.getRecipe();
    }

    protected void updateRecipe() {
        //check if last recipe is still valid
        if(recipe != null) {
            if(recipe.test(contentHandler())) {
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
            recipeInfo().ticks = (int) ((Recipe)recipeInfo().recipe()).getTimeModifier();
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
        if(getLevel().getGameTime() % 10 == 0) {
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

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(codeId, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return NAME;
        }


        @Override
        public @NotNull String getGroup() {
            return TARGET_CHAMBER_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(TARGET_CHAMBER_BLOCKS.get(codeId).get());
        }
    }
}
