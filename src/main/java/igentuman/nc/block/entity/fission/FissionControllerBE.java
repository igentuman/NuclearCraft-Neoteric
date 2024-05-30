package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.compat.cc.NCSolidFissionReactorPeripheral;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.item.ItemFuel;
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
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.FissionFuel.ITEM_PROPERTIES;
import static igentuman.nc.setup.registration.NCSounds.FISSION_REACTOR;
import static igentuman.nc.setup.registration.NcParticleTypes.RADIATION;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isMekanismLoadeed;
import static net.minecraft.world.item.Items.AIR;

public class FissionControllerBE <RECIPE extends FissionControllerBE.Recipe> extends FissionBE  {

    public static String NAME = "fission_reactor_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage = createEnergy();

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;

    @NBTField
    public boolean isSteamMode = false;
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
    public int toggleModeTimer = 2000;

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
    public double moderationLevel = 1D;
    @NBTField
    public boolean powered = false;
    protected boolean forceShutdown = false;
    public int fuelCellMultiplier = 1;
    public int moderatorCellMultiplier = 1;

    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo<RECIPE> recipeInfo = new RecipeInfo<>();
    public boolean controllerEnabled = false;
    private Direction facing;
    public RECIPE recipe;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();
    @NBTField
    private double steamRate;
    @NBTField
    public int steamPerTick = 0;

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
                1, 1);
        contentHandler.setBlockEntity(this);
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler.fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler.itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.fluidCapability.tanks.get(0).setCapacity(10000);
        contentHandler.fluidCapability.tanks.get(1).setCapacity(10000);
        contentHandler.setAllowedInputFluids(0, getAllowedCoolants());
        contentHandler.setAllowedInputFluids(1, getAllowedCoolantsOutput());

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

    public FluidTank getFluidTank(int i) {
        return contentHandler.fluidCapability.tanks.get(i);
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
            cooling += getNetHeat();
        }
        cooling = Math.min(heat, cooling);
        double heatEff =  cooling * FISSION_CONFIG.BOILING_MULTIPLIER.get();

        if(hasCoolant()) {
            FluidStack steam = boilingRecipe.getOutputFluids().get(0);
            FluidStack coolant = boilingRecipe.getInputFluids(0).get(0);
            double conversion = heatEff/boilingRecipe.conversionRate();
            FluidStack currentCoolant = contentHandler.fluidCapability.getFluidInSlot(0);
            FluidStack currentOutput = contentHandler.fluidCapability.getFluidInSlot(1);
            if(!steam.isFluidEqual(currentOutput) && !currentOutput.isEmpty()) {
                //No room? Heat up
                heat += coolingPerTick()/2;
                return;
            }
            double capacity = contentHandler.fluidCapability.tanks.get(1).getCapacity() - currentOutput.getAmount();
            int ops = (int) (capacity/steam.getAmount());
            capacity = ops*steam.getAmount();
            int canGetAmount = (int) Math.min(steam.getAmount()*conversion, capacity);
            ops = canGetAmount/steam.getAmount();
            ops = Math.min(currentCoolant.getAmount()/coolant.getAmount(), ops);
            contentHandler.fluidCapability.tanks.get(0).drain(ops*coolant.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            FluidStack out = steam.copy();
            out.setAmount(ops*steam.getAmount());
            steamPerTick = ops*steam.getAmount();
            contentHandler.fluidCapability.tanks.get(1).fill(out, IFluidHandler.FluidAction.EXECUTE);
            changed = true;
            if(ops < conversion) {
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return contentHandler.getItemCapability(side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && isSteamMode) {
            return contentHandler.getFluidCapability(side);
        }
        if (cap == ForgeCapabilities.ENERGY && !isSteamMode) {
            return energy.cast();
        }
        if(isMekanismLoadeed() && isSteamMode) {
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

    protected void playRunningSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(FISSION_REACTOR.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(FISSION_REACTOR.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(FISSION_REACTOR.get(), SoundSource.BLOCKS, 0.2f, level.getRandom(), getBlockPos());
        }
    }

    public void tickClient() {
        if(!isCasingValid || !isInternalValid) {
            stopSound();
            return;
        }
        if(isProcessing()) {
            spawnParticles();
            playRunningSound();
        }
    }
    protected int reValidateCounter = 0;

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            irradiationHeat = 0;
            return;
        }
        changed = false;
        hopToggleMode();

        super.tickServer();
        boolean wasPowered = powered;
        handleValidation();
        trackChanges(wasPowered, powered);
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && multiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;

        if (multiblock().isFormed()) {
            trackChanges(updateModerationLevel());
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
    public FissionReactorMultiblock multiblock() {
        if(multiblock == null) {
            multiblock = new FissionReactorMultiblock(this);
        }
        return super.multiblock();
    }

    private void handleValidation() {
        multiblock().tick();
        boolean wasFormed = multiblock().isFormed();
        if (!wasFormed || !isInternalValid || !isCasingValid) {
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
        if(multiblock().isFormed()) {
            contentHandler.fluidCapability.tanks.get(0).setCapacity(5000*height*width*depth);
            contentHandler.fluidCapability.tanks.get(1).setCapacity(5000*height*width*depth);
        }
        trackChanges(wasFormed, multiblock().isFormed());
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {
        if (heat >= getMaxHeat()) {
            BlockPos explosionPos = getBlockPos().relative(getFacing(), 2);
            List<BlockPos> fuelCells = new ArrayList<>(multiblock().fuelCells);
            if (FISSION_CONFIG.EXPLOSION_RADIUS.get() == 0) {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 2F, Level.ExplosionInteraction.NONE);
            } else {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), FISSION_CONFIG.EXPLOSION_RADIUS.get().floatValue(), Level.ExplosionInteraction.NONE);
                getLevel().setBlock(explosionPos, NCFluids.getBlock("corium"), 1);
                for (BlockPos pos : fuelCells) {
                    getLevel().explode(null, pos.getX(), pos.getY(), pos.getZ(), 2, Level.ExplosionInteraction.NONE);
                    getLevel().setBlock(pos, NCFluids.getBlock("corium"), 1);
                }
            }

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
        if(getLevel().isClientSide()) {
            return;
        }
        if(multiblock() != null) {
            multiblock().onControllerRemoved();
        }
    }

    private boolean coolDown() {
        double wasHeat = heat;
        heat -= coolingPerTick();
        boil();
        heat = Math.max(0, heat);
        return wasHeat != heat;
    }

    public float speed = 0.001f;

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

    private void spawnParticles() {
        if(multiblock() == null) {
            return;
        }
        if(!multiblock().isFormed()) {
            multiblock().validate();
        }
        if(level.getGameTime()  % (level.random.nextInt(10)+5) != 0) {
            return;
        }
        BlockPos topBlock = multiblock().getTopRightInnerBlock();
        BlockPos bottomLeft = multiblock().getBottomLeftInnerBlock();

        for(BlockPos blockPos: BlockPos.betweenClosed(bottomLeft, topBlock)) {
            if(level.random.nextBoolean()) {
                level.addParticle(RADIATION.get(), blockPos.getX()+level.random.nextFloat(), blockPos.getY()+level.random.nextFloat(), blockPos.getZ()+level.random.nextFloat(), 0, -0.05f, 0);
            }
        }
    }

    private boolean process() {
        recipeInfo.process(fuelCellsCount * (heatMultiplier() + collectedHeatMultiplier() - 1));
        if(recipeInfo.radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), recipeInfo.radiation/1000, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
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
        return moderationLevel * recipeInfo.heat * moderatorCellMultiplier * (FISSION_CONFIG.MODERATOR_HEAT_MULTIPLIER.get() / 100);
    }

    public double moderatorsFE() {
        return moderationLevel * recipeInfo.energy * moderatorCellMultiplier * (FISSION_CONFIG.MODERATOR_FE_MULTIPLIER.get() / 100);
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
        infoTag.putLong("erroredBlock", errorBlockPos.asLong());
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

    public List<FissionBoilingRecipe> getBoilingRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<FissionBoilingRecipe>) NcRecipeType.ALL_RECIPES
                    .get("fission_boiling")
                    .getRecipeType().getRecipes(getLevel());
        }
        return coolantRecipes;
    }

    protected List<FissionBoilingRecipe> coolantRecipes;

    protected FissionBoilingRecipe boilingRecipe;
    public boolean hasCoolant() {
        FluidStack coolant = contentHandler.fluidCapability.getFluidInSlot(0);
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
        controllerEnabled = mode;
    }

    private double targetModerationLevel = 1D;

    public void adjustModerator(int redstoneSignal) {
        String formatted = String.format(Locale.US, "%.2f", (double) redstoneSignal / 15);
        targetModerationLevel = Double.parseDouble(formatted);
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
        return moderationLevel;
    }

    public void adjustModerationLevel(int level) {
        String formatted = String.format(Locale.US,"%.2f", (double) Math.max(1, level) / 100);
        targetModerationLevel = Double.parseDouble(formatted);
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(codeId, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return "fission_reactor_controller";
        }

        protected ItemFuel fuelItem;

        public ItemFuel getFuelItem() {
            if(fuelItem == null) {
                Item item = getFirstItemStackIngredient(0).getItem();
                if( !(item instanceof ItemFuel) && !item.equals(AIR)) {
                    fuelItem = new ItemFuel(ITEM_PROPERTIES, new FuelDef(item.toString(), "", (int) powerModifier, 50D, 10D, timeModifier, 100D));
                    return fuelItem;
                }
                Item item1 = getFirstItemStackIngredient(0).getItem();
                if(item1 instanceof ItemFuel) {
                    fuelItem  = (ItemFuel) item1;
                }
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
            return (int) (getFuelItem().depletion*20*timeModifier);
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

        public FissionBoilingRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double conversionRate, double powerModifier, double radiation, double rar) {
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
            return new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get());
        }

        public double conversionRate() {
            return Math.max(conversionRate, 1);
        }
    }
}
