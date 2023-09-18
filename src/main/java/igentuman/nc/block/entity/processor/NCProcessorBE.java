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
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.block.ProcessorBlock.ACTIVE;
import static igentuman.nc.handler.config.CommonConfig.PROCESSOR_CONFIG;
import static igentuman.nc.util.ModUtil.isCcLoaded;

public class NCProcessorBE<RECIPE extends AbstractRecipe> extends NuclearCraftBE {

    public static String NAME;
    public final SidedContentHandler contentHandler;
    protected final CustomEnergyStorage energyStorage;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    public final UpgradesHandler upgradesHandler = createUpgradesHandler();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> upgradesHandler);
    public final CatalystHandler catalystHandler = createCatalystHadnler();

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
            recipe.extractInputs(contentHandler);
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

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(prefab().config().getPower()*5000, 100000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    protected CatalystHandler createCatalystHadnler() {
        return new CatalystHandler(this);
    }
    protected UpgradesHandler createUpgradesHandler() {
        return new UpgradesHandler(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return contentHandler.getItemCapability(side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler.getFluidCapability(side);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
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
            level.addParticle(DustParticleOptions.REDSTONE, d0 + d5, d1 + d6, d2 + d7, 0, 0, 0);
        }
    }

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

    protected int howMuchICanSkip()
    {
        return Math.min(((int)(energyStorage.getEnergyStored() / energyPerTick())),PROCESSOR_CONFIG.SKIP_TICKS.get());
    }

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        if(redstoneMode == 1 && !hasRedstoneSignal()) return;
        if(howMuchICanSkip() >= skippedTicks) {
            skippedTicks++;
            return;
        }
        boolean updated = manualUpdate();
        contentHandler.setAllowedInputItems(getAllowedInputItems());
        processRecipe();
        handleRecipeOutput();
        updated = updated || contentHandler.tick();
        if(updated || wasUpdated) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, isActive));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, isActive), Block.UPDATE_ALL);
        }
        skippedTicks = 1;

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
    public void load(CompoundTag tag) {
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if(infoTag.contains("upgrades")) {
                upgradesHandler.deserializeNBT((CompoundTag) (infoTag).get("upgrades"));
            }
            if(infoTag.contains("catalyst")) {
                catalystHandler.deserializeNBT((CompoundTag) (infoTag).get("catalyst"));
            }
        }
        updateRecipeAfterLoad();
        super.load(tag);
    }

    private void updateRecipeAfterLoad() {
        if(recipe == null && recipeInfo != null && recipeInfo.recipe() != null) {
            recipe = recipeInfo.recipe();
        }
    }

    //used to save data to chunk
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        contentHandler.saveSideMap();

        if(!tag.contains("Content")) {
            tag.put("Content", contentHandler.serializeNBT());
        }
        if(!tag.contains("Energy")) {
            tag.put("Energy", energyStorage.serializeNBT());
        }
        CompoundTag infoTag = new CompoundTag();
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
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    protected void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if(infoTag.contains("energy")) {
                energyStorage.setEnergy(infoTag.getInt("energy"));
            }
            if(infoTag.contains("upgrades")) {
                upgradesHandler.deserializeNBT((CompoundTag) (infoTag).get("upgrades"));
            }
            if(infoTag.contains("catalyst")) {
                catalystHandler.deserializeNBT((CompoundTag) (infoTag).get("catalyst"));
            }
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    protected void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
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

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public CompoundTag getTagForStack() {
        CompoundTag data = new CompoundTag();
        contentHandler.saveSideMap();
        data.put("Content", contentHandler.serializeNBT());
        data.put("Energy", energyStorage.serializeNBT());
        CompoundTag infoTag = new CompoundTag();
        saveTagData(infoTag);
        infoTag.put("upgrades", upgradesHandler.serializeNBT());
        infoTag.put("catalyst", catalystHandler.serializeNBT());
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        data.put("Info", infoTag);
        return data;
    }

    public List<Item> getAllowedCatalysts() {
        return List.of();
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
}
