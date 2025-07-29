package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.SolidFissionReactorPeripheral;
import igentuman.nc.compat.oc2.FissionReactorDevice;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.NcRecipeType;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
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
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.compat.oc2.FissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.heatsinks;
import static igentuman.nc.setup.registration.FissionFuel.ITEM_PROPERTIES;
import static igentuman.nc.setup.registration.NCSounds.FISSION_REACTOR;
import static igentuman.nc.setup.registration.NcParticleTypes.RADIATION;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.item.Items.AIR;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.*;

public class FissionControllerBE extends MultiblockControllerBE {

    public static final String NAME = "fission_reactor_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public double maxHeat = FISSION_CONFIG.HEAT_CAPACITY.getDefault();
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
    public int irradiationLines = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public double moderationLevel = 1D;
    @NBTField
    public boolean powered = false;
    @NBTField
    public double steamRate;
    @NBTField
    public int steamPerTick = 0;
    @NBTField
    public int extraFuelCells = 0;
    protected boolean forceShutdown = false;
    public int fuelCellMultiplier = 1;
    public int moderatorCellMultiplier = 1;
    public boolean controllerEnabled = false;
    private Direction facing;
    protected List<FissionBoilingRecipe> coolantRecipes;
    protected FissionBoilingRecipe boilingRecipe;
    private List<ItemStack> allowedInputs;
    @NBTField
    public boolean hasRedstoneSignal = false;
    private double envCooling = 0.0D;
    @NBTField
    public double boilingPenalty = 0;
    @NBTField
    public int connectedPorts = 0;
    @NBTField
    public int allModerators = 0;
    @NBTField
    public int allHeatSinks = 0;
    @NBTField
    public int activeCoolingHeatsinks = 0;
    @NBTField
    public int validIrradiators = 0;
    @NBTField
    public int allIrradiators = 0;
    @NBTField
    public double cellsHeatMult = 0;
    @NBTField
    public double moderatorsHeatMult = 0;
    @NBTField
    public double cellsEnergyMult = 0;
    @NBTField
    public double moderatorsEnergyMult = 0;
    private List<FluidStack>  allowedCoolants;
    private List<FluidStack>  allowedCoolantOutputs;

    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(),pPos, pBlockState);
        contentHandler = new SidedContentHandler(
                1, 1,
                1+activeCoolersTypes().size(), 1);
        contentHandler().setBlockEntity(this);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().fluidHandler.tanks.get(0).setCapacity(10000);
        contentHandler().fluidHandler.tanks.get(1).setCapacity(10000);
        contentHandler().setAllowedInputFluids(0, this::getAllowedCoolants);
        contentHandler().setAllowedInputFluids(1, this::getAllowedCoolantsOutput);
        for(String type: activeCoolersTypes()) {
            contentHandler().setAllowedInputFluids(
                    2+activeCoolersTypes().indexOf(type),
                    () -> heatsinks.get(type).getAllowedFluids()
                );
            contentHandler().fluidHandler.setGlobalMode(2+activeCoolersTypes().indexOf(type), SlotModePair.SlotMode.PULL);
        }
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        energyStorage = createEnergy();
        energyStorage
                .setInputEnergyTier(GTCEU_CONFIG.FISSION_REACTOR_TIER.get().ordinal()+ upgrade_tier)
                .setOutputEnergyTier(GTCEU_CONFIG.FISSION_REACTOR_TIER.get().ordinal()+ upgrade_tier)
                .setInputAmperage(0)
                .setOutputAmperage(16);
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
        for(String name: heatsinks.keySet()) {
            if(name.contains("active") && !name.contains("empty")) {
                types.add(name.replace("active_", ""));
            }
        }
        return types;
    }

    protected List<FluidStack> getAllowedCoolantsOutput() {
        if(allowedCoolantOutputs == null) {
            allowedCoolantOutputs = new ArrayList<>();
            for(FissionBoilingRecipe recipe : getBoilingRecipes()) {
                allowedCoolantOutputs.addAll(recipe.getOutputFluids(0));
            }
        }
        return allowedCoolantOutputs;
    }

    protected List<FluidStack> getAllowedCoolants() {
        if(!isSteamMode) return List.of();
        if(allowedCoolants == null) {
            allowedCoolants = new ArrayList<>();
            for (FissionBoilingRecipe recipe : getBoilingRecipes()) {
                allowedCoolants.addAll(recipe.getInputFluids(0));
            }
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
        boilingPenalty = 0;
        if(!isProcessing()) return;
        double cooling = coolingPerTick();
        if(getNetHeat() < 0) {
            cooling = heatPerTick;
        }
        double heatEff =  cooling * FISSION_CONFIG.BOILING_MULTIPLIER.get()/100D * heatMultiplier;

        if(hasCoolant()) {
            FluidStack steam = boilingRecipe.getOutputFluids().get(0);
            FluidStack coolant = boilingRecipe.getInputFluids(0).get(0);
            double conversion = heatEff/boilingRecipe.conversionRate();
            FluidStack currentCoolant = contentHandler().fluidHandler.getFluidInSlot(0);
            FluidStack currentOutput = contentHandler().fluidHandler.getFluidInSlot(1);
            if(!steam.isFluidEqual(currentOutput) && !currentOutput.isEmpty()) {
                boilingPenalty = coolingPerTick();
                return;
            }
            double capacity = contentHandler().fluidHandler.tanks.get(1).getCapacity() - currentOutput.getAmount();
            maxSteamOutput = (int) (steam.getAmount()*conversion);
            int ops = (int) (capacity/steam.getAmount());
            capacity = ops*steam.getAmount();
            int canGetAmount = (int) Math.min(maxSteamOutput, capacity);
            ops = canGetAmount/steam.getAmount();
            ops = Math.min(currentCoolant.getAmount()/coolant.getAmount(), ops);
            steamPerTick = Math.max(ops*steam.getAmount(), 0);
            if(steamPerTick == 0) {
                heat += heatPerTick;
                boilingPenalty = coolingPerTick()*0.75;
                return;
            }
            contentHandler().fluidHandler.tanks.get(0).drain(ops*coolant.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            FluidStack out = steam.copy();
            out.setAmount(ops*steam.getAmount());

            contentHandler().fluidHandler.tanks.get(1).fill(out, IFluidHandler.FluidAction.EXECUTE);
            changed = true;
            if(ops < Math.floor(conversion)) {
                boilingPenalty = coolingPerTick()*(conversion/ops)-coolingPerTick();
            }
        } else {
            boilingPenalty = coolingPerTick()*0.75;
        }
    }
    public void toggleMode() {
        if(!FISSION_CONFIG.BOILING_ENABLED.get()) {
            isSteamMode = false;
            return;
        }
        toggleModeTimer = 200;
    }

    private LazyOptional<SolidFissionReactorPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new SolidFissionReactorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> FissionReactorDevice.createDevice(this)).cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_HANDLER) {
            return contentHandler().getItemCapability(side);
        }
        if (cap == FLUID_HANDLER && canAcceptFluid()) {
            return contentHandler().getFluidCapability(side);
        }

        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled() && !isSteamMode && side == null) {
                    return getGTEnergy(this, side).cast();
                }
            }
        }
        if (cap == ENERGY && !isSteamMode && side == null) {
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
        if(isMekanismLoaded() && isSteamMode) {
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
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            BlockOverlayHandler.reactors.remove(this);
            stopSound();
            return;
        }
        if(efficiency > 0) {
            if(!BlockOverlayHandler.reactors.contains(this)) {
                BlockOverlayHandler.reactors.add(this);
            }
            spawnParticles();
            playSound(FISSION_REACTOR, 0.2f);
        } else {
            stopSound();
        }
    }

    public void tickServer() {
        heatMultiplier = 0;
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            irradiationHeat = 0;
            controllerEnabled = false;
            return;
        }
        changed = false;
        super.tickServer();
        boilingPenalty = 0;
        hopToggleMode();
        boolean wasFormed = getMultiblock().isFormed();
        boolean wasEnabled = controllerEnabled;
        boolean wasPowered = powered;

        handleValidation();

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
        irradiationHeat = 0;
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.1.reactor_all_moderators", String.valueOf(allModerators));
        report.put("report.nc.2.reactor_moderators", String.valueOf(moderatorsCount));
        report.put("report.nc.3.reactor_moderator_attachments", String.valueOf(moderatorAttachments));
        report.put("report.nc.4.reactor_all_heat_sinks", String.valueOf(allHeatSinks));
        report.put("report.nc.5.reactor_heat_sinks", String.valueOf(heatSinksCount));
        report.put("report.nc.6.active_cooling_heatsinks", String.valueOf(activeCoolingHeatsinks));
        report.put("report.nc.7.all_irradiators", String.valueOf(allIrradiators));
        report.put("report.nc.8.irradiators", String.valueOf(validIrradiators));
        report.put("report.nc.9.ports", String.valueOf(connectedPorts));
        report.put("report.nc.10.reactor_fuel_cells", String.valueOf(fuelCellsCount));
        report.put("report.nc.11.has_recipe", String.valueOf(recipeInfo().recipe != null));
        return report;
    }

    protected void handleValidation() {
        super.handleValidation();
    }

    private void hopToggleMode() {
        if(!FISSION_CONFIG.BOILING_ENABLED.get()) {
            isSteamMode = false;
            return;
        }
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

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {
        if (heat > getMaxHeat()) {
            BlockPos explosionPos = getBlockPos().relative(getFacing(), 2);
            List<Long> fuelCells = new ArrayList<>(getMultiblock().fuelCells);
            if (FISSION_CONFIG.EXPLOSION_RADIUS.get() == 0) {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 2F, Level.ExplosionInteraction.NONE);
            } else {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), FISSION_CONFIG.EXPLOSION_RADIUS.get().floatValue(), Level.ExplosionInteraction.TNT);
                getLevel().setBlock(explosionPos, NCFluids.getBlock("corium"), 1);
                for (long packedPos : fuelCells) {
                    BlockPos pos = BlockPos.of(packedPos);
                    getLevel().explode(null, pos.getX(), pos.getY(), pos.getZ(), 2, Level.ExplosionInteraction.TNT);
                    getLevel().setBlock(pos, NCFluids.getBlock("corium"), 1);
                }
            }

            RadiationManager.get(getLevel()).addRadiation(getLevel(), 100000*fuelCellsCount, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
            setRemoved();
        }
    }

    private void tickActiveHeatSinks() {
        double calculatedCooling = 0;
        for(String coolant: getMultiblock().coolantPerTick.keySet()) {
            int amount = getMultiblock().coolantPerTick.get(coolant);
            if(amount == 0) {
                continue;
            }
            if(!hasEnoughCoolant(coolant, amount)) {
                calculatedCooling -= getCoolingByCoolant(coolant, amount);
                continue;
            }
            if (heat > 0 || isProcessing()) {
                drainCoolant(coolant, amount);
            }
        }
        if (calculatedCooling != activeCooling) {
            activeCooling = calculatedCooling;
            setChanged();
        }
    }

    private double getCoolingByCoolant(String coolant, int amount) {
        if(!FissionReactorRegistration.heatsinks.containsKey("active_"+coolant)) {
            return 0;
        }
        int mbPerTick = FISSION_CONFIG.ACTIVE_HEATSINK_COOLANT_PER_TICK.get();
        FissionReactorRegistration.heatsinks.get("active_"+coolant);
        return ((double)amount /(double)mbPerTick)*FissionReactorRegistration.heatsinks.get("active_"+coolant).heat;
    }

    private boolean coolDown() {
        tickActiveHeatSinks();
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
        reactivityLevel += controllerEnabled ? 1 : -1;
        reactivityLevel = Math.max(0, Math.min(reactivityLevel, 100));
        if(recipeInfo().be == null) {
            recipeInfo().be = this;
        }
        recipeInfo().process(fuelCellsCount * (heatMultiplier() + collectedHeatMultiplier() - 1) * reactivityLevel/100D);
        if(recipeInfo().radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo().radiation/10000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        if (!recipeInfo().isCompleted()) {
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

    public double heatMultiplier() {
        if(heatMultiplier == 0) {
            double h = heatPerTick();
            double c = Math.max(1, coolingPerTick());
            heatMultiplier = Math.log10(h / c) / (1 + Math.exp(h / c * FISSION_CONFIG.HEAT_MULTIPLIER.get())) + 1;
            //round heatMultiplier to 2 digits
            heatMultiplier = Math.round(heatMultiplier * 100.0) / 100.0;
        }
        return heatMultiplier;
    }

    public double collectedHeatMultiplier() {
        return Math.min(FISSION_CONFIG.HEAT_MULTIPLIER_CAP.get(), Math.pow((heat + getMaxHeat() / 8) / getMaxHeat(), 5) + 0.9999694824);
    }

    public double coolingPerTick() {
        return heatSinksCooling() + environmentCooling() - boilingPenalty;
    }

    public double environmentCooling() {
        if(envCooling == 0.0D) {
            envCooling = 1 / Math.max(getLevel().getBiome(getBlockPos()).get().getBaseTemperature(), 0.01);
        }
        return envCooling;
    }

    public double heatSinksCooling() {
        heatSinkCooling = getMultiblock().countCooling(refreshCacheFlag);
        return heatSinkCooling+activeCooling;
    }

    public double heatPerTick() {
        heatPerTick = recipeInfo().heat * (cellsHeatMult + moderatorsHeat()) + irradiationHeat;
        return heatPerTick;
    }

    private double calculateHeat() {
        return heatPerTick() * Math.max(0.5D, reactivityLevel / 100D);
    }

    private int calculateEnergy() {
        energyPerTick = (int) (
                (recipeInfo().energy * (cellsEnergyMult + moderatorsFE()))
                * (heatMultiplier() + collectedHeatMultiplier() - 1)
                * FISSION_CONFIG.FE_GENERATION_MULTIPLIER.get()/10D
                * ENERGY_GENERATION.GENERATION_MULTIPLIER.get()
                * reactivityLevel / 100D
        );
        return energyPerTick;
    }

    public double moderatorsHeat() {
        return Math.max(0.1, getModerationLevel())*moderatorsHeatMult;
    }

    public double moderatorsFE() {
        return getModerationLevel() * moderatorsEnergyMult;
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
            recipeInfo().ticks = ((Recipe)recipeInfo().recipe()).getDepletionTime();
            recipeInfo().energy = recipeInfo().recipe().getEnergy();
            recipeInfo().heat = ((Recipe)recipeInfo().recipe()).getHeat();
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

    public double getDepletionProgress() {
        return recipeInfo().getProgress();
    }

    public double getMaxHeat() {
        return maxHeat;
    }

    public double boilingEfficiency() {
        double mult = fuelCellsCount;
        if(fuelCellMultiplier > fuelCellsCount) {
            mult = (double) fuelCellMultiplier / fuelCellsCount;
        }
        return heatPerTick / (recipeInfo.heat * mult / 100);
    }

    public double calculateEfficiency() {
        double mult = fuelCellsCount;
        if(extraFuelCells > fuelCellsCount) {
            mult = (double) extraFuelCells / fuelCellsCount;
        }
        return (double) energyPerTick / (recipeInfo.energy * mult / 100);
    }

    public double getNetHeat() {
        return heatPerTick - heatSinksCooling();
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

    public List<FissionBoilingRecipe> getBoilingRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<FissionBoilingRecipe>) NcRecipeType.getAllRecipesFor("fission_boiling", getLevel());
        }
        return coolantRecipes;
    }

    public boolean hasCoolant() {
        FluidStack coolant = contentHandler().fluidHandler.getFluidInSlot(0);
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
        return hasRecipe() && recipeInfo().ticksProcessed > 0 && !recipeInfo().isCompleted();
    }

    public void addIrradiationHeat() {
        irradiationHeat += irradiationLines * 15;
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
        return Math.round(moderationLevel * 100) / 100.0;
    }

    public void adjustModerationLevel(int level) {
        String formatted = String.format(Locale.US,"%.2f", (double) Math.max(1, level) / 100);
        targetModerationLevel = Double.parseDouble(formatted);
    }

    public boolean canAcceptFluid() {
        return isSteamMode || getMultiblock().coolantPerTick.size() > 0;
    }

    public boolean hasEnoughCoolant(String coolant, int amount) {
        for(int i = 2; i < contentHandler().fluidHandler.tanks.size(); i++) {
            FluidStack stack = contentHandler().fluidHandler.tanks.get(i).getFluid();
            if(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).getPath().equals(coolant) && stack.getAmount() >= amount) {
                return true;
            }
        }
        return false;
    }

    public void drainCoolant(String coolant, int amount) {
        for(int i = 2; i < contentHandler().fluidHandler.tanks.size(); i++) {
            FluidStack stack = contentHandler().fluidHandler.tanks.get(i).getFluid();
            if(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).getPath().equals(coolant) && stack.getAmount() >= amount) {
                contentHandler().fluidHandler.tanks.get(i).drain(amount, IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    public AABB getGlowAABB() {
        if (bottomLeft.equals(BlockPos.ZERO) || topRight.equals(BlockPos.ZERO)) {
            return new AABB(0, 0, 0, 0, 0, 0);
        }
        Vec3 topRightInner = new Vec3(topRight.getX(), topRight.getY(), topRight.getZ());
        topRightInner = topRightInner.relative(getFacing(), 0.05D).relative(UP, 0.05D).relative(getFacing().getClockWise(),-0.05D);
        Vec3 bottomLeftInner = new Vec3(bottomLeft.getX(), bottomLeft.getY(), bottomLeft.getZ());
        bottomLeftInner = bottomLeftInner.relative(getFacing(), 0.95D).relative(UP, 0.95D).relative(getFacing().getCounterClockWise(),0.95D);
        return new AABB(bottomLeftInner, topRightInner);
    }

    public void refresh() {
        double multiplier = Math.max(1, ((double) Math.round(Math.log(height*width*depth)*10)/10)-1);
        maxHeat = FISSION_CONFIG.HEAT_CAPACITY.get()*multiplier;
        contentHandler().fluidHandler.tanks.get(0).setCapacity((int) (Math.pow(multiplier, 2)*1_000_000));
        contentHandler().fluidHandler.tanks.get(1).setCapacity((int) (Math.pow(multiplier, 2)*1_000_000));
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
