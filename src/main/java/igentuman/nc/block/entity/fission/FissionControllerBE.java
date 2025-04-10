package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.NCSolidFissionReactorPeripheral;
import igentuman.nc.compat.oc2.NCFissionReactorDevice;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.*;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.oc2.NCFissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.FissionFuel.ITEM_PROPERTIES;
import static igentuman.nc.setup.registration.NCSounds.FISSION_REACTOR;
import static igentuman.nc.setup.registration.NcParticleTypes.RADIATION;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraft.world.item.Items.AIR;

public class FissionControllerBE extends MultiblockControllerBE {

    public static String NAME = "fission_reactor_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public boolean isSteamMode = false;
    @NBTField
    public double heat = 0;
    @NBTField
    public int maxSteamOutput = 0;
    @NBTField
    public int fuelCellsCount = 0;
    @NBTField
    public int reactivityLevel = 0;
    @NBTField
    public int irradiationHeat = 0;
    @NBTField
    public int moderatorsCount = 0;
    @NBTField
    public int heatSinksCount = 0;
    @NBTField
    public int moderatorAttachments = 0;
    @NBTField
    public int toggleModeTimer = 2000;
    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public double heatSinkCooling = 0;
    @NBTField
    public double activeCooling = 0;
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
    public double moderationLevel = 1D;
    @NBTField
    public boolean powered = false;
    @NBTField
    private double steamRate;
    @NBTField
    public int steamPerTick = 0;
    protected boolean forceShutdown = false;
    public int fuelCellMultiplier = 1;
    public int moderatorCellMultiplier = 1;
    public boolean controllerEnabled = false;
    private Direction facing;
    protected List<FissionBoilingRecipe> coolantRecipes;
    protected FissionBoilingRecipe boilingRecipe;
    private List<ItemStack> allowedInputs;

    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactor.FISSION_BE.get(NAME).get(),pPos, pBlockState);
        multiblock = new FissionReactorMultiblock(this);
        contentHandler = new SidedContentHandler(
                1, 1,
                1+activeCoolersTypes().size(), 1);
        contentHandler().setBlockEntity(this);
        contentHandler().fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().fluidCapability.tanks.get(0).setCapacity(10000);
        contentHandler().fluidCapability.tanks.get(1).setCapacity(10000);
        contentHandler().setAllowedInputFluids(0, this::getAllowedCoolants);
        contentHandler().setAllowedInputFluids(1, this::getAllowedCoolantsOutput);
        for(String type: activeCoolersTypes()) {
            contentHandler().setAllowedInputFluids(
                    1+activeCoolersTypes().indexOf(type),
                    () -> FissionBlocks.heatsinks.get(type).getAllowedFluids()
                );
            contentHandler().fluidCapability.setGlobalMode(2+activeCoolersTypes().indexOf(type), SlotModePair.SlotMode.PULL);
        }
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        energyStorage = createEnergy();
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

    private List<String> activeCoolersTypes() {
        List<String> types = new ArrayList<>();
        for(String name: FissionBlocks.heatsinks.keySet()) {
            if(name.contains("active") && !name.contains("empty")) {
                types.add(name.replace("active_", ""));
            }
        }
        return types;
    }

    protected List<FluidStack> getAllowedCoolantsOutput() {
        List<FluidStack> allowedCoolants = new ArrayList<>();
        for(FissionBoilingRecipe recipe : getBoilingRecipes()) {
            allowedCoolants.addAll(recipe.getOutputFluids(0));
        }
        return allowedCoolants;
    }

    protected List<FluidStack> getAllowedCoolants() {
        List<FluidStack> allowedCoolants = new ArrayList<>();
        for(FissionBoilingRecipe recipe : getBoilingRecipes()) {
            allowedCoolants.addAll(recipe.getInputFluids(0));
        }
        return allowedCoolants;
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

    public double getSteamRate()
    {
        return Math.max(0, steamRate);
    }

    public void boil()
    {
        steamPerTick = 0;
        if(!isProcessing()) return;
        double cooling = coolingPerTick();
        if(getNetHeat() < 0) {
            cooling = heatPerTick;
        }
        double heatEff =  cooling * FISSION_CONFIG.BOILING_MULTIPLIER.get() * efficiency * 0.00005D * heatMultiplier;

        if(hasCoolant()) {
            FluidStack steam = boilingRecipe.getOutputFluids().get(0);
            FluidStack coolant = boilingRecipe.getInputFluids(0).get(0);
            double conversion = heatEff/boilingRecipe.conversionRate();
            FluidStack currentCoolant = contentHandler().fluidCapability.getFluidInSlot(0);
            FluidStack currentOutput = contentHandler().fluidCapability.getFluidInSlot(1);
            if(!steam.isFluidEqual(currentOutput) && !currentOutput.isEmpty()) {
                //No room? Heat up
                heat += coolingPerTick()/2;
                return;
            }
            double capacity = contentHandler().fluidCapability.tanks.get(1).getCapacity() - currentOutput.getAmount();
            maxSteamOutput = (int) (steam.getAmount()*conversion);
            int ops = (int) (capacity/steam.getAmount());
            capacity = ops*steam.getAmount();
            int canGetAmount = (int) Math.min(maxSteamOutput, capacity);
            ops = canGetAmount/steam.getAmount();
            ops = Math.min(currentCoolant.getAmount()/coolant.getAmount(), ops);
            steamPerTick = Math.max(ops*steam.getAmount(), 0);
            if(steamPerTick == 0) {
                heat += coolingPerTick()/2;
                return;
            }
            contentHandler().fluidCapability.tanks.get(0).drain(ops*coolant.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            FluidStack out = steam.copy();
            out.setAmount(ops*steam.getAmount());

            contentHandler().fluidCapability.tanks.get(1).fill(out, IFluidHandler.FluidAction.EXECUTE);
            changed = true;
            if(ops < Math.floor(conversion)) {
                heat += coolingPerTick()/(conversion - ops);
            }
        }
    }
    public void toggleMode() {
        toggleModeTimer = 200;
    }

    private LazyOptional<NCSolidFissionReactorPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCSolidFissionReactorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> NCFissionReactorDevice.createDevice(this)).cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return contentHandler().getItemCapability(side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && canAcceptFluid()) {
            return contentHandler().getFluidCapability(side);
        }
        if (cap == ForgeCapabilities.ENERGY && !isSteamMode && side == null) {
            return energy.cast();
        }
        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
            }
        }
        if(isMekanismLoadeed() && isSteamMode) {
            if(cap == mekanism.common.capabilities.Capabilities.GAS_HANDLER) {
                if(contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler().gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == mekanism.common.capabilities.Capabilities.SLURRY_HANDLER) {
                if(contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler().getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }

        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    public void tickClient() {
        if(!isCasingValid || !isInternalValid) {
            stopSound();
            return;
        }
        if(isProcessing() && powered) {
            spawnParticles();
            playSound(FISSION_REACTOR, 0.2f);
        } else {
            stopSound();
        }
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, isProcessing() && powered));
    }

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            irradiationHeat = 0;
            controllerEnabled = false;
            return;
        }
        changed = false;
        hopToggleMode();
        boolean wasFormed = getMultiblock().isFormed();
        super.tickServer();
        boolean wasPowered = powered;
        handleValidation();
        trackChanges(wasPowered, powered);
        boolean wasEnabled = controllerEnabled;
        controllerEnabled = hasRedstoneSignal() && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        //do not allow change reactor state during cooldown or heating up
        if(controllerEnabled != wasEnabled && reactivityLevel > 10 && reactivityLevel < 99) {
            controllerEnabled = wasEnabled;
        }
        if (getMultiblock().isFormed()) {
            trackChanges(updateModerationLevel());
            trackChanges(contentHandler().tick());
            if(controllerEnabled || reactivityLevel > 0) {
                powered = processReaction();
                trackChanges(powered);
            } else {
                powered = false;
            }
            trackChanges(coolDown());
            handleMeltdown();

        } else {
            //if reactor was broken during processing, contaminate area
            if(isProcessing() && wasFormed) {
                RadiationManager.get(getLevel()).addRadiation(getLevel(), 10000*fuelCellsCount, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            }
        }
        refreshCacheFlag = !getMultiblock().isFormed();
        if(refreshCacheFlag || changed) {
            try {
                assert level != null;
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {}
        }
        irradiationHeat = 0;


    }

    private void hopToggleMode() {
        if(toggleModeTimer < 201) {
            toggleModeTimer--;
            changed = true;
            if (toggleModeTimer < 1) {
                toggleModeTimer = 2000;
                isSteamMode = !isSteamMode;
            }
        }
    }

    @Override
    public FissionReactorMultiblock getMultiblock() {
        if(multiblock == null) {
            multiblock = new FissionReactorMultiblock(this);
        }
        return (FissionReactorMultiblock) multiblock;
    }

    private int delay = 20;

    private void handleValidation() {
        boolean wasFormed = getMultiblock().isFormed();
        boolean assembled = wasFormed && isInternalValid && isCasingValid;
        if (
                (!assembled && getLevel().getGameTime() % delay == 0)
                || (assembled && getLevel().getGameTime() % (delay* 8L) == 0 && hasRecipe())
        ) {
            Random rand = new Random(getBlockPos().asLong());
            delay = rand.nextInt(80) + 80;

            getMultiblock().validate();
            isCasingValid = getMultiblock().isOuterValid();
            if(isCasingValid) {
                isInternalValid = getMultiblock().isInnerValid();
            }
            powered = false;
            changed = true;
            height = getMultiblock().height();
            width = getMultiblock().width();
            depth = getMultiblock().depth();
            if(
                    getMultiblock().isFormed()
                    && contentHandler().fluidCapability.tanks.get(0).getCapacity() != 5000*height*width*depth
            ) {
                contentHandler().fluidCapability.tanks.get(0).setCapacity(5000*height*width*depth);
                contentHandler().fluidCapability.tanks.get(1).setCapacity(5000*height*width*depth);
            }
        }

        trackChanges(wasFormed, getMultiblock().isFormed());
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {
        if (heat >= getMaxHeat()) {
            BlockPos explosionPos = getBlockPos().relative(getFacing(), 2);
            List<BlockPos> fuelCells = new ArrayList<>(getMultiblock().fuelCells);
            if (FISSION_CONFIG.EXPLOSION_RADIUS.get() == 0) {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 2F, Level.ExplosionInteraction.NONE);
            } else {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), FISSION_CONFIG.EXPLOSION_RADIUS.get().floatValue(), Level.ExplosionInteraction.TNT);
                getLevel().setBlock(explosionPos, NCFluids.getBlock("corium"), 1);
                for (BlockPos pos : fuelCells) {
                    getLevel().explode(null, pos.getX(), pos.getY(), pos.getZ(), 2, Level.ExplosionInteraction.TNT);
                    getLevel().setBlock(pos, NCFluids.getBlock("corium"), 1);
                }
            }

            //1 mRad per fuel cell
            RadiationManager.get(getLevel()).addRadiation(getLevel(), 100000*fuelCellsCount, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            setRemoved();
            //at any case if reactor still works we punish player
            //heat = getMaxHeat();
            //energyStorage.setEnergy((int) (energyStorage.getEnergyStored() - calculateEnergy()));
        }

    }

    public void setRemoved() {
        super.setRemoved();
        if(getLevel().isClientSide()) {
            return;
        }
        if(getMultiblock() != null) {
            getMultiblock().onControllerRemoved();
        }
    }

    private boolean coolDown() {
        double wasHeat = heat;
        heat -= coolingPerTick();
        if(isSteamMode) {
            boil();
        }
        heat = Math.max(0, heat);
        return wasHeat != heat;
    }

    public float speed = 0.001f;

    private boolean processReaction() {
        heatMultiplier = heatMultiplier() + collectedHeatMultiplier() - 1;
        if(recipeInfo.recipe != null && recipeInfo.isCompleted()) {
            if(contentHandler().itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
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

    private void spawnParticles() {
        if(getMultiblock() == null) {
            return;
        }
        if(!getMultiblock().isFormed()) {
            getMultiblock().validate();
        }
        if(level.getGameTime()  % (level.random.nextInt(10)+5) != 0) {
            return;
        }
        BlockPos topBlock = getMultiblock().getTopRightInnerBlock();
        BlockPos bottomLeft = getMultiblock().getBottomLeftInnerBlock();

        for(BlockPos blockPos: BlockPos.betweenClosed(bottomLeft, topBlock)) {
            if(level.random.nextBoolean()) {
                level.addParticle(RADIATION.get(), blockPos.getX()+level.random.nextFloat(), blockPos.getY()+level.random.nextFloat(), blockPos.getZ()+level.random.nextFloat(), 0, -0.05f, 0);
            }
        }
    }

    private boolean process() {
        reactivityLevel += controllerEnabled ? 1 : -1;
        reactivityLevel = Math.max(0, Math.min(reactivityLevel, 100));
        if(recipeInfo.be == null) {
            recipeInfo.be = this;
        }
        recipeInfo.process(fuelCellsCount * (heatMultiplier() + collectedHeatMultiplier() - 1) * reactivityLevel/100D);
        if(recipeInfo.radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo.radiation/10000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        if (!recipeInfo.isCompleted()) {
            if(!isSteamMode) {
                energyStorage.addEnergy(calculateEnergy());
            }
            heat += calculateHeat();
        }

        handleRecipeOutput();

        efficiency = calculateEfficiency();
        return true;
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo.isCompleted()) {
            if(recipe == null) {
                recipe = (Recipe) recipeInfo.recipe();
            }
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
                if(contentHandler().itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
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
        return 1 / Math.max(getLevel().getBiome(getBlockPos()).get().getBaseTemperature(), 0.01);
    }

    public double heatSinksCooling() {
        heatSinkCooling = getMultiblock().countCooling(refreshCacheFlag);
        return heatSinkCooling+activeCooling;
    }

    public double heatPerTick() {
        heatPerTick = recipeInfo.heat * Math.max(fuelCellsCount, fuelCellMultiplier) + moderatorsHeat() + irradiationHeat;
        return heatPerTick;
    }

    private double calculateHeat() {
        return heatPerTick() * Math.max(0.5D, reactivityLevel / 100D);
    }

    private int calculateEnergy() {
        energyPerTick = (int) (
                (recipeInfo.energy * Math.abs(fuelCellMultiplier-fuelCellsCount) + moderatorsFE())
                * (heatMultiplier() + collectedHeatMultiplier() - 1)
                * FISSION_CONFIG.FE_GENERATION_MULTIPLIER.get()/10D
                * ENERGY_GENERATION.GENERATION_MULTIPLIER.get()
                * reactivityLevel / 100D
        );
        return energyPerTick;
    }

    public double moderatorsHeat() {
        return Math.max(0.25, getModerationLevel()) * recipeInfo.heat * moderatorCellMultiplier * (FISSION_CONFIG.MODERATOR_HEAT_MULTIPLIER.get() / 100);
    }

    public double moderatorsFE() {
        return getModerationLevel() * recipeInfo.energy * moderatorCellMultiplier * (FISSION_CONFIG.MODERATOR_FE_MULTIPLIER.get() / 100);
    }

    @Override
    public Recipe getRecipe() {
        if(contentHandler().itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) return null;
        return (Recipe) super.getRecipe();
    }

    protected void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = ((Recipe)recipeInfo.recipe()).getDepletionTime();
            recipeInfo.energy = recipeInfo.recipe().getEnergy();
            recipeInfo.heat = ((Recipe)recipeInfo.recipe()).getHeat();
            recipeInfo.radiation = recipeInfo.recipe().getRadiation();
            recipeInfo.be = this;
            recipe.consumeInputs(contentHandler, 1);
        }
    }

    public boolean recipeIsStuck() {
        return recipeInfo.isStuck();
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
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
        return heatPerTick - heatSinksCooling();
    }

    public boolean hasRedstoneSignal() {
        return enabledByController || Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
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
        return recipeInfo.recipe().getFirstItemStackIngredient(0);
    }

    public List<FissionBoilingRecipe> getBoilingRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<FissionBoilingRecipe>) NcRecipeType.getAllRecipesFor("fission_boiling", getLevel());
        }
        return coolantRecipes;
    }

    public boolean hasCoolant() {
        FluidStack coolant = contentHandler().fluidCapability.getFluidInSlot(0);
        if(coolant.isEmpty()) {
            boilingRecipe = null;
            return false;
        }
        if(boilingRecipe == null) {
            for(FissionBoilingRecipe recipe: getBoilingRecipes()) {
                if(recipe.getInputFluids()[0].test(coolant)) {
                    boilingRecipe = recipe;
                    return true;
                }
            }
        } else {
            if(!boilingRecipe.getInputFluids()[0].test(coolant)) {
                boilingRecipe = null;
                return false;
            }
        }
        return boilingRecipe instanceof FissionBoilingRecipe;
    }

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo.ticksProcessed > 0 && !recipeInfo.isCompleted();
    }

    public void addIrradiationHeat() {
        irradiationHeat += irradiationConnections * 15;
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

    private double targetModerationLevel = 1D;

    public void adjustModerator(int redstoneSignal) {
        BigDecimal bd = BigDecimal.valueOf((double) redstoneSignal / 15);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        targetModerationLevel = bd.doubleValue();
    }

    /**
     * Slowly update the moderation level
     * @return boolean
     */
    public boolean updateModerationLevel()
    {
        if(Math.abs(moderationLevel - targetModerationLevel) > 0.005) {
            if(moderationLevel < targetModerationLevel) {
                moderationLevel += 0.0025;
            } else {
                moderationLevel -= 0.0025;
            }
            return true;
        }
        return false;
    }

    public double getModerationLevel() {
        if(moderatorsCount == 0) return 1D;
        BigDecimal bd = BigDecimal.valueOf(moderationLevel);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void adjustModerationLevel(int level) {
        String formatted = String.format(Locale.US,"%.2f", (double) Math.max(1, level) / 100);
        targetModerationLevel = Double.parseDouble(formatted);
    }

    public boolean canAcceptFluid() {
        return isSteamMode || getMultiblock().coolantPerTick.size() > 0;
    }

    public boolean hasEnoughCoolant(String coolant, int amount) {
        for(int i = 0; i < contentHandler().fluidCapability.tanks.size(); i++) {
            FluidStack stack = contentHandler().fluidCapability.tanks.get(i).getFluid();
            if(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).getPath().equals(coolant) && stack.getAmount() >= amount) {
                return true;
            }
        }
        return false;
    }

    public void drainCoolant(String coolant, int amount) {
        for(int i = 0; i < contentHandler().fluidCapability.tanks.size(); i++) {
            FluidStack stack = contentHandler().fluidCapability.tanks.get(i).getFluid();
            if(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).getPath().equals(coolant) && stack.getAmount() >= amount) {
                contentHandler().fluidCapability.tanks.get(i).drain(amount, IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
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

        protected ItemFuel fuelItem;

        public ItemFuel getFuelItem() {
            if(fuelItem == null) {
                Item item = getFirstItemStackIngredient(0).getItem();
                if( !(item instanceof ItemFuel) && !item.equals(AIR)) {
                    fuelItem = new ItemFuel(ITEM_PROPERTIES, item.toString(), "", "");
                    return fuelItem;
                }
                Item item1 = getFirstItemStackIngredient(0).getItem();
                if(item1 instanceof ItemFuel) {
                    fuelItem  = (ItemFuel) item1;
                }
            }
            if(fuelItem.def == null) {
                fuelItem.initDefinition();
            }
            return fuelItem;
        }

        @Override
        public @NotNull String getGroup() {
            return FISSION_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FISSION_BLOCKS.get(codeId).get());
        }

        public int getDepletionTime() {
            if(getFuelItem() == null) return 0;
            return (int) (getFuelItem().depletion()*20*timeModifier);
        }

        public double getEnergy() {
            if(getFuelItem() == null) return 0;
            return getFuelItem().forge_energy;
        }

        public double getHeat() {
            if(getFuelItem() == null) return 0;
            return getFuelItem().heat;
        }

        public double getRadiation() {
            return ItemRadiation.byItem(getFuelItem())/20;
        }
    }

    public static class FissionBoilingRecipe extends NcRecipe {
        protected double conversionRate;

        public FissionBoilingRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output,
                                    FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                                    double conversionRate, double powerModifier, double radiation, double rar) {
            super(id, input, output, inputFluids, outputFluids, conversionRate, powerModifier, radiation, rar);
            this.conversionRate = conversionRate;
        }

        @Override
        public @NotNull String getGroup() {
            return "fission_boiling";
        }

        @Override
        public String getCodeId() {
            return "fission_boiling";
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FISSION_BLOCKS.get(NAME).get());
        }

        public double conversionRate() {
            return Math.max(conversionRate, 1);
        }
    }
}
