package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.KugelblitzPeripheral;
import igentuman.nc.compat.oc2.KugelblitzDevice;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
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
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BE;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.setup.registration.GameEvents.BLACKHOLE_VIBRATION;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static net.minecraft.world.level.block.Blocks.AIR;

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
    public int energyConvertionRate = 7;
    @NBTField
    public boolean controllerEnabled = false;
    @NBTField
    public int transformers = 0;
    @NBTField
    public int fluxRegulators = 0;
    @NBTField
    public int blackholeStability = 100;
    @NBTField
    public int stabilizers = 0;


    protected Direction facing;
    public Recipe recipe;
    public HashMap<String, Recipe> cachedRecipes = new HashMap<>();
    private List<ItemStack> allowedInputs;
    private final List<ItemStack> orderedOutputs = new ArrayList<>();
    private List<FluidStack> allowedInputFluids;

    public ChamberTerminalBE(BlockPos pPos, BlockState pBlockState) {
        super(KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 0, 1000);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        contentHandler.setBlockEntity(this);
        contentHandler.setAllowedInputFluids(0, this::getAllowedInputFluids);
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("kugelblitz_chamber", getLevel())) {
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
            if(!hasResultItem(cRecipe)) {
                return null;
            }
            return cRecipe;
        }
        if(!NcRecipeType.ALL_RECIPES.containsKey("kugelblitz_chamber")) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("kugelblitz_chamber", getLevel())) {
            if(recipe.test(contentHandler())) {
                addToCache(recipe);
                if(!hasResultItem((Recipe) recipe)) {
                    return null;
                }
                return (Recipe) recipe;
            }
        }
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
        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
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
                frequency = (byte) (0.15D * ratio);
            }
        }
        setChanged();
        MultiblockHandler.instance.addIgnoreToUpdate(getBlockPos());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, controllerEnabled), Block.UPDATE_ALL);
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, controllerEnabled));
    }

    public void tickClient() {
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
        controllerEnabled = getMultiblock().isFormed() && hasBlackhole();

        fluxRegulators = getMultiblock().fluxRegulators();
        transformers = getMultiblock().transformers();
        stabilizers = getMultiblock().stabilizers();
        if (controllerEnabled) {
            trackChanges(contentHandler().tick());
            long wasMass = mass;
            updateBlackhole();
            trackChanges(false, wasMass != mass);
            handleMeltdown();
            trackChanges(processRecipe());
            handleRecipeOutput();
            if(!isBlackHoleStable()) {
                 getLevel().gameEvent(null, BLACKHOLE_VIBRATION.get(), getMultiblock().getCenter());
            }
        }
        refreshCacheFlag = !getMultiblock().isFormed();
        if(wasEnabled != controllerEnabled) {
           setChanged();
        }
        if(refreshCacheFlag || changed) {
            try {
                MultiblockHandler.instance.addIgnoreToUpdate(getBlockPos());
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, controllerEnabled), Block.UPDATE_ALL);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, controllerEnabled));
            } catch (NullPointerException ignored) {}
        }
    }

    private boolean isBlackHoleStable() {
        return blackholeStability > 39;
    }

    public boolean hasBlackhole() {
        if(getMultiblock().getBlackHole() instanceof BlackHoleBE) {
            if(getMultiblock().getBlackHole().isRemoved()) {
                getMultiblock().removeBlackHole();
                return false;
            }
            return true;
        }
        return false;
    }

    private void updateBlackhole()
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
        updateBlackholeStability();
    }

    private void updateBlackholeStability() {
        if (!hasBlackhole() || mass <= 0) {
            blackholeStability = 100;
            return;
        }

        if(getLevel().getGameTime() % 2 == 0 || getLevel().random.nextInt(128) < getMultiblock().stabilizers()) {
            return;
        }

        if(getLevel().random.nextDouble() > 0.98D) {
            blackholeStability++;
            return;
        }

        double massRange = MAX_MASS - MIN_MASS*5;
        double normalizedMass = (mass - MIN_MASS) / massRange;
        double distanceFromOptimal = Math.abs(normalizedMass - 0.5) * 2.0;

        // Base chance is 5%, increases up to 15% when at boundaries
        double decreaseChance = 0.05 + (distanceFromOptimal * 0.1);

        // Additional factor: extremely large masses are more unstable
        if (mass > MAX_MASS * 0.9) {
            decreaseChance += 0.1;
        }

        // Random check for stability decrease
        if (getLevel().random.nextDouble() < decreaseChance) {
            blackholeStability = Math.max(0, blackholeStability - 1);
            setChanged();
        }

        // Small chance to recover stability when in optimal mass range
        if (distanceFromOptimal < 0.3 && getLevel().random.nextDouble() < 0.02) {
            blackholeStability = Math.min(100, blackholeStability + 1);
            setChanged();
        }
    }

    private void updateEnergyGeneration() {
        int wasEnergy = energyPerTick;
        double massRatio = (double)MAX_MASS / Math.max(mass, MIN_MASS);
        energyPerTick = (int)(massRatio * 5000 * Math.log(energyConvertionRate*Math.log(fluxRegulators*4)+1));
        energyPerTick *= ENERGY_GENERATION.GENERATION_MULTIPLIER.get();
        energyPerTick *= KUGELBLITZ_CONFIG.GENERATION_MULTIPLIER.get();
        energyStorage().addEnergy(energyPerTick);
        if (wasEnergy != energyPerTick) {
            setChanged();
        }
    }

    private void updateEvaporation() {
        int wasEvaporation = evaporation;
        int rate = (int) Math.max(1, energyConvertionRate * 100 / Math.log(fluxRegulators));
        if (recipeInfo().recipe() != null && !recipeInfo().isCompleted()) {
            rate+= (int) ((100 - energyConvertionRate) * 100 / Math.log(transformers) );
        }
        rate = (int) Math.pow(rate, 1.2);
        double massRatio = Math.log((double)MAX_MASS / Math.max(mass, MIN_MASS));
        evaporation = (int)(rate * KUGELBLITZ_CONFIG.EVAPORATION_MULTIPLIER.get() * massRatio);

        if (blackholeStability < 20) {
            // At very low stability, risk of spontaneous mass change
            if (getLevel().random.nextDouble() < 0.1) {
                int fluctuation = (int)(mass * 0.0001 * (getLevel().random.nextDouble() - 0.5) * 2);
                evaporation += fluctuation;
            }
        }

        if (blackholeStability < 10) {
            if (getLevel().random.nextDouble() < 0.1) {
                int fluctuation = (int)(mass * 0.0005 * getLevel().random.nextDouble() * 20D/blackholeStability);
                evaporation += fluctuation;
            }
        }

        if (wasEvaporation != evaporation) {
            setChanged();
        }
    }

    private void doEvaporation() {
        if(hasBlackhole()) {
            getLevel().setBlockAndUpdate(getMultiblock().getBlackHole().getBlockPos(), AIR.defaultBlockState());
            mass = 0;
            feeding = 0;
            energyPerTick = 0;
            evaporation = 0;
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
            if (getMultiblock().isFormed() && hasBlackhole()) {
                getMultiblock().getBlackHole().meltdown();
                mass = 0;
                feeding = 0;
                energyPerTick = 0;
                evaporation = 0;
            }
        }
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
        double multiplier = Math.log10(transformers) * (100 - energyConvertionRate)/100D;
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

    private boolean hasResultItem(Recipe recipe) {
        ServerLevel serverLevel = (ServerLevel) getLevel();
        long seed = serverLevel.getSeed();

        if(orderedOutputs.isEmpty()) {
            //shuffle outputs dependently on seed
            orderedOutputs.addAll(getAllowedInputItems());
            Random seededRandom = new Random(seed);
            Collections.shuffle(orderedOutputs, seededRandom);
        }
        //result item exists only if frequency is matches seed based random frequency
        return frequency == getTargetFrequencyForItem(recipe.getResultItem(), seed);
    }

    public boolean recipeIsStuck() {
        return recipeInfo().isStuck();
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe() != null && hasResultItem((Recipe) recipeInfo().recipe());
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
        return LazyOptional.of(() -> KugelblitzDevice.createDevice(this)).cast();
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidCapability.tanks.get(i);
    }

    public void handleLaserBurst() {
        blackholeStability += 50 + getLevel().random.nextInt(50);
        if (blackholeStability > 100) {
            blackholeStability = 100;
        }
        mass += (getLevel().random.nextInt(200)+200) * 10000L;
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return "kugelblitz_chamber";
        }

        @Override
        public @NotNull String getGroup() {
            return "kugelblitz_chamber";
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(KUGELBLITZ_BLOCKS.get(NAME).get());
        }

        public int getBaseTime() {
            return (int) (timeModifier * 50);
        }

        public double getEnergy() { return powerModifier * 1000; }
    }
}
