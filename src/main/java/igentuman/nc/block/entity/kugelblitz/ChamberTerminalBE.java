package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.KugelblitzPeripheral;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.block.entity.kugelblitz.BlackHoleBE.MAX_MASS;
import static igentuman.nc.block.entity.kugelblitz.BlackHoleBE.MIN_MASS;
import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.content.materials.Materials.subliquid_matter;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;

public class ChamberTerminalBE extends MultiblockControllerBE {

    public static String NAME = "chamber_terminal";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    private LazyOptional<KugelblitzPeripheral> peripheralCap;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public long feeding = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public long mass = 0;
    @NBTField
    public int evaporation = 0;
    @NBTField
    public byte frequency = 0;
    @NBTField
    public int energyConvertionRate = 50;
    @NBTField
    public boolean controllerEnabled = false;


    protected Direction facing;
    public Recipe recipe;
    public HashMap<String, Recipe> cachedRecipes = new HashMap<>();
    private List<FluidStack> allowedInputs;

    public ChamberTerminalBE(BlockPos pPos, BlockState pBlockState) {
        super(KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
        multiblock = new KugelblitzMultiblock(this);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 0, 1000);
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler.setBlockEntity(this);
        contentHandler.setAllowedInputFluids(0, this::getAllowedInputFluids);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
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
    public Recipe getRecipe() {
        if(contentHandler().itemHandler.getStackInSlot(0).isEmpty()) return null;
        //TODO implement
        return null;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new KugelblitzPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(null);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return getEnergy().cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void handleSliderUpdate(int buttonId, int ratio) {
        switch(buttonId) {
            case 0 -> {
                energyConvertionRate = ratio;
            }
            case 1 -> {
                frequency = (byte) ratio;
            }
        }
        setChanged();
    }

    public void tickClient() {
        if(!isCasingValid || !isInternalValid) {
            stopSound();
            return;
        }
        if(energyPerTick > 0) {
            //spawnSteamParticles();
        }
    }
    protected int reValidateCounter = 0;

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            return;
        }
        changed = false;
        super.tickServer();
        boolean wasEnabled = controllerEnabled;
        handleValidation();
        controllerEnabled = getMultiblock().isFormed() && hasBlackhole();

        if (controllerEnabled) {
            trackChanges(contentHandler.tick());
            long wasMass = mass;
            updateBlackholeMass();
            trackChanges(false, wasMass != mass);
            handleMeltdown();
        }
        refreshCacheFlag = !getMultiblock().isFormed();
        if(wasEnabled != controllerEnabled) {
           setChanged();
        }
        if(refreshCacheFlag || changed) {
            try {
                MultiblockHandler.addIgnoreToUpdate(getBlockPos());
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, controllerEnabled), Block.UPDATE_ALL);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, controllerEnabled));
            } catch (NullPointerException ignored) {}
        }
    }

    public boolean hasBlackhole() {
        return getMultiblock().getBlackHole() instanceof BlackHoleBE;
    }

    private void updateBlackholeMass()
    {
        if (!hasBlackhole()) {
            mass = 0;
            evaporation = 0;
            energyPerTick = 0;
            return;
        }
        feeding = contentHandler().fluidCapability.getFluidInSlot(0).getAmount() * 10L;
        mass += feeding;
        contentHandler.fluidCapability.voidSlot(0);
        updateEnergyGeneration();
        updateEvaporation();
        mass -= evaporation;
        if (mass < MIN_MASS) {
            doEvaporation();
        }
    }

    private void updateEnergyGeneration() {
        int wasEnergy = energyPerTick;
        energyPerTick = (int)  (mass * 0.00005D * Math.log(energyConvertionRate+1));
        energyPerTick *= ENERGY_GENERATION.GENERATION_MULTIPLIER.get();
        energyPerTick *= KUGELBLITZ_CONFIG.GENERATION_MULTIPLIER.get();
        energyStorage().addEnergy(energyPerTick);
        if (wasEnergy != energyPerTick) {
            setChanged();
        }
    }

    private void updateEvaporation() {
        int wasEvaporation = evaporation;
        int rate = Math.max(1, energyConvertionRate);
        if (recipeInfo().recipe() != null && !recipeInfo().isCompleted()) {
            rate = 100;
        }
        rate = (int) Math.pow(rate, 1.2);
        evaporation = (int) (rate * KUGELBLITZ_CONFIG.EVAPORATION_MULTIPLIER.get() * (mass * 0.0000001D));
        if (wasEvaporation != evaporation) {
            setChanged();
        }
    }

    private void doEvaporation() {
        
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            allowedInputs.addAll(IngredientCreatorAccess.fluid().from(subliquid_matter, 1).getRepresentations());
        }
        return allowedInputs;
    }

    @Override
    public KugelblitzMultiblock getMultiblock() {
        if(multiblock == null) {
            multiblock = new KugelblitzMultiblock(this);
        }
        return (KugelblitzMultiblock) multiblock;
    }

    private void handleValidation() {
        if(multiblock == null) return;
        ValidationResult wasResult = validationResult;
        boolean wasFormed = this.getMultiblock().isFormed();
        if (!wasFormed || !isInternalValid || !isCasingValid) {
            reValidateCounter++;
            if(reValidateCounter < 40) {
                return;
            }
            reValidateCounter = 0;
            this.getMultiblock().validate();
            isCasingValid = this.getMultiblock().isOuterValid();
            if(isCasingValid) {
                isInternalValid = this.getMultiblock().isInnerValid();
            }
            changed = true;
        }
        validationResult = this.getMultiblock().validationResult;
        if(validationResult.id != wasResult.id) {
            changed = true;
        }

        height = this.getMultiblock().height();
        width = this.getMultiblock().width();
        depth = this.getMultiblock().depth();
        trackChanges(wasFormed, this.getMultiblock().isFormed());
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {
        if(mass > MAX_MASS) {
            if (getMultiblock().isFormed() && getMultiblock().getBlackHole() != null) {
                getMultiblock().getBlackHole().meltdown();
            }
        }
    }

    private boolean processRecipe() {
        if(recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if(contentHandler().fluidCapability.getFluidInSlot(0).isEmpty()) {
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
        recipeInfo().process(1);

        return true;
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if(recipe == null) {
                recipe = (Recipe) recipeInfo().recipe();
            }
            if (recipe.handleOutputs(contentHandler())) {
                recipeInfo().clear();
                if(contentHandler().fluidCapability.getFluidInSlot(0).isEmpty()) {
                    recipe = null;
                }
            } else {
                recipeInfo.stuck = true;
            }
            setChanged();
        }
    }

    private int calculateEnergy() {
        int wasEnergy = energyPerTick;
        energyPerTick = 0;
        if(wasEnergy != energyPerTick) {
            changed = true;
        }
        return energyPerTick;
    }

    private void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe)recipeInfo().recipe()).getBaseTime();
            recipeInfo().energy = recipeInfo().recipe.getEnergy();
            recipeInfo().be = this;
            //recipe.consumeInputs(contentHandler);
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

    public double calculateEfficiency() {
        return (double) energyPerTick / (recipeInfo().energy / 100);
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

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo().ticksProcessed > 0 && !recipeInfo.isCompleted();
    }

    @Override
    public ItemCapabilityHandler getItemInventory()
    {
        return contentHandler().itemHandler;
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidCapability.tanks.get(i);
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return NAME;
        }

        @Override
        public @NotNull String getGroup() {
            return NAME;
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(KugelblitzRegistration.KUGELBLITZ_BLOCKS.get(getCodeId()).get());
        }

        public int getBaseTime() {
            return (int) Math.max(1, timeModifier);
        }

        public double getEnergy() { return Math.max(1, powerModifier); }

    }
}
