package igentuman.nc.block.entity.fusion;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.compat.cc.NCFusionReactorPeripheral;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
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
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.handler.config.FusionConfig.FUSION_CONFIG;
import static igentuman.nc.setup.registration.NCSounds.*;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isMekanismLoadeed;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class FusionCoreBE <RECIPE extends FusionCoreBE.Recipe> extends FusionBE {

    @NBTField
    public double reactorHeat = 0;
    @NBTField
    public boolean isCasingValid = false;
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
    @NBTField
    public int rfAmplificationRatio = 0;
    @NBTField
    public int functionalBlocksCharge = 0;
    @NBTField
    public long plasmaTemperature = 0;
    public long chargeAmount = 0;
    @NBTField
    protected int rfEfficiency = 0;
    @NBTField
    protected int magnetsEfficiency = 0;
    @NBTField
    protected double lastKnownOptimalTemp = 1000000;
    @NBTField
    protected boolean isInternalValid = false;
    public List<FusionCoolantRecipe> getCoolantRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<FusionCoolantRecipe>) NcRecipeType.ALL_RECIPES.get("fusion_coolant").getRecipeType().getRecipes(getLevel());
        }
        return coolantRecipes;
    }

    protected List<FusionCoolantRecipe> coolantRecipes;

    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage = createEnergy();

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;

    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo<RECIPE> recipeInfo = new RecipeInfo<>();
    public boolean controllerEnabled = false;
    public RECIPE recipe;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    protected boolean initialized = false;
    protected int reValidateCounter = 40;
    protected boolean refreshCacheFlag;
    protected List<FluidStack> allowedInputs;
    protected FusionCoreProxyBE[] proxyBES;

    public FluidTank getFluidTank(int i) {
        return contentHandler.fluidCapability.tanks.get(i);
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(2_048_000_000, 100000000, 100000000) {
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
                3, 5, 10, 50);
        contentHandler.setBlockEntity(this);
        //fuel
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler.fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.INPUT);
        //coolant
        contentHandler.fluidCapability.setGlobalMode(2, SlotModePair.SlotMode.INPUT);

        //products
        contentHandler.fluidCapability.setGlobalMode(3, SlotModePair.SlotMode.OUTPUT);
        contentHandler.fluidCapability.setGlobalMode(4, SlotModePair.SlotMode.OUTPUT);
        contentHandler.fluidCapability.setGlobalMode(5, SlotModePair.SlotMode.OUTPUT);
        contentHandler.fluidCapability.setGlobalMode(6, SlotModePair.SlotMode.OUTPUT);
        //hot coolant
        contentHandler.fluidCapability.setGlobalMode(7, SlotModePair.SlotMode.OUTPUT);
    }

    protected LazyOptional<NCFusionReactorPeripheral> peripheralCap;

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
        if(isMekanismLoadeed()) {
            if(cap == mekanism.common.capabilities.Capabilities.GAS_HANDLER) {
                if(contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler.gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == mekanism.common.capabilities.Capabilities.SLURRY_HANDLER) {
                if(contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler.getSlurryConverter(side));
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

    @Override
    public void onLoad() {
        initialized = false;
    }

    public double getMaxHeat() {
        return Math.max(minRFAmplifiersTemp*2, maxMagnetsTemp*2);
    }

    public void handleValidation()
    {
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
        changed = wasPowered != powered || wasFormed != multiblock().isFormed();
        trackChanges(updateCharacteristics());
        size = multiblock().isFormed() ? multiblock.width() : 0;
        refreshCacheFlag = !multiblock().isFormed();
        if(multiblock().isFormed()) {
            contentHandler.fluidCapability.tanks.get(2).setCapacity(50000*size);
            contentHandler.fluidCapability.tanks.get(7).setCapacity(50000*size);
        }
    }
    protected int updateSpan = 20;
    protected void periodicalUpdate()
    {
        updateSpan--;
        if(updateSpan < 0) {
            updateSpan = 20;
            changed = true;
            setChanged();
        }
    }

    public void tickClient() {
        BlockOverlayHandler.removeFusionReactor(getBlockPos());
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
               // BlockOverlayHandler.addFusionReactor(getBlockPos());
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
        NCBlockPos pos = new NCBlockPos(getBlockPos().above());
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
        if(currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound)) {
            if(currentSound != null && currentSound.getLocation().equals(FUSION_READY.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(FUSION_READY.get(), SoundSource.BLOCKS, 0.8f, level.getRandom(), getBlockPos());
        }
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
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(FUSION_RUNNING.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(FUSION_RUNNING.get(), SoundSource.BLOCKS, 0.5f, level.getRandom(), getBlockPos());
        }
    }

    @Override
    public void tickServer() {
        changed = false;
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        if(!initialized) {
            initialized = true;
            FusionCoreBlock block = (FusionCoreBlock) getBlockState().getBlock();
            block.placeProxyBlocks(getBlockState(), level, worldPosition, this);
        }
        super.tickServer();
        multiblock().tick();
        handleValidation();
        periodicalUpdate();

        simulateReaction();
        sendOutPower();
        handleMeltdown();
        contentHandler.setAllowedInputFluids(0, getAllowedInputFluids());
        contentHandler.setAllowedInputFluids(1, getAllowedInputFluids());
        contentHandler.setAllowedInputFluids(2, getAllowedCoolants());
        contentHandler.setAllowedInputFluids(7, getAllowedCoolantsOutput());
        contentHandler.fluidCapability.tanks.get(2).setCapacity(100000);
        contentHandler.fluidCapability.tanks.get(7).setCapacity(100000);
        if(refreshCacheFlag || changed) {
            try {
                assert level != null;
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignore) {}
        }
        controllerEnabled = false;
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
            if(recipeInfo.recipe().getOptimalTemperature() > 0) {
                lastKnownOptimalTemp = recipeInfo.recipe().getOptimalTemperature() * 1000000;
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
        if(be instanceof BlockEntity && !(be instanceof FusionBE)) {
            IEnergyStorage r = be.getCapability(ForgeCapabilities.ENERGY, Direction.UP).orElse(null);
            if(r == null) return;
            if(r.canReceive()) {
                int recieved = r.receiveEnergy(energyStorage.getEnergyStored() - rfAmplifiersPower - magnetsPower, false);
                energyStorage.setEnergy(energyStorage.getEnergyStored()-recieved);
            }
        }
    }

    protected FusionCoreProxyBE[] getProxies() {
        if(proxyBES == null) {
            proxyBES = new FusionCoreProxyBE[18];
            int i = 0;
            for(int y = 0; y < 3; y+=2) {
                for (int x = -1; x < 2; x++) {
                    for (int z = -1; z < 2; z++) {
                        BlockEntity be = Objects.requireNonNull(getLevel()).getBlockEntity(getBlockPos().offset(x, y, z));
                        if (be instanceof FusionCoreProxyBE) {
                            proxyBES[i] = (FusionCoreProxyBE) be;
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
            for(NcRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
                for(FluidStackIngredient ingredient: recipe.getInputFluids()) {
                    allowedInputs.addAll(ingredient.getRepresentations());
                }
            }
        }
        return allowedInputs;
    }

    protected void simulateReaction() {
        if (!multiblock().isFormed()) {
            return;
        }
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && multiblock().isReadyToProcess();
        controllerEnabled = !forceShutdown && controllerEnabled;
        updateCharge();
        controllerEnabled = functionalBlocksCharge == 100 && controllerEnabled;
        if(controllerEnabled) {
            powered = processReaction();
            trackChanges(powered);
        } else {
            powered = false;
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
            chargeAmount += energyStorage.extractEnergy((rfAmplifiersPower+magnetsPower)/2, false);
            changed = true;
        }
        functionalBlocksCharge = (int) Math.min(((chargeAmount*100)/getTargetCharge()), 100);
    }

    protected void playChargingSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(FUSION_CHARGING.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if(currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound)) {
            if(currentSound != null && currentSound.getLocation().equals(FUSION_CHARGING.get().getLocation())) {
                return;
            }
            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(FUSION_CHARGING.get(), SoundSource.BLOCKS, 0.5f, level.getRandom(), getBlockPos());
        }
    }

    protected boolean updateCharacteristics() {
        boolean hasChanges =
                magneticFieldStrength != multiblock().magneticFieldStrength
                || rfEfficiency != multiblock().rfEfficiency
                || magnetsEfficiency != multiblock().magnetsEfficiency
                || maxMagnetsTemp != multiblock().maxMagnetsTemp
                || rfAmplification != multiblock().rfAmplification*rfAmplifierRatio()
                || rfAmplifiersPower != multiblock().rfAmplifiersPower*rfAmplifierRatio()
                || minRFAmplifiersTemp != multiblock().maxRFAmplifiersTemp;
        rfEfficiency = multiblock().rfEfficiency;
        magnetsEfficiency = multiblock().magnetsEfficiency;
        magneticFieldStrength = multiblock().magneticFieldStrength;
        magnetsPower = multiblock().magnetsPower;
        maxMagnetsTemp = multiblock().maxMagnetsTemp;
        rfAmplification = (int) (multiblock().rfAmplification*rfAmplifierRatio());
        rfAmplifiersPower = (int) (multiblock().rfAmplifiersPower*rfAmplifierRatio());
        minRFAmplifiersTemp = multiblock().maxRFAmplifiersTemp;
        if(hasChanges) {
            currentRfAmplification = rfAmplification;
        }
        return hasChanges;
    }

    protected double rfAmplifierRatio() {
        return Math.max(((double)rfAmplificationRatio/100), 1D);
    }
    protected void coolantCoolDown()
    {
        if(hasCoolant()) {
            if(reactorHeat > coolantRecipe.getCoolingRate()) {
                int coolantNeeded = (int) (reactorHeat/coolantRecipe.getCoolingRate());
                int coolantPerOp = coolantRecipe.getInputFluids()[0].getAmount();
                int availableCoolant = contentHandler.fluidCapability.tanks.get(2).getFluidAmount();
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
        changeReactorHeat(-1000*size);
        coolantCoolDown();
        return wasHeat != reactorHeat;
    }

    protected void extractCoolant(int ops) {
        contentHandler.fluidCapability.tanks.get(2).drain(coolantRecipe.getInputFluids()[0].getAmount()*ops, EXECUTE);
        FluidStack output = coolantRecipe.getOutputFluids().get(0).copy();
        output.setAmount(output.getAmount()*ops);
        contentHandler.fluidCapability.tanks.get(7).fill(output, EXECUTE);
    }

    protected boolean processReaction() {
        if(recipeInfo.recipe != null && recipeInfo.isCompleted()) {
            if(contentHandler.fluidCapability.getFluidInSlot(0).equals(FluidStack.EMPTY)) {
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
    protected double radiationAggregated = 0;
    protected boolean process() {
        recipeInfo.process(efficiency);
        if(recipeInfo.radiation != 1D) {
            radiationAggregated += recipeInfo.radiation/5000;
            if(radiationAggregated > 100) {
                RadiationManager.get(getLevel()).addRadiation(getLevel(), radiationAggregated, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
                radiationAggregated = 0;
            }
        }
        if (!recipeInfo.isCompleted()) {
            simulateHeatExchange();
            if(energyPerTick > 0) {
                renderBeam();
                energyStorage.addEnergy(energyPerTick);
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
        changeReactorHeat((double) Math.min(plasmaTemperature, 100000000) / (10000*size* getControlPartsEfficiency()));
    }


    protected void plasmaToEnergyExchange() {
        double optimalTemp = getOptimalTemperature();
        double sizeFactor = Math.log(Math.pow(size+1, 8))/8D;
        if(plasmaTemperature < optimalTemp) {
            energyPerTick = (int) (plasmaTemperature/optimalTemp*recipeInfo.recipe().getEnergy());
        } else {
            energyPerTick = (int) (optimalTemp/plasmaTemperature*recipeInfo.recipe().getEnergy());
        }
        changePlasmaTemperature(-(long) ((plasmaTemperature)/(150D*energyPerTick/recipeInfo.recipe().getEnergy())));
        energyPerTick = (int) ((energyPerTick*calculateEfficiency()*size*sizeFactor)*FUSION_CONFIG.PLASMA_TO_ENERGY_CONVERTION.get());
        if(plasmaTemperature < 1000000) {
            energyPerTick = 0;
        }
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
        if (hasRecipe() && recipeInfo.isCompleted()) {
            if(recipe == null) {
                recipe = recipeInfo.recipe();
            }
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
                if(contentHandler.fluidCapability.getFluidInSlot(0).isEmpty()) {
                    recipe = null;
                }
            } else {
                recipeInfo.stuck = true;
            }
            setChanged();
        }
    }

    protected void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = (int) (recipeInfo.recipe().getTimeModifier()*10);
            recipeInfo.energy = recipeInfo.recipe.getEnergy();
            recipeInfo.heat = recipeInfo.recipe().getHeat();
            recipeInfo.radiation = recipeInfo.recipe().getRadiation();
            recipeInfo.be = this;
            recipe.consumeInputs(contentHandler);
        }
    }

    protected void addToCache(RECIPE recipe) {
        String key = contentHandler.getCacheKey();
        if(cachedRecipes.containsKey(key)) {
            cachedRecipes.replace(key, recipe);
        } else {
            cachedRecipes.put(key, recipe);
        }
    }
    public RECIPE getRecipe() {
        if(contentHandler.fluidCapability.getFluidInSlot(0).isEmpty()) return null;
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

    protected void handleMeltdown() {
        if(!isCasingValid) {
            return;
        }
        if (reactorHeat > getMaxHeat() && plasmaTemperature > 1000) {
            meltDown();
            plasmaTemperature /= 5D;
            reactorHeat /= 2D;
        }
    }

    private void meltDown() {
        double explosionRadius = FUSION_CONFIG.EXPLOSION_RADIUS.get();
        if (explosionRadius > 0) {
            BlockPos pos = getBlockPos();
/*            getLevel().explode(null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    1, true, Explosion.BlockInteraction.NONE);*/
        }
    }

    public boolean hasRedstoneSignal() {
        return inputRedstoneSignal > 0;
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    public void voidFuel() {
        contentHandler.fluidCapability.tanks.get(0).setFluid(FluidStack.EMPTY);
        contentHandler.fluidCapability.tanks.get(1).setFluid(FluidStack.EMPTY);
        contentHandler.clearHolded();
    }

    @Override
    public void handleSliderUpdate(int buttonId, int ratio)
    {
        if(buttonId == 0) {
            rfAmplificationRatio = Math.min(100, Math.max(ratio, 1));
            changed = updateCharacteristics();
        }
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
        if(errorBlockPos instanceof BlockPos) {
            infoTag.putLong("erroredBlock", errorBlockPos.asLong());
        }
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    @Override
    public void loadClientData(CompoundTag tag) {
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
    protected void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        saveTagData(infoTag);
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("validationId", validationResult.id);
        if(errorBlockPos != null) {
            infoTag.putLong("erroredBlock", errorBlockPos.asLong());
        }
        tag.put("Content", contentHandler.serializeNBT());
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


    protected FusionCoolantRecipe coolantRecipe;
    public boolean hasCoolant() {
        FluidStack coolant = contentHandler.fluidCapability.getFluidInSlot(2);
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
        return energyStorage.getEnergyStored() > rfAmplifiersPower+magnetsPower;
    }

    public boolean isRunning() {
        return powered && energyPerTick > 0 && plasmaTemperature > 0 && efficiency > 0;
    }

    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double radiation, double temperature) {
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
            return new ItemStack(FusionReactor.FUSION_BLOCKS.get("fusion_core").get());
        }

        public double getCoolingRate() {
            return Math.max(rarityModifier, 1);
        }
    }
}
