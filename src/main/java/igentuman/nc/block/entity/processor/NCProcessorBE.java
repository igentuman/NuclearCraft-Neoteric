package igentuman.nc.block.entity.processor;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.compat.cc.NCProcessorPeripheral;
import igentuman.nc.handler.CatalystHandler;
import igentuman.nc.handler.UpgradesHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.particle.FallingDustParticle;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.block.ProcessorBlock.ACTIVE;
import static igentuman.nc.handler.config.ProcessorsConfig.PROCESSOR_CONFIG;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraft.particles.RedstoneParticleData.REDSTONE;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class NCProcessorBE<RECIPE extends AbstractRecipe> extends NuclearCraftBE implements ITickableTileEntity {

    public static String NAME;
    public final SidedContentHandler contentHandler;
    protected final CustomEnergyStorage energyStorage;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    public final UpgradesHandler upgradesHandler = createUpgradesHandler();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> upgradesHandler);
    public final CatalystHandler catalystHandler = createCatalystHandler();

    protected boolean saveSideMapFlag = true;

    public boolean wasUpdated = true;

    protected RECIPE recipe;
    @NBTField
    public int speedMultiplier = 1;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public int energyMultiplier = 1;

    @NBTField
    public int redstoneMode = 0;

    @NBTField
    public boolean isActive = false;

    public int manualUpdateCounter = 40;

    private List<ItemStack> allowedInputs;
    private List<FluidStack> allowedFluids;

    public NCProcessorBE(TileEntityType<? extends NCProcessorBE<?>> tileEntityType) {
        super(tileEntityType);
        energy = null;
        energyStorage = null;
        contentHandler = null;
    }
    @Override
    public void tick() {
        if(level == null) return;
        if(level.isClientSide()) {
            tickClient();
        }
        if(!level.isClientSide()) {
            tickServer();
        }
    }
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected final LazyOptional<IEnergyStorage> energy;

    protected ProcessorPrefab prefab;

    protected int skippedTicks = 1;

    public RecipeInfo<RECIPE> recipeInfo = new RecipeInfo<RECIPE>();

    public ProcessorPrefab prefab() {
        if(prefab == null) {
            prefab = Processors.all().get(getName());
        }
        return prefab;
    }


    private LazyOptional<NCProcessorPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCProcessorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Override
    public ItemCapabilityHandler getItemInventory() {
        return contentHandler.itemHandler;
    }

    protected void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = (int) (getBaseProcessTime() * recipe.getTimeModifier());
            recipeInfo.energy = getBasePower() * recipe.getEnergy();
            recipeInfo.radiation = recipeInfo.recipe.getRadiation();
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
        RECIPE cachedRecipe = getCachedRecipe();
        if(cachedRecipe != null) return cachedRecipe;
        if(!NcRecipeType.ALL_RECIPES.containsKey(getName())) return null;
        if(getRecipes() == null) return null;
        for(AbstractRecipe recipe: getRecipes()) {
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

    protected int getBaseProcessTime() {
        return prefab().config().getTime();
    }

    protected int getBasePower() {
        return prefab().config().getPower();
    }

    protected void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo.isCompleted()) {
            if (recipe.handleOutputs(contentHandler)) {
                recipeInfo.clear();
            } else {
                recipeInfo.stuck = true;
            }
        }
    }

    public double speedMultiplier()
    {
        if(!prefab().supportSpeedUpgrade) return 1;
        int id = prefab().supportEnergyUpgrade ? 1 : 0;
        speedMultiplier = upgradesHandler.getStackInSlot(id).getCount()+1;
        return speedMultiplier;
    }

    public int energyPerTick()
    {
        double energy = recipe == null ? prefab().config().getPower() : recipe.getEnergy();
        energyPerTick = (int) (energy*energyMultiplier()*prefab().config().getPower());
        return energyPerTick;
    }

    public boolean recipeIsStuck() {
        if (recipeInfo.isCompleted() || recipeInfo.recipe == null) {
            handleRecipeOutput();
        }
        return false;
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe != null;
    }

    public int getEnergyCapacity()
    {
        return prefab().config().getPower()*5000;
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(getEnergyCapacity(), 100000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    protected CatalystHandler createCatalystHandler() {
        return new CatalystHandler(this);
    }
    protected UpgradesHandler createUpgradesHandler() {
        return new UpgradesHandler(this);
    }

    protected boolean gtEUSupported()
    {
        return PROCESSOR_CONFIG.GT_SUPPORT.get() == 2 || PROCESSOR_CONFIG.GT_SUPPORT.get() == 1;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_HANDLER_CAPABILITY) {
            return contentHandler.getItemCapability(side);
        }
        if (cap == FLUID_HANDLER_CAPABILITY) {
            return contentHandler.getFluidCapability(side);
        }
        if (cap == ENERGY) {
            if(prefab().config().getPower() > 0) {
                return energy.cast();
            }
            return LazyOptional.empty();
        }

        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
       /* if(isMekanismLoadeed()) {
            if(cap == Capabilities.GAS_HANDLER_CAPABILITY) {
                if(contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler.gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == Capabilities.SLURRY_HANDLER_CAPABILITY) {
                if(contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> contentHandler.getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }*/
        return super.getCapability(cap, side);
    }

    public NCProcessorBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(NCProcessors.PROCESSORS_BE.get(name).get(), pPos, pBlockState);
        this.name = name;
        prefab = Processors.all().get(name);
        contentHandler = new SidedContentHandler(
                prefab().getSlotsConfig().getInputItems(), prefab().getSlotsConfig().getOutputItems(),
                prefab().getSlotsConfig().getInputFluids(), prefab().getSlotsConfig().getOutputFluids());
        contentHandler.setBlockEntity(this);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    public void tickClient() {
        if(isActive && level.getRandom().nextInt(50) < 5) {
            BlockPos pos = worldPosition;
            Direction direction = getFacing();
            Direction.Axis direction$axis = direction.getAxis();
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5D;
            double d3 = 0.52D;
            double d4 = level.getRandom().nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = level.getRandom().nextDouble() * 6.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            level.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0, 0.0D);
            level.addParticle(REDSTONE, d0 + d5, d1 + d6, d2 + d7, 0, 0, 0);
        }
    }

    public List<ItemStack> getAllowedInputItems()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(AbstractRecipe recipe: getRecipes()) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    allowedInputs.addAll(Arrays.asList(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
    }

    protected List<NcRecipe> recipes;
    private List<NcRecipe> getRecipes() {
        if(recipes == null) {
            recipes = new ArrayList<>();
            if(level == null) {
                World level = NuclearCraft.instance.server.overworld();
            }
            if(level == null) return null;
            for (NcRecipe recipe: level.getRecipeManager().getAllRecipesFor(NcRecipeType.ALL_RECIPES.get(getName()))) {
                if(recipe.isIncomplete()) {
                    continue;
                }
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    protected int howMuchICanSkip()
    {
        if(energyPerTick() == 0) {
            return PROCESSOR_CONFIG.SKIP_TICKS.get();
        }
        return Math.min(((int)(energyStorage.getEnergyStored() / energyPerTick())),PROCESSOR_CONFIG.SKIP_TICKS.get());
    }

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        if(redstoneMode == 1 && !hasRedstoneSignal()) return;
        if(howMuchICanSkip() >= skippedTicks) {
            skippedTicks++;
            return;
        }
        boolean updated = manualUpdate();
        if(getRecipes() != null) {
            contentHandler.setAllowedInputItems(getAllowedInputItems());
            for (int i = 0; i < prefab().getSlotsConfig().getInputFluids(); i++) {
                contentHandler.setAllowedInputFluids(i, getAllowedInputFluids());
            }
        }
        processRecipe();
        handleRecipeOutput();
        updated = updated || contentHandler.tick();
        if(updated || wasUpdated) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, isActive));
           // level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, isActive), Block.UPDATE_ALL);
        }
        skippedTicks = 1;

    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedFluids == null) {
            allowedFluids = new ArrayList<>();
            if(getRecipes() == null) return null;
            for(NcRecipe recipe: getRecipes()) {
                for(FluidStackIngredient ingredient: recipe.getInputFluids()) {
                    allowedFluids.addAll(ingredient.getRepresentations());
                }
            }
        }
        return allowedFluids;
    }

    private boolean manualUpdate() {
        if(manualUpdateCounter > 0) {
            manualUpdateCounter--;
            return false;
        }
        manualUpdateCounter = 40;
        saveSideMapFlag = true;
        energyStorage.wasUpdated = true;
        upgradesHandler.wasUpdated = true;
        catalystHandler.wasUpdated = true;
        return true;
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }


    protected void processRecipe() {
        if(!hasRecipe()) {
            updateRecipe();
        }
        if(!hasRecipe()) {
            isActive = false;
            return;
        }

        if(energyStorage.getEnergyStored() < energyPerTick()*skippedTicks) {
            isActive = false;
            return;
        }
        recipeInfo.process(speedMultiplier()*skippedTicks);
        if(recipeInfo.radiation != 1D) {
            RadiationManager.get(getLevel()).addRadiation(getLevel(), (recipeInfo.radiation/1000000)*speedMultiplier()*skippedTicks, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        isActive = true;
        setChanged();
        if(!recipeInfo.isCompleted() && hasRecipe()) {
            energyStorage.consumeEnergy(energyPerTick()*skippedTicks);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        wasUpdated = true;
    }

    public int getSpeedUpgrades()
    {
        if(!prefab().supportSpeedUpgrade) return 1;
        return upgradesHandler.getStackInSlot(1).getCount();
    }

    public int energyMultiplier() {
        energyMultiplier = (int) Math.max(speedMultiplier()-1, Math.pow(speedMultiplier()-1, 2)+speedMultiplier()-Math.pow(getSpeedUpgrades(),2));
        return energyMultiplier;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        contentHandler.invalidate();
        energy.invalidate();
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.getCompound("Energy"));
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
        if (tag.contains("Info")) {
            CompoundNBT infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if(infoTag.contains("upgrades")) {
                upgradesHandler.deserializeNBT((CompoundNBT) (infoTag).get("upgrades"));
            }
            if(infoTag.contains("catalyst")) {
                catalystHandler.deserializeNBT((CompoundNBT) (infoTag).get("catalyst"));
            }
        }
        updateRecipeAfterLoad();
        super.load(state, tag);
    }

    private void updateRecipeAfterLoad() {
        if(recipe == null && recipeInfo != null && recipeInfo.recipe() != null) {
            recipe = recipeInfo.recipe();
        }
    }

    //used to save data to chunk
    public void saveAdditional(CompoundNBT tag) {

        contentHandler.saveSideMap();

        if(!tag.contains("Content")) {
            tag.put("Content", contentHandler.serializeNBT());
        }
        if(!tag.contains("Energy")) {
            tag.put("Energy", energyStorage.serializeNBT());
        }
        CompoundNBT infoTag = new CompoundNBT();
        saveTagData(infoTag);
        infoTag.put("upgrades", upgradesHandler.serializeNBT());
        infoTag.put("catalyst", catalystHandler.serializeNBT());
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        tag.put("Info", infoTag);
    }

    public double getProgress() {
        return recipeInfo.getProgress();
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    protected void loadClientData(CompoundNBT tag) {
        if (tag.contains("Info")) {
            CompoundNBT infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if(infoTag.contains("energy")) {
                energyStorage.setEnergy(infoTag.getInt("energy"));
            }
            if(infoTag.contains("upgrades")) {
                upgradesHandler.deserializeNBT((CompoundNBT) (infoTag).get("upgrades"));
            }
            if(infoTag.contains("catalyst")) {
                catalystHandler.deserializeNBT((CompoundNBT) (infoTag).get("catalyst"));
            }
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
    }

    @Override
    public @NotNull CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    protected void saveClientData(CompoundNBT tag) {
        CompoundNBT infoTag = new CompoundNBT();
        saveTagData(infoTag);
        if(saveSideMapFlag) {
            contentHandler.saveSideMap();
            saveSideMapFlag = false;
        }
        if(upgradesHandler.wasUpdated) {
            infoTag.put("upgrades", upgradesHandler.serializeNBT());
            upgradesHandler.wasUpdated = false;
        }
        if(catalystHandler.wasUpdated) {
            infoTag.put("catalyst", catalystHandler.serializeNBT());
            catalystHandler.wasUpdated = false;
        }
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        tag.put("Info", infoTag);
        tag.put("Content", contentHandler.serializeNBT());
        infoTag.putInt("energy", energyStorage.getEnergyStored());
    }

/*    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        int oldEnergy = energyStorage.getEnergyStored();

        CompoundNBT tag = pkt.getTag();
        handleUpdateTag(tag);

        if (oldEnergy != energyStorage.getEnergyStored()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }*/

    public int toggleSideConfig(int slotId, int direction) {
        setChanged();
        saveSideMapFlag = true;
        return contentHandler.toggleSideConfig(slotId, direction);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        return contentHandler.getSlotMode(direction, slotId);
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler.fluidCapability.tanks.get(i);
    }

    public void toggleRedstoneMode() {
        redstoneMode++;
        if (redstoneMode > 1) redstoneMode = 0;
        setChanged();
    //    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public CompoundNBT getTagForStack() {
        CompoundNBT data = new CompoundNBT();
        contentHandler.saveSideMap();
        data.put("Content", contentHandler.serializeNBT());
        data.put("Energy", energyStorage.serializeNBT());
        CompoundNBT infoTag = new CompoundNBT();
        saveTagData(infoTag);
        infoTag.put("upgrades", upgradesHandler.serializeNBT());
        infoTag.put("catalyst", catalystHandler.serializeNBT());
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        data.put("Info", infoTag);
        return data;
    }

    public List<Item> getAllowedCatalysts() {
        return Arrays.asList();
    }

    public int getRecipeProgress() {
        if(hasRecipe()) {
            return (int) (recipeInfo.getProgress()*100);
        }
        return 0;
    }

    public int getSlotsCount() {
        return prefab().getSlotsConfig().slotsCount();
    }

    public void voidSlotContent(int id) {
        if(id < 0 || id >= getSlotsCount()) return;
        contentHandler.voidSlot(id);
    }

    public Object[] getSlotContent(int id) {
        if(id < 0 || id >= getSlotsCount()) return new Object[]{};
        return contentHandler.getSlotContent(id);
    }

    public void voidFluidSlot(int slotId) {
        if(contentHandler != null) {
            contentHandler.voidFluidSlot(slotId);
        }
    }
}
