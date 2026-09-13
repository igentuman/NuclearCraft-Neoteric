package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.content.particles.*;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.block.accelerator.AcceleratorPortBlock.POWERED;
import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.radiation.ItemRadiation.getItemByName;
import static igentuman.nc.setup.registration.NCItems.ION_SOURCES;
import static net.minecraft.world.item.Items.AIR;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class AbstractAcceleratorControllerBE extends MultiblockControllerBE {

    @NBTField
    public double accelerationEnergy = 1;
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
    public int heatStored = 0;
    @NBTField
    public long heatCapacity = 0;
    @NBTField
    public long currentHeating = 0;
    @NBTField
    public int ambientTemp = 290;
    @NBTField
    public boolean thermalInitialized = false;
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
    public boolean energyIsTooLow = false;
    @NBTField
    public boolean energyIsTooHigh = false;
    @NBTField
    public int overheatCooldown = 0;
    protected double initialFocus = 0D;

    protected final LazyOptional<IParticleStackHandler> particleHandler;
    protected final ParticleStorage particleStorage;
    protected final LazyOptional<IEnergyStorage> energy;
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    private List<ItemStack> allowedInputs;
    private List<FluidStack> allowedInputFluids;
    protected LinearAcceleratorControllerBE.CoolantRecipe coolantRecipe;
    protected List<LinearAcceleratorControllerBE.CoolantRecipe> coolantRecipes;
    private List<FluidStack> allowedCoolants;
    private List<FluidStack> allowedCoolantsOutput;

    protected AbstractAcceleratorControllerBE(BlockEntityType<?> pType, BlockPos pos, BlockState state) {
        super(pType, pos, state);
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


    public LazyOptional<IParticleStackHandler> particleHandler() {
        return particleHandler;
    }

    public ParticleStack getParticleStack() {
        return particleStorage.getParticle();
    }

    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            return;
        }
        lastTickTime = currentTick;
        changed = false;
        super.tickServer();
        boolean wasEnabled = controllerEnabled;
        handleValidation();

        boolean formed = getMultiblock().isFormed();
        if(formed && !thermalInitialized) {
            initThermal();
        } else if(!formed && thermalInitialized) {
            resetThermal();
        }
        tickOverheatCooldown();
        if(!isControlledByComputer && currentTick % 10 == 0) {
            int maxSignal = getRedstoneSignal();
            for (igentuman.nc.block.entity.MultiblockPortBE port : getMultiblock().getPorts()) {
                if (port instanceof AcceleratorPortBE accPort && accPort.redstoneMode == AcceleratorPortBE.SignalSource.INPUT) {
                    maxSignal = Math.max(maxSignal, accPort.getRedstoneSignal());
                }
            }
            analogSignal = (byte) maxSignal;
            accelerationEnergy = analogSignal / 15D;
        }
        controllerEnabled = formed && !isOverheated() && (analogSignal > 0 || (accelerationEnergy > 0 && externalControlled));
        externalControlled = false;
        if (wasEnabled != controllerEnabled) {
            particleStorage.clearClient();
        }
        currentHeating = 0;
        if (formed) {
            externalHeating();
        }
        if (controllerEnabled) {
            handleOverheat();
            if(controllerEnabled && hasEnoughEnergy()) {
                trackChanges(contentHandler().tick());
                trackChanges(accelerateParticle());
            }
            handleOverheat();
        } else {
            if(particleStorage.getParticleStack() != null) {
                particleStorage.clearAll();
                hasParticle = false;
                changed = true;
            }
        }
        coolantCoolDown();
        refreshCacheFlag = !formed;
        changed |= wasEnabled != controllerEnabled;
        if(refreshCacheFlag || changed || currentTick % 20 == 0) {
            try {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, controllerEnabled), Block.UPDATE_ALL);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, controllerEnabled));
            } catch (NullPointerException ignored) {}
        }
    }

    protected abstract boolean accelerateParticle();

    public CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get();
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputFluids == null) {
            allowedInputFluids = new ArrayList<>();
            for(String name: ParticleSources.fluidSources.keySet()) {
                allowedInputFluids.addAll(IngredientCreatorAccess.fluid().from(name, 1).getRepresentations());
            }
        }
        return allowedInputFluids;
    }

    public List<CoolantRecipe> getCoolantRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<CoolantRecipe>) NcRecipeType.getAllRecipesFor("accelerator_coolant", getLevel());
        }
        return coolantRecipes;
    }

    protected List<FluidStack> getAllowedCoolants() {
        if(allowedCoolants == null) {
            allowedCoolants = new ArrayList<>();
            for(CoolantRecipe recipe : getCoolantRecipes()) {
                allowedCoolants.addAll(recipe.getInputFluids(0));
            }
        }
        return allowedCoolants;
    }

    protected List<FluidStack> getAllowedCoolantsOutput() {
        if(allowedCoolantsOutput == null) {
            allowedCoolantsOutput = new ArrayList<>();
            for(CoolantRecipe recipe : getCoolantRecipes()) {
                allowedCoolantsOutput.addAll(recipe.getOutputFluids(0));
            }
        }
        return allowedCoolantsOutput;
    }

    @Override
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal();
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(String item: ParticleSources.sources.keySet()) {
                Item sourceItem = AIR;
                if(ION_SOURCES.containsKey(item)) {
                    sourceItem = ION_SOURCES.get(item).get();
                } else {
                    sourceItem = getItemByName(item);
                }
                allowedInputs.add(new ItemStack(sourceItem));
            }
        }
        return allowedInputs;
    }

    public int getMinEnergy() {
        return 1;
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
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
        }
    }
    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
        }
    }


    public boolean isProcessing() {
        return hasParticle && controllerEnabled;
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

    protected boolean drainEnergy() {
        if(energyStorage().getEnergyStored() < energyRequired) {
            return false;
        }
        energyStorage().consumeEnergy(energyRequired);
        return true;
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

    protected boolean hasEnoughEnergy() {
        return energyStorage().getEnergyStored() >= energyRequired;
    }

    protected void coolantCoolDown() {
        if(heatStored <= 0 || coolingRate <= 0) return;
        if(!hasCoolant()) return;
        if(getTemperature() < coolantRecipe.getInputFluids(0).get(0).getFluid().getFluidType().getTemperature()) {
            return;
        }
        int heatPerMB = (int) Math.max(1, coolantRecipe.getCoolingRate());
        int inputAmountPerOp = coolantRecipe.getInputFluids()[0].getAmount();
        int outputAmountPerOp = coolantRecipe.getOutputFluids().get(0).getAmount();
        if(inputAmountPerOp <= 0 || outputAmountPerOp <= 0) return;

        long maxHeatChange = Math.min(heatStored, (long) coolingRate);
        long opsByThroughput = maxHeatChange / ((long) heatPerMB * inputAmountPerOp);

        FluidTank inputTank = contentHandler().fluidHandler.tanks.get(2);
        FluidTank outputTank = contentHandler().fluidHandler.tanks.get(3);
        long opsByCoolant = inputTank.getFluidAmount() / inputAmountPerOp;
        long outputSpace = (long) outputTank.getCapacity() - outputTank.getFluidAmount();
        long opsByOutput = outputSpace / outputAmountPerOp;

        long ops = Math.min(opsByThroughput, Math.min(opsByCoolant, opsByOutput));
        if(ops <= 0) return;

        long heatRemoved = ops * heatPerMB * inputAmountPerOp;
        heatStored -= (int) Math.min(heatStored, heatRemoved);
        if(heatStored < 0) heatStored = 0;
        currentHeating -= heatRemoved;

        extractCoolant((int) ops);
    }

    protected void extractCoolant(int ops) {
        if(coolantRecipe != null && ops > 0) {
            contentHandler().fluidHandler.tanks.get(2).drain(coolantRecipe.getInputFluids()[0].getAmount() * ops, EXECUTE);
            FluidStack output = coolantRecipe.getOutputFluids().get(0).copy();
            output.setAmount(output.getAmount() * ops);
            contentHandler().fluidHandler.tanks.get(3).fill(output, EXECUTE);
        }
    }

    public int getMaxTemp() {
        return ACCELERATOR_CONFIG.MAX_TEMP.get();
    }

    public int getTemperature() {
        if(heatCapacity <= 0) return ambientTemp;
        return Math.round(400 * (float) heatStored / heatCapacity);
    }

    public long getExteriorSurfaceArea() {
        AbstractAcceleratorMultiblock mb = getAcceleratorMultiblock();
        if(mb == null) return 0L;
        return mb.getExteriorSurfaceArea();
    }

    public long getExternalHeating() {
        return (long) ((ambientTemp - getTemperature()) * ACCELERATOR_CONFIG.THERMAL_CONDUCTIVITY.get() * getExteriorSurfaceArea());
    }

    protected void externalHeating() {
        long delta = getExternalHeating();
        applyHeatDelta(delta);
        currentHeating += delta;
    }

    protected void internalHeating(long delta) {
        applyHeatDelta(delta);
        currentHeating += delta;
    }

    protected void applyHeatDelta(long delta) {
        long newHeat = (long) heatStored + delta;
        if(newHeat < 0) newHeat = 0;
        if(newHeat > heatCapacity) newHeat = heatCapacity;
        heatStored = (int) Math.min(Integer.MAX_VALUE, newHeat);
    }

    protected AbstractAcceleratorMultiblock getAcceleratorMultiblock() {
        return null;
    }

    public void initThermal() {
        AbstractAcceleratorMultiblock mb = getAcceleratorMultiblock();
        if(mb == null || level == null) return;

        long baseCapacity = (long) ACCELERATOR_CONFIG.BASE_HEAT_CAPACITY.get();
        heatCapacity = baseCapacity * mb.getCapacityMultiplier();
        if(heatCapacity <= 0) heatCapacity = baseCapacity;

        float biomeTemp = level.getBiome(worldPosition).value().getBaseTemperature();
        ambientTemp = 273 + (int) (biomeTemp * 10F);

        if(!thermalInitialized) {
            heatStored = (int) Math.min(Integer.MAX_VALUE, ambientTemp * heatCapacity / getMaxTemp());
            thermalInitialized = true;
        }
    }

    public void resetThermal() {
        thermalInitialized = false;
        currentHeating = 0;
    }

    protected void handleOverheat() {
        if(!controllerEnabled || isOverheated() || !isAcceleratorTooHot()) return;
        overheatCooldown = ACCELERATOR_CONFIG.OVERHEAT_COOLDOWN_TICKS.get();
        controllerEnabled = false;
        particleStorage.clearAll();
        hasParticle = false;
        changed = true;
    }

    protected void tickOverheatCooldown() {
        if(overheatCooldown <= 0) return;
        overheatCooldown--;
        if(overheatCooldown == 0) {
            changed = true;
        }
    }

    public boolean isOverheated() {
        return overheatCooldown > 0;
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidHandler.tanks.get(i);
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.empty();
    }

    public boolean isAcceleratorTooHot() {
        return getTemperature() > maxTemperature;
    }

    public ParticleStorage getParticleStorage() {
        return particleStorage;
    }

    public static class CoolantRecipe extends NcRecipe {
        protected double coolingRate;

        public CoolantRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double temperature, double powerModifier, double radiation, double rar) {
            super(id, input, output, inputFluids, outputFluids, temperature, powerModifier, radiation, rar);
            coolingRate = temperature;
        }

        @Override
        public @NotNull String getGroup() {
            return "accelerator_coolant";
        }

        @Override
        public String getCodeId() {
            return "accelerator_coolant";
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(ACCELERATOR_BLOCKS.get("accelerator_port").get());
        }

        public double getCoolingRate() {
            return Math.max(rarityModifier, 1);
        }
    }
}
