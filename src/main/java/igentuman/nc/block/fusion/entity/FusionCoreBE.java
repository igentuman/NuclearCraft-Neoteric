package igentuman.nc.block.fusion.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.compat.cc.FusionReactorPeripheral;
import igentuman.nc.compat.oc2.FusionReactorDevice;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import igentuman.nc.multiblock.fusion.FusionReactorMultiblock;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.compat.oc2.FusionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.handler.config.FusionConfig.FUSION_CONFIG;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BE;
import static igentuman.nc.setup.registration.NCSounds.*;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class FusionCoreBE extends MultiblockControllerBE {


    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte redstoneMode = SignalSource.HEAT;
    @NBTField
    public double reactorHeat = 0;
    @NBTField
    public int size = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    public int inputRedstoneSignal = 0;
    @NBTField
    public int currentRfAmplification = 0;
    @NBTField
    public int amplifiers = 0;
    @NBTField
    protected boolean forceShutdown = false;
    @NBTField
    public double magneticFieldStrength = 0;
    @NBTField
    public int magnets = 0;
    @NBTField
    public int connectors = 0;
    @NBTField
    public int casingBlocks = 0;
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
    @NBTField
    public int rfAmplificationRatio = 0;
    @NBTField
    public int amplificationAdjustment = 50;
    @NBTField
    public int functionalBlocksCharge = 0;
    @NBTField
    public long plasmaTemperature = 0;
    public long chargeAmount = 0;
    @NBTField
    public int rfEfficiency = 0;
    @NBTField
    public int magnetsEfficiency = 0;
    @NBTField
    protected double lastKnownOptimalTemp = 1000000;

    protected FusionCoolantRecipe coolantRecipe;
    protected final LazyOptional<IEnergyStorage> energy;
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected LazyOptional<FusionReactorPeripheral> peripheralCap;
    protected List<FusionCoolantRecipe> coolantRecipes;
    public boolean controllerEnabled = false;
    public Recipe recipe;
    public HashMap<String, Recipe> cachedRecipes = new HashMap<>();
    protected boolean initialized = false;
    protected boolean refreshCacheFlag;
    protected List<FluidStack> allowedInputs;
    protected FusionCoreProxyBE[] proxyBES;

    public FusionCoreBE(BlockPos pPos, BlockState pBlockState) {
        super(FUSION_BE.get("fusion_core").get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energyStorage.setInputEnergyTier(getBaseGTEnergyTier())
                .setOutputEnergyTier(getBaseGTEnergyTier())
                .setInputAmperage(4)
                .setOutputAmperage(16);
        energy = LazyOptional.of(() -> energyStorage);
        contentHandler = new SidedContentHandler(
                0, 0,
                3, 5, 10, 50);
        contentHandler().setBlockEntity(this);
        //fuel
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.INPUT);
        //coolant
        contentHandler().fluidHandler.setGlobalMode(2, SlotModePair.SlotMode.INPUT);

        //products
        contentHandler().fluidHandler.setGlobalMode(3, SlotModePair.SlotMode.OUTPUT);
        contentHandler().fluidHandler.setGlobalMode(4, SlotModePair.SlotMode.OUTPUT);
        contentHandler().fluidHandler.setGlobalMode(5, SlotModePair.SlotMode.OUTPUT);
        contentHandler().fluidHandler.setGlobalMode(6, SlotModePair.SlotMode.OUTPUT);
        //hot coolant
        contentHandler().fluidHandler.setGlobalMode(7, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setAllowedInputFluids(0, this::getAllowedInputFluids);
        contentHandler().setAllowedInputFluids(1, this::getAllowedInputFluids);
        contentHandler().setAllowedInputFluids(2, this::getAllowedCoolants);
        contentHandler().setAllowedInputFluids(7, this::getAllowedCoolantsOutput);
        contentHandler().fluidHandler.tanks.get(2).setCapacity(100000);
        contentHandler().fluidHandler.tanks.get(7).setCapacity(100000);
    }

    @Override
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.FUSION_REACTOR_ENERGY_TIER.get().ordinal();
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(2_048_000_000, 1000000, 100000000) {
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

    @Override
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    private void initMultiblock() {
        if(multiblock == null) {
            multiblock = new FusionReactorMultiblock(this);
        }
    }

    public void toggleRedstoneMode() {
        redstoneMode++;
        if(redstoneMode > SignalSource.EFFICIENCY) {
            redstoneMode = SignalSource.ENERGY;
        }
        MultiblockHandler.get(getLevel().dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
    }

    public List<FusionCoolantRecipe> getCoolantRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<FusionCoolantRecipe>) NcRecipeType.getAllRecipesFor("fusion_coolant", getLevel());
        }
        return coolantRecipes;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new FusionReactorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(this, side).cast();
                }
            }
        }
        if (cap == ForgeCapabilities.ENERGY) {
            if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
                return LazyOptional.empty();
            }
            return getEnergy().cast();
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

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> FusionReactorDevice.createDevice(this)).cast();
    }

    public void updateAnalogSignal() {
        switch (redstoneMode) {
            case FusionCoreBE.SignalSource.ENERGY:
               analogSignal = (byte) ((double)energyStorage().getEnergyStored() * 15 / (double)energyStorage().getMaxEnergyStored());
                break;
            case FusionCoreBE.SignalSource.HEAT:
                analogSignal = (byte) (reactorHeat * 15 / getMaxHeat());
                break;
            case FusionCoreBE.SignalSource.EFFICIENCY:
                analogSignal = (byte) (efficiency * 15 );
                break;
        }
    }

    @Override
    public void onLoad() {
        initialized = false;
    }

    public double getMaxHeat() {
        return Math.max(minRFAmplifiersTemp*2, maxMagnetsTemp*2);
    }

    public void handleValidation()
    {
        super.handleValidation();
        /*if(getMultiblock().isFormed() != wasFormed) {
            contentHandler().fluidHandler.tanks.get(2).setCapacity(50000*size);
            contentHandler().fluidHandler.tanks.get(7).setCapacity(50000*size);
        }*/
    }

    public void tickClient() {
        super.tickClient();
        if(!isCasingValid) {
            stopSound();
            return;
        }
        if(playSoundCooldown > 0) {
            playSoundCooldown--;
            return;
        }
        if(functionalBlocksCharge < 100 && functionalBlocksCharge > 0) {
            playChargingSound();
            return;
        }
        if(isReady()) {
            if(energyPerTick > 0 && plasmaTemperature > 0) {
                playRunningSound();
                return;
            }
            playReadySound();
        } else {
            stopSound();
        }
    }

    protected void sendBeamData(FusionBeamParticleData data, BlockPos from) {
        Vec3 vec = Vec3.atCenterOf(from);
        if (!getLevel().isClientSide() && level instanceof ServerLevel serverWorld) {
            for (ServerPlayer player : serverWorld.players()) {
                serverWorld.sendParticles(player, data, true, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
            }
        }
    }

    protected void renderBeam() {
        BlockPosInstance pos = new BlockPosInstance(getBlockPos().above());
        int beamLength = size*2+4;
        sendBeamData(new FusionBeamParticleData(Direction.EAST, beamLength, 0.35f),
                pos.revert().relative(Direction.NORTH, size+2).relative(Direction.WEST, size+2)
        );
        sendBeamData(new FusionBeamParticleData(Direction.EAST, beamLength, 0.35f),
                pos.revert().relative(Direction.SOUTH, size+2).relative(Direction.WEST, size+2)
        );
        sendBeamData(new FusionBeamParticleData(Direction.SOUTH, beamLength, 0.35f),
                pos.revert().relative(Direction.EAST, size+2).relative(Direction.NORTH, size+2)
        );
        sendBeamData(new FusionBeamParticleData(Direction.SOUTH, beamLength, 0.35f),
                pos.revert().relative(Direction.WEST, size+2).relative(Direction.NORTH, size+2)
        );
    }

    protected void playReadySound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(FUSION_READY.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        playSound(FUSION_READY, 0.7f);
    }

    protected boolean isReady() {
        return isCasingValid
                && rfAmplifiersPower > 0
                && magnetsPower > 0
                && hasCoolant()
                && hasRecipe()
                && functionalBlocksCharge > 99;
    }

    protected void playRunningSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(FUSION_RUNNING.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        playSound(FUSION_RUNNING, 0.5f);
    }

    @Override
    public void tickServer() {
        changed = false;
        initMultiblock();
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        if(!initialized) {
            initialized = true;
            FusionCoreBlock block = (FusionCoreBlock) getBlockState().getBlock();
            block.placeProxyBlocks(getBlockState(), level, worldPosition, this);
        }
        tickProxyBlocks();
        super.tickServer();
        handleValidation();
        simulateReaction();
        sendOutPower();
        handleMeltdown();
        if(recipe == null && plasmaTemperature > 0) {
            changePlasmaTemperature(-(long) (plasmaTemperature / 10D));
            changed = true;
        }
        if(refreshCacheFlag || changed) {
            if(level.getGameTime() % 10 == 0) {
                updateAnalogSignal();
            }
            try {
                setChanged();
                assert level != null;
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_NEIGHBORS);
            } catch (NullPointerException ignore) {}
        }
        controllerEnabled = false;
        inputRedstoneSignal = 0;
    }

    private void tickProxyBlocks() {
        for(FusionCoreProxyBE proxy: getProxies()) {
            if(proxy == null) continue;
            proxy.forceTickServer(this);
        }
    }

    protected List<FluidStack> getAllowedCoolantsOutput() {
        List<FluidStack> allowedCoolants = new ArrayList<>();
        for(FusionCoolantRecipe recipe : getCoolantRecipes()) {
            allowedCoolants.addAll(recipe.getOutputFluids(0));
        }
        return allowedCoolants;
    }

    protected double lastHeadDeviationMult = 1;
    public double getHeatDeviationMultiplier() {
        double mEff = (magnetsEfficiency() - 50) / 100;
        double rEff = (rfEfficiency() - 50) / 100;
        //better plasma stability = smaller deviation window
        double plasmaStability = (1+ getControlPartsEfficiency())/2;
        double minMult = 0.2*plasmaStability + (mEff + rEff)/2;
        double maxMult = 1.7* getControlPartsEfficiency() - (mEff + rEff)/2;
        double rand = ((new Random()).nextDouble()+4)/5;
        lastHeadDeviationMult = (lastHeadDeviationMult + rand*(maxMult-minMult)+minMult)/2;
        return lastHeadDeviationMult;
    }

    /**
     * Depends on reactor casing heat
     */
    protected double rfEfficiency() {
        return rfEfficiency * Math.min((minRFAmplifiersTemp / ((reactorHeat+minRFAmplifiersTemp)/2)), 1);
    }

    /**
     * Depends on reactor casing heat
     */
    protected double magnetsEfficiency() {
        return magnetsEfficiency * Math.min((maxMagnetsTemp / ((reactorHeat+maxMagnetsTemp)/2)), 1);
    }

    /**
     * Falls back to variable in case of issues
     */
    public double getOptimalTemperature()
    {
        if(hasRecipe()) {
            if(((Recipe)recipeInfo().recipe()).getOptimalTemperature() > 0) {
                lastKnownOptimalTemp = ((Recipe)recipeInfo().recipe()).getOptimalTemperature() * 1000000;
            }
        }
        return lastKnownOptimalTemp;
    }

    /**
     * Depends on ratio of real plasma temperature to optimal + reactor size
     */
    public double minimalMagneticField() {
        return FUSION_CONFIG.MINIMAL_MAGNETIC_FIELD.get()*(plasmaTemperature/getOptimalTemperature())*size;
    }

    public double overallMagneticField()
    {
        return magneticFieldStrength/size;
    }

    /**
     * Depends on magnets efficiency and ratio of minimal magnetic field to overall magnetic field
     */
    public double getControlPartsEfficiency()
    {
        return Math.min(1.9, Math.max(0.1, 2D / (magnetsEfficiency() / 100 * (minimalMagneticField() / overallMagneticField()))));
    }

    public double getPlasmaStability()
    {
        return (getControlPartsEfficiency()/1.9D+calculateEfficiency()*2)/3;
    }


    protected List<FluidStack> getAllowedCoolants() {
        List<FluidStack> allowedCoolants = new ArrayList<>();
        for(FusionCoolantRecipe recipe : getCoolantRecipes()) {
            allowedCoolants.addAll(recipe.getInputFluids(0));
        }
        return allowedCoolants;
    }

    protected void sendOutPower() {
        for(FusionCoreProxyBE proxy: getProxies()) {
            if(proxy == null) continue;
            proxy.sendOutEnergy();
        }
        BlockEntity be = Objects.requireNonNull(getLevel()).getBlockEntity(getBlockPos().relative(Direction.DOWN));
        if(be instanceof BlockEntity && !(be instanceof FusionCoreProxyBE) && !(be instanceof FusionCoreBE)) {
            IEnergyStorage r = be.getCapability(ForgeCapabilities.ENERGY, Direction.UP).orElse(null);
            if(r == null) return;
            if(r.canReceive()) {
                int received = r.receiveEnergy(energyStorage().getEnergyStored() - rfAmplifiersPower - magnetsPower, false);
                energyStorage().setEnergy(energyStorage().getEnergyStored()-received);
            }
        }
    }

    protected FusionCoreProxyBE[] getProxies() {
        if(proxyBES == null) {
            proxyBES = new FusionCoreProxyBE[26];
            int i = 0;
            for(int y = 0; y < 3; y++) {
                for (int x = -1; x < 2; x++) {
                    for (int z = -1; z < 2; z++) {
                        BlockEntity be = blockEntity(getBlockPos().offset(x, y, z));
                        if (be instanceof FusionCoreProxyBE proxy) {
                            proxyBES[i] = proxy;
                            i++;
                        }
                    }
                }
            }
        }
        return proxyBES;
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
                for(FluidStackIngredient ingredient: recipe.getInputFluids()) {
                    allowedInputs.addAll(ingredient.getRepresentations());
                }
            }
        }
        return allowedInputs;
    }

    protected void simulateReaction() {
        if (!getMultiblock().isFormed()) {
            return;
        }
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;
        updateCharge();
        controllerEnabled = functionalBlocksCharge == 100 && controllerEnabled;
        if(controllerEnabled) {
            powered = processReaction();
            trackChanges(powered);
        } else {
            powered = false;
            if(plasmaTemperature > 0) {
                plasmaTemperature = (long) Math.max(0, plasmaTemperature/1.2f - 10000);
                changed = true;
            }
        }
        if (!hasRecipe()) {
            updateRecipe();
            trackChanges(hasRecipe());
        }
        trackChanges(coolDown());
    }

    public long getTargetCharge()
    {
        return (rfAmplifiersPower+magnetsPower) * 7L;
    }

    protected void updateCharge() {
        if(getTargetCharge() == 0) return;
        if(chargeAmount < getTargetCharge()) {
            chargeAmount += energyStorage().extractEnergy((rfAmplifiersPower+magnetsPower)/2, false);
            changed = true;
        }
        functionalBlocksCharge = (int) Math.min(((chargeAmount*100)/getTargetCharge()), 100);
    }

    protected void playChargingSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(FUSION_CHARGING.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        playSound(FUSION_CHARGING, 0.7f);
    }

    @Override
    public FusionReactorMultiblock getMultiblock() {
        if(getLevel().isClientSide()) return null;
        if(multiblock == null) {
            multiblock = new FusionReactorMultiblock(this);
        }
        return (FusionReactorMultiblock) multiblock;
    }

    public double rfAmplifierRatio() {
        return Math.max(0, Math.min((((double)rfAmplificationRatio/100)*(double)amplificationAdjustment/100), 1D));
    }

    protected void coolantCoolDown()
    {
        if(hasCoolant()) {
            if(reactorHeat > coolantRecipe.getCoolingRate()) {
                int coolantNeeded = (int) (reactorHeat/coolantRecipe.getCoolingRate());
                int coolantPerOp = coolantRecipe.getInputFluids()[0].getAmount();
                int availableCoolant = contentHandler().fluidHandler.tanks.get(2).getFluidAmount();
                int possibleOps = availableCoolant/coolantPerOp;
                int actualOps = Math.min(possibleOps, coolantNeeded*coolantPerOp);
                changeReactorHeat(-(coolantRecipe.getCoolingRate() / size) * actualOps);
                extractCoolant(actualOps);
            }
        }
    }

    protected void changeReactorHeat(double amount) {
        reactorHeat += amount;
        reactorHeat = Math.max(0, reactorHeat);
    }

    protected boolean coolDown() {
        double wasHeat = reactorHeat;
        //passive cooldown
        changeReactorHeat(-1000*Math.log(size+4));
        coolantCoolDown();
        return wasHeat != reactorHeat;
    }

    protected void extractCoolant(int ops) {
        contentHandler().fluidHandler.tanks.get(2).drain(coolantRecipe.getInputFluids()[0].getAmount()*ops, EXECUTE);
        FluidStack output = coolantRecipe.getOutputFluids().get(0).copy();
        output.setAmount(output.getAmount()*ops);
        contentHandler().fluidHandler.tanks.get(7).fill(output, EXECUTE);
    }

    protected boolean processReaction() {
        if(recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if(contentHandler().fluidHandler.getFluidInSlot(0).equals(FluidStack.EMPTY)) {
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

    protected double radiationAggregated = 0;
    protected boolean process() {
        recipeInfo().process(efficiency*4);
        if(recipeInfo().radiation != 1D) {
            radiationAggregated += recipeInfo().radiation/5000;
            if(radiationAggregated > 100) {
                RadiationManager.get(getLevel()).addRadiation(getLevel(), radiationAggregated, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
                radiationAggregated = 0;
            }
        }
        if (!recipeInfo().isCompleted()) {
            simulateHeatExchange();
            if(energyPerTick > 0) {
                renderBeam();
                energyStorage().addEnergy(energyPerTick);
            }
        }

        handleRecipeOutput();

        efficiency = calculateEfficiency();
        return true;
    }

    protected double calculateEfficiency() {
        double temperatureEfficiency = plasmaTemperature/getOptimalTemperature();
        if(plasmaTemperature > getOptimalTemperature()) {
            temperatureEfficiency = getOptimalTemperature()/plasmaTemperature + 0.1; //if plasma hotter we still get some small benefit
        }
        return temperatureEfficiency;
    }

    protected void changePlasmaTemperature(long amount) {
        plasmaTemperature += amount;
        plasmaTemperature = Math.max(0, plasmaTemperature);
    }

    protected void simulateHeatExchange() {
        amplifyPlasma();
        plasmaToEnergyExchange();
        heatLossExchange();
    }

    protected void heatLossExchange() {
        double sizeFactor = Math.log(Math.pow(size+2, 2))/100;
        changePlasmaTemperature((long) -((plasmaTemperature / Math.pow(getControlPartsEfficiency(), 2))*sizeFactor));
        changeReactorHeat((double) Math.min(plasmaTemperature, 100000000) / (10000*Math.log(size+4)* getControlPartsEfficiency()));
    }


    protected void plasmaToEnergyExchange() {
        double optimalTemp = getOptimalTemperature();
        double sizeFactor = Math.log(Math.pow(size+1, 8))/8D;
        if(plasmaTemperature < optimalTemp) {
            energyPerTick = (int) (plasmaTemperature/optimalTemp*recipeInfo().recipe().getEnergy());
        } else {
            energyPerTick = (int) (optimalTemp/plasmaTemperature*recipeInfo().recipe().getEnergy());
        }
        changePlasmaTemperature(-(long) ((plasmaTemperature)/(150D*energyPerTick/recipeInfo().recipe().getEnergy())));
        energyPerTick = (int) ((energyPerTick*calculateEfficiency()*size*sizeFactor)*FUSION_CONFIG.PLASMA_TO_ENERGY_CONVERTION.get());
        if(plasmaTemperature < 1000000) {
            energyPerTick = 0;
        }
        energyPerTick = (int) (energyPerTick* getControlPartsEfficiency());
    }

    protected long prevAmplification = 0;

    protected void amplifyPlasma() {
        double sizeFactor = Math.log(Math.pow(size+1, 5))/10D;
        double pRatio = Math.log(getOptimalTemperature()/150000000);
        double plasmaHeatScale = (pRatio-Math.sqrt(pRatio)+5)/7;
        double amplificationVolume = (double) rfAmplification * sizeFactor;
        double amplification =
                        amplificationVolume
                        * plasmaHeatScale
                        * rfAmplifierRatio()
                        * getHeatDeviationMultiplier()
                        * FUSION_CONFIG.RF_AMPLIFICATION_MULTIPLIER.get();
        prevAmplification += (long) (amplification/1000);
        prevAmplification = (long) Math.min(prevAmplification, amplification);
        changePlasmaTemperature(prevAmplification);
    }

    protected void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if(recipe == null) {
                recipe = (Recipe) recipeInfo().recipe();
            }
            if (recipe.handleOutputs(contentHandler)) {
                updateRecipe();
            } else {
                recipeInfo().stuck = true;
            }
            setChanged();
        }
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
            recipeInfo().ticks = (int) (recipeInfo().recipe().getTimeModifier()*10);
            recipeInfo().energy = recipeInfo().recipe.getEnergy();
            recipeInfo().heat = ((Recipe)recipeInfo().recipe()).getHeat();
            recipeInfo().radiation = recipeInfo().recipe().getRadiation();
            recipeInfo().be = this;
            if(!recipe.consumeInputs(contentHandler, 1)) {
                recipeInfo().clear();
                recipe = null;
            }
        } else {
            recipeInfo().clear();
        }
    }

    @Override
    public Recipe getRecipe() {
        if(contentHandler().fluidHandler.getFluidInSlot(0).isEmpty() || contentHandler().fluidHandler.getFluidInSlot(0).isEmpty()) return null;
        return (Recipe) super.getRecipe();
    }

    public boolean recipeIsStuck() {
        return recipeInfo().isStuck();
    }

    protected void handleMeltdown() {
        if(!isCasingValid) {
            return;
        }
        if (reactorHeat > getMaxHeat() && plasmaTemperature > 10000) {
            meltDown();
            plasmaTemperature /= 5D;
            reactorHeat /= 2D;
        }
    }

    private void meltDown() {
        double explosionRadius = FUSION_CONFIG.EXPLOSION_RADIUS.get();
        if (explosionRadius > 0) {
            BlockPos pos = getRandomPosAtRing();
            getLevel().explode((Entity) null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    (float) explosionRadius, Level.ExplosionInteraction.TNT);
        }
    }

    private BlockPos getRandomPosAtRing() {
        BlockPos pos = getBlockPos();
        int xDir = getLevel().random.nextBoolean() ? 1 : -1;
        int zDir = getLevel().random.nextBoolean() ? 1 : -1;
        int x = xDir * size;
        int z = zDir * size;
        int y = 1;

        return pos.offset(x, y, z);
    }

    public boolean hasRedstoneSignal() {
        return inputRedstoneSignal > 0;
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe() != null;
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    public void voidFuel() {
        contentHandler().fluidHandler.tanks.get(0).setFluid(FluidStack.EMPTY);
        contentHandler().fluidHandler.tanks.get(1).setFluid(FluidStack.EMPTY);
        contentHandler().clearHolded();
    }

    @Override
    public void handleSliderUpdate(int buttonId, int ratio)
    {
        if(buttonId == 0) {
            amplificationAdjustment = Math.min(100, Math.max(ratio, 1));
            changed = getMultiblock().updateCharacteristics();
        }
    }

    public boolean hasCoolant() {
        FluidStack coolant = contentHandler().fluidHandler.getFluidInSlot(2);
        if(coolant.isEmpty()) {
            coolantRecipe = null;
            return false;
        }
        if(coolantRecipe == null) {
            for(FusionCoolantRecipe recipe: getCoolantRecipes()) {
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
        return coolantRecipe instanceof FusionCoolantRecipe;
    }

    public boolean hasEnoughEnergy() {
        return energyStorage().getEnergyStored() > rfAmplifiersPower+magnetsPower;
    }

    public boolean isRunning() {
        return powered && energyPerTick > 0 && plasmaTemperature > 0 && efficiency > 0;
    }

    public boolean canAnalyze() {
        return analyzeDelay < 1;
    }

    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        report.put("report.nc.1.fusion_size", String.valueOf(size));
        report.put("report.nc.2.magnets", String.valueOf(magnets));
        report.put("report.nc.3.amplifiers", String.valueOf(amplifiers));
        report.put("report.nc.4.fusion_rf_amplification", String.valueOf(rfAmplification));
        report.put("report.nc.5.casing_blocks", String.valueOf(casingBlocks));
        report.put("report.nc.6.connectors", String.valueOf(connectors));
        return report;
    }

    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double radiation, double temperature) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, radiation, temperature);
        }

        @Override
        public @NotNull String getGroup() {
            return FusionReactorRegistration.FUSION_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FusionReactorRegistration.FUSION_BLOCKS.get(codeId).get());
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
            buffer.writeDouble(getOptimalTemperature());
        }

        public double getOptimalTemperature() {
            return rarityModifier;
        }

        @Override
        public String getCodeId() {
            return "fusion_core";
        }
    }

    public static class FusionCoolantRecipe extends NcRecipe {
        protected double coolingRate;

        public FusionCoolantRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double temperature, double powerModifier, double radiation, double rar) {
            super(id, input, output, inputFluids, outputFluids, temperature, powerModifier, radiation, rar);
            coolingRate = temperature;
        }

        @Override
        public @NotNull String getGroup() {
            return "fusion_coolant";
        }

        @Override
        public String getCodeId() {
            return "fusion_coolant";
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FusionReactorRegistration.FUSION_BLOCKS.get("fusion_core").get());
        }

        public double getCoolingRate() {
            return Math.max(rarityModifier, 1);
        }
    }

    public static class SignalSource {
        public static final byte ENERGY = 11;
        public static final byte HEAT = 12;
        public static final byte EFFICIENCY = 13;
    }
}
