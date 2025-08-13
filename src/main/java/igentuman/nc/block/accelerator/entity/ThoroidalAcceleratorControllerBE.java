package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.accelerator.AcceleratorPortBlock;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.ThoroidalAcceleratorPeripheral;
import igentuman.nc.compat.oc2.ThoroidalAcceleratorDevice;
import igentuman.nc.content.particles.*;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.multiblock.accelerator.ThoroidalAcceleratorMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.compat.oc2.ThoroidalAcceleratorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.materials.Materials.subliquid_matter;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.util.Equations.*;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class ThoroidalAcceleratorControllerBE extends MultiblockControllerBE {

    public static String NAME = "thoroidal_accelerator_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    private LazyOptional<ThoroidalAcceleratorPeripheral> peripheralCap;
    protected final LazyOptional<IEnergyStorage> energy;
    protected final LazyOptional<IParticleStackHandler> particleHandler;
    protected final ParticleStorage particleStorage;

    @NBTField
    public int heatMax = 0;
    @NBTField
    public BlockPos ionSourcePos = BlockPos.ZERO;
    @NBTField
    public boolean hasParticle = false;
    @NBTField
    public int coolers;
    @NBTField
    public int beamLength = 0;
    @NBTField
    public boolean controllerEnabled = false;
    @NBTField
    public int amplifiers = 0;
    @NBTField
    public int quadroupoles = 0;
    @NBTField
    public int dipoles = 0;
    @NBTField
    public double focus = 0;
    @NBTField
    public int maxTemperature = 0;
    @NBTField
    public int heatRate = 0;
    @NBTField
    public int heat = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public double quadStrength = 0;
    @NBTField
    public double dipoleStrength = 0;
    @NBTField
    public long acceleratingVoltage = 0;
    @NBTField
    public int energyRequired = 0;
    @NBTField
    public int coolingRate = 0;
    @NBTField
    public double redstoneLevel = 0;

    protected Direction facing;
    public LinearAcceleratorControllerBE.Recipe recipe;
    public HashMap<String, LinearAcceleratorControllerBE.Recipe> cachedRecipes = new HashMap<>();
    private List<ItemStack> allowedInputs;
    private List<FluidStack> allowedInputFluids;
    protected LinearAcceleratorControllerBE.CoolantRecipe coolantRecipe;
    protected List<LinearAcceleratorControllerBE.CoolantRecipe> coolantRecipes;
    private List<FluidStack> allowedCoolants;
    private List<FluidStack> allowedCoolantsOutput;


    public ThoroidalAcceleratorControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energyStorage
                .setInputEnergyTier(getBaseGTEnergyTier())
                .setOutputEnergyTier(0)
                .setInputAmperage(16)
                .setOutputAmperage(0);
        energy = LazyOptional.of(() -> energyStorage);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 3, 1000);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        // Particle source fluid input
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        // Product output
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.OUTPUT);
        // Coolant input
        contentHandler().fluidHandler.setGlobalMode(2, SlotModePair.SlotMode.INPUT);
        // Hot coolant output
        contentHandler().fluidHandler.setGlobalMode(3, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        contentHandler().setBlockEntity(this);
        contentHandler().setAllowedInputFluids(0, this::getAllowedInputFluids);
        contentHandler().setAllowedInputFluids(2, this::getAllowedCoolants);
        contentHandler().setAllowedInputFluids(3, this::getAllowedCoolantsOutput);
        contentHandler().fluidHandler.tanks.get(2).setCapacity(100000);
        contentHandler().fluidHandler.tanks.get(3).setCapacity(100000);
        particleStorage = new ParticleStorage();
        particleStorage.setTileEntity(this);
        particleHandler = CapabilityParticleStackHandler.createHandler(particleStorage);
    }

    @Override
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal();
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

    public List<LinearAcceleratorControllerBE.CoolantRecipe> getCoolantRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<LinearAcceleratorControllerBE.CoolantRecipe>) NcRecipeType.getAllRecipesFor("accelerator_coolant", getLevel());
        }
        return coolantRecipes;
    }

    protected List<FluidStack> getAllowedCoolants() {
        if(allowedCoolants == null) {
            allowedCoolants = new ArrayList<>();
            for(LinearAcceleratorControllerBE.CoolantRecipe recipe : getCoolantRecipes()) {
                allowedCoolants.addAll(recipe.getInputFluids(0));
            }
        }
        return allowedCoolants;
    }

    protected List<FluidStack> getAllowedCoolantsOutput() {
        if(allowedCoolantsOutput == null) {
            allowedCoolantsOutput = new ArrayList<>();
            for(LinearAcceleratorControllerBE.CoolantRecipe recipe : getCoolantRecipes()) {
                allowedCoolantsOutput.addAll(recipe.getOutputFluids(0));
            }
        }
        return allowedCoolantsOutput;
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
        return new CustomEnergyStorage(100000000, 100000000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @Override
    public LinearAcceleratorControllerBE.Recipe getRecipe() {
        if(contentHandler().itemHandler.getStackInSlot(0).isEmpty()) return null;
        NcRecipe cachedRecipe = getCachedRecipe();
        if(cachedRecipe instanceof LinearAcceleratorControllerBE.Recipe cRecipe) {
            return cRecipe;
        }
        if(!NcRecipeType.ALL_RECIPES.containsKey("accelerator")) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("accelerator", getLevel())) {
            if(recipe.test(contentHandler())) {
                addToCache(recipe);
                return (LinearAcceleratorControllerBE.Recipe) recipe;
            }
        }
        return null;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new ThoroidalAcceleratorPeripheral(this));
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
        if(redstoneLevel < 1) {
            redstoneLevel = getRedstoneSignal();
        }
        controllerEnabled = getMultiblock().isFormed() && redstoneLevel > 0;

        if (controllerEnabled) {
            trackChanges(contentHandler().tick());
            handleMeltdown();
            trackChanges(accelerateParticle());
        }
        // Passive cooling
        heat -= coolingRate;
        heat = Math.max(0, heat);
        // Coolant cooling
        coolantCoolDown();
        refreshCacheFlag = !getMultiblock().isFormed();
        if(wasEnabled != controllerEnabled) {
            setChanged();
        }
        if(refreshCacheFlag || changed || getLevel().getGameTime() % 20 == 0) {
            try {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(AcceleratorPortBlock.POWERED, controllerEnabled), Block.UPDATE_NEIGHBORS);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AcceleratorPortBlock.POWERED, controllerEnabled));
            } catch (NullPointerException ignored) {}
        }
        //particleStorage.clear();
    }

    private boolean accelerateParticle() {
        hasParticle = false;
        if(energyStorage().getEnergyStored() < energyRequired) {
            return false;
        }
        if(particleStorage.getParticle() == null) {
            getParticleFromIonSource();
        }
        if(particleStorage.getParticle() == null) {
            return false;
        }
        if(!drainEnergy()) {
            return false;
        }
        ParticleStack particleStack = particleStorage.getParticle();
        particleStack.addFocus(focusGain(focus, particleStack)-focusLoss(beamLength, particleStack));
        particleStack.setMeanEnergy((long)(linacEnergyGain(acceleratingVoltage, particleStack)*(redstoneLevel / 15d)));
        particleStorage.setParticleStack(particleStack);
        heat += heatRate;
        hasParticle = true;
        getMultiblock().extractParticle(particleStack);
        return true;
    }

    private boolean drainEnergy() {
        if(energyStorage().getEnergyStored() < energyRequired) {
            return false;
        }
        energyStorage().extractEnergy(energyRequired, false);
        return true;
    }

    private void getParticleFromIonSource() {
        ItemStack stack = contentHandler().itemHandler.getStackInSlot(0);
        if(stack.getItem() instanceof ParticleSourceItem sourceItem) {
            stack = sourceItem.use(stack, 10000);
            ParticleStack particle = sourceItem.getParticleStack(stack);
            if (particle != null) {
                particle.addFocus(0.4);
                particle.setAmount(10000);
                particleStorage.setParticleStack(particle);
                contentHandler().itemHandler.setStackInSlot(0, stack);
            }
        } else {
            FluidStack fluidStack = contentHandler().fluidHandler.getFluidInSlot(0);
            if (fluidStack != null && !fluidStack.isEmpty()) {
                ParticleStack particle = ParticleSources.getParticleFromFluid(fluidStack);
                if (particle != null) {
                    particle.addFocus(0.4);
                    particle.setAmount(10000);
                    particleStorage.setParticleStack(particle);
                    contentHandler().fluidHandler.tanks.get(0).drain(1, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

    }

    public boolean hasCoolant() {
        FluidStack coolant = contentHandler().fluidHandler.getFluidInSlot(2);
        if(coolant.isEmpty()) {
            coolantRecipe = null;
            return false;
        }
        if(coolantRecipe == null) {
            for(LinearAcceleratorControllerBE.CoolantRecipe recipe: getCoolantRecipes()) {
                if(recipe.getInputFluids()[0].test(coolant)) {
                    coolantRecipe = recipe;
                    return true;
                }
            }
        } else {
            if(!coolantRecipe.getInputFluids()[0].test(coolant)) {
                coolantRecipe = null;
                return false;
            }
        }
        return coolantRecipe instanceof LinearAcceleratorControllerBE.CoolantRecipe;
    }

    protected void coolantCoolDown() {
        if(hasCoolant() && heat > 0) {
            double coolantNeededRatio = (double) coolingRate / coolantRecipe.getCoolingRate();
            int coolantPerOp = coolantRecipe.getInputFluids()[0].getAmount();
            int coolantNeeded = (int) Math.ceil(coolantNeededRatio * coolantPerOp);

            int availableCoolant = contentHandler().fluidHandler.tanks.get(2).getFluidAmount();

            if(availableCoolant >= coolantNeeded) {
                // We have enough coolant to provide full cooling
                int opsNeeded = Math.max(1, coolantNeeded / coolantPerOp);
                double actualCooling = Math.min(coolingRate, heat);

                heat -= (int) actualCooling;
                heat = Math.max(0, heat);

                extractCoolant(opsNeeded);
            } else if(availableCoolant >= coolantPerOp) {
                // We have some coolant but not enough for full cooling
                int possibleOps = availableCoolant / coolantPerOp;
                double partialCooling = (possibleOps * coolantPerOp * coolantRecipe.getCoolingRate()) / coolantPerOp;
                double actualCooling = Math.min(partialCooling, heat);

                heat -= (int) actualCooling;
                heat = Math.max(0, heat);

                extractCoolant(possibleOps);
            }
        }
    }

    protected void extractCoolant(int ops) {
        if(coolantRecipe != null) {
            contentHandler().fluidHandler.tanks.get(2).drain(coolantRecipe.getInputFluids()[0].getAmount() * ops, EXECUTE);
            FluidStack output = coolantRecipe.getOutputFluids().get(0).copy();
            output.setAmount(output.getAmount() * ops);
            contentHandler().fluidHandler.tanks.get(3).fill(output, EXECUTE);
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
    public ThoroidalAcceleratorMultiblock getMultiblock() {
        if(multiblock == null) {
            multiblock = new ThoroidalAcceleratorMultiblock(this);
        }
        return (ThoroidalAcceleratorMultiblock) multiblock;
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
            recipeInfo().ticks = ((LinearAcceleratorControllerBE.Recipe)recipeInfo().recipe()).getBaseTime();
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
        return LazyOptional.of(() -> ThoroidalAcceleratorDevice.createDevice(this)).cast();
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidHandler.tanks.get(i);
    }
}
