package igentuman.nc.block.entity.accelerator;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.cc.LinearAcceleratorPeripheral;
import igentuman.nc.compat.oc2.LinearAcceleratorDevice;
import igentuman.nc.content.particles.CapabilityParticleStackHandler;
import igentuman.nc.content.particles.IParticleStackHandler;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.oc2.FissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.materials.Materials.subliquid_matter;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.ION_SOURCES;
import static igentuman.nc.util.Equations.*;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class LinearAcceleratorControllerBE extends MultiblockControllerBE {

    public static String NAME = "linear_accelerator_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    private LazyOptional<LinearAcceleratorPeripheral> peripheralCap;
    protected final LazyOptional<IEnergyStorage> energy;
    protected final LazyOptional<IParticleStackHandler> particleHandler;
    protected final ParticleStorage particleStorage;

    @NBTField
    public boolean hasParticle = false;
    @NBTField
    public int coolers;
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

    protected Direction facing;
    public Recipe recipe;
    public HashMap<String, Recipe> cachedRecipes = new HashMap<>();
    private List<ItemStack> allowedInputs;
    private List<FluidStack> allowedInputFluids;


    public LinearAcceleratorControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 1, 1000);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler.fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        contentHandler.setBlockEntity(this);
        contentHandler.setAllowedInputFluids(0, this::getAllowedInputFluids);
        particleStorage = new ParticleStorage();
        particleHandler = CapabilityParticleStackHandler.createHandler(particleStorage);
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(RegistryObject<Item> item: ION_SOURCES.values()) {
                allowedInputs.add(new ItemStack(item.get()));
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
        return new CustomEnergyStorage(100000000, 100000000, 0) {
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
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return particleHandler.cast();
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
            trackChanges(accelerateParticle());
        }
        heat -= coolingRate;

        refreshCacheFlag = !getMultiblock().isFormed();
        if(wasEnabled != controllerEnabled) {
           setChanged();
        }
        if(refreshCacheFlag || changed) {
            try {
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

    private boolean accelerateParticle() {
        hasParticle = false;
        if(particleStorage.getParticle() == null) {
            getParticleFromSourceItem();
        }
        if(particleStorage.getParticle() == null) {
            return false;
        }
        if(!drainEnergy()) {
            //return false;
        }
        ParticleStack particleStack = particleStorage.getParticle();
        particleStack.setFocus(focusGain(focus, particleStack)-focusLoss(Math.max(width, depth), particleStack));
        particleStack.setMeanEnergy(linacEnergyGain(acceleratingVoltage, particleStack));
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

    private void getParticleFromSourceItem() {
        ItemStack stack = contentHandler().itemHandler.getStackInSlot(0);
        if(stack.getItem() instanceof ParticleSourceItem sourceItem) {
            stack = sourceItem.use(stack, 10000);
            ParticleStack particle = sourceItem.getParticleStack(stack);
            if (particle != null) {
                particleStorage.setParticleStack(particle);
                contentHandler().itemHandler.setStackInSlot(0, stack);
            }
        }
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

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> LinearAcceleratorDevice.createDevice(this)).cast();
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidCapability.tanks.get(i);
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

    public ParticleStack getParticleStack() {
        return particleStorage.getParticle();
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
