package igentuman.nc.block.entity.accelerator;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.entity.kugelblitz.BlackHoleBE;
import igentuman.nc.compat.cc.KugelblitzPeripheral;
import igentuman.nc.compat.cc.LinearAcceleratorPeripheral;
import igentuman.nc.compat.oc2.KugelblitzDevice;
import igentuman.nc.compat.oc2.LinearAcceleratorDevice;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorMultiblock;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
import java.util.*;

import static igentuman.nc.block.entity.kugelblitz.BlackHoleBE.MAX_MASS;
import static igentuman.nc.block.entity.kugelblitz.BlackHoleBE.MIN_MASS;
import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.oc2.FissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.materials.Materials.subliquid_matter;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BE;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.setup.registration.GameEvents.BLACKHOLE_VIBRATION;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static net.minecraft.world.level.block.Blocks.AIR;

public class LinearAcceleratorControllerBE extends MultiblockControllerBE {

    public static String NAME = "linear_accelerator_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    private LazyOptional<LinearAcceleratorPeripheral> peripheralCap;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public boolean controllerEnabled = false;

    protected Direction facing;
    public Recipe recipe;
    public HashMap<String, Recipe> cachedRecipes = new HashMap<>();
    private List<ItemStack> allowedInputs;
    private final List<ItemStack> orderedOutputs = new ArrayList<>();
    private List<FluidStack> allowedInputFluids;

    public LinearAcceleratorControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 1, 10000);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler.fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        contentHandler.setBlockEntity(this);
        contentHandler.setAllowedInputFluids(0, this::getAllowedInputFluids);
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("linear_accelerator_controller", getLevel())) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    allowedInputs.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
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
        NcRecipe cachedRecipe = getCachedRecipe();
        if(cachedRecipe instanceof Recipe cRecipe) {
            return cRecipe;
        }
        if(!NcRecipeType.ALL_RECIPES.containsKey("accelerator")) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("accelerator", getLevel())) {
            if(recipe.test(contentHandler())) {
                addToCache(recipe);
                return (Recipe) recipe;
            }
        }
        return null;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new LinearAcceleratorPeripheral(this));
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
        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }


    public void tickClient() {
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            stopSound();
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
        controllerEnabled = getMultiblock().isFormed() && hasRedstoneSignal();

        if (controllerEnabled) {
            trackChanges(contentHandler().tick());
            handleMeltdown();
            trackChanges(processRecipe());
            handleRecipeOutput();
        }
        refreshCacheFlag = !getMultiblock().isFormed();
        if(wasEnabled != controllerEnabled) {
           setChanged();
        }
        if(refreshCacheFlag || changed) {
            try {
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, controllerEnabled), Block.UPDATE_NEIGHBORS);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, controllerEnabled));
            } catch (NullPointerException ignored) {}
        }
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputFluids == null) {
            allowedInputFluids = new ArrayList<>();
            allowedInputFluids.addAll(IngredientCreatorAccess.fluid().from(subliquid_matter, 1).getRepresentations());
        }
        return allowedInputFluids;
    }

    @Override
    public LinearAcceleratorMultiblock getMultiblock() {
        if(multiblock == null) {
            multiblock = new LinearAcceleratorMultiblock(this);
        }
        return (LinearAcceleratorMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {

    }

    private boolean processRecipe() {
        if(recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if(contentHandler().itemHandler.getStackInSlot(0).isEmpty()) {
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
        double multiplier = 1;
        recipeInfo().process(multiplier);
        return true;
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if(recipe == null) {
                recipe = (Recipe) recipeInfo().recipe();
            }
            int id = getIngredientId(recipe.getResultItem());
            if (recipe.handleOutputs(contentHandler(), orderedOutputs.get(id))) {
                recipeInfo().clear();
                if(contentHandler().itemHandler.getStackInSlot(0).isEmpty()) {
                    recipe = null;
                }
            } else {
                recipeInfo.stuck = true;
            }
            setChanged();
        }
    }

    private int getIngredientId(@NotNull ItemStack resultItem) {
        for(int i = 0; i < allowedInputs.size(); i++) {
            if(allowedInputs.get(i).is(resultItem.getItem())) {
                return i;
            }
        }
        return 0;
    }

    private void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe)recipeInfo().recipe()).getBaseTime();
            recipeInfo().energy = recipeInfo().recipe.getEnergy();
            recipeInfo().be = this;
            if (!recipeInfo().consumeInputs(contentHandler())) {
                recipe = null;
                recipeInfo().clear();
            }
        }
    }

    private int getTargetFrequencyForItem(ItemStack input, long seed) {
        Random rand = new Random(seed + input.getItem().toString().hashCode());
        return rand.nextInt(15);
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

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> LinearAcceleratorDevice.createDevice(this)).cast();
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
            return "accelerator";
        }

        @Override
        public @NotNull String getGroup() {
            return "accelerator";
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(ACCELERATOR_BLOCKS.get(NAME).get());
        }

        public int getBaseTime() {
            return (int) (timeModifier * 50);
        }

        public double getEnergy() { return powerModifier * 1000; }
    }
}
