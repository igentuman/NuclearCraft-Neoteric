package igentuman.nc.block.accelerator.entity;

import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.content.particles.*;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.setup.registration.NCItems.ION_SOURCES;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class AbstractAcceleratorControllerBE extends MultiblockControllerBE {

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

    protected final ParticleStorage particleStorage;
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
    }

    public ParticleStack getParticleStack() {
        return particleStorage.getParticle();
    }

    public ParticleStorage getParticleStorage() {
        return particleStorage;
    }

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
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal();
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(DeferredHolder<Item, Item> item: ION_SOURCES.values()) {
                allowedInputs.add(new ItemStack(item.get()));
            }
        }
        return allowedInputs;
    }

    //todo implement
    public int getMinEnergy() {
        return 0;
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
            particleStorage.clear();
        }
    }

    @Override
    public void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadClientData(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
        }
    }
    @Override
    protected void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveClientData(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
            particleStorage.clear();
        }
    }


    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
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
        energyStorage().extractEnergy(energyRequired, false);
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

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidHandler.tanks.get(i);
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }


}
