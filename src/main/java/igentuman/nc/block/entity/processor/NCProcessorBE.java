package igentuman.nc.block.entity.processor;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.setup.processors.ProcessorPrefab;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class NCProcessorBE<RECIPE extends NcRecipe> extends NuclearCraftBE {

    public static String NAME;
    public final SidedContentHandler contentHandler;
    protected final CustomEnergyStorage energyStorage;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    public final ItemStackHandler upgradesHandler = createHandler();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> upgradesHandler);

    private RECIPE recipe;
    @NBTField
    public int speedMultiplier = 1;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public int energyMultiplier = 1;

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected final LazyOptional<IEnergyStorage> energy;

    protected ProcessorPrefab prefab;

    public RecipeInfo<RECIPE> recipeInfo = new RecipeInfo<RECIPE>();

    public ProcessorPrefab prefab() {
        if(prefab == null) {
            prefab = Processors.all().get(getName());
        }
        return prefab;
    }

    @Override
    public ItemCapabilityHandler getItemInventory() {
        return contentHandler.itemHandler;
    }

    private void updateRecipe() {
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

    private void addToCache(RECIPE recipe) {
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
        for(NcRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
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

    private int getBaseProcessTime() {
        return prefab().config().getTime();
    }

    private int getBasePower() {
        return prefab().config().getPower();
    }

    private void handleRecipeOutput() {
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
        speedMultiplier = upgradesHandler.getStackInSlot(0).getCount()+1;
        return speedMultiplier;
    }

    public int energyPerTick()
    {
        energyPerTick = (int) (recipe.getEnergy()*energyMultiplier()*prefab().config().getPower());
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

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(prefab().config().getPower()*5000, 100000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(prefab().getUpgradesSlots()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return true;
            }
        };
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
    }

    public void tickServer() {
        processRecipe();
        handleRecipeOutput();
        contentHandler.tick();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private void processRecipe() {
        if(!hasRecipe()) {
            updateRecipe();
        }
        if(!hasRecipe()) return;

        if(energyStorage.getEnergyStored() < energyPerTick()) return;
        recipeInfo.process(speedMultiplier());
        if(!recipeInfo.isCompleted()) {
            energyStorage.consumeEnergy(energyPerTick());
        }
    }

    public int energyMultiplier() {
        energyMultiplier = (int) Math.max(speedMultiplier()-1, Math.pow(speedMultiplier()-1, 2)+speedMultiplier()-Math.pow(upgradesHandler.getStackInSlot(1).getCount(),2));
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
            upgradesHandler.deserializeNBT((CompoundTag) (infoTag).get("upgrades"));
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("Content", contentHandler.serializeNBT());
        tag.put("Energy", energyStorage.serializeNBT());

        CompoundTag infoTag = new CompoundTag();
        saveTagData(infoTag);
        infoTag.put("upgrades", upgradesHandler.serializeNBT());
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

    private void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            energyStorage.setEnergy(infoTag.getInt("energy"));
            upgradesHandler.deserializeNBT((CompoundTag) (infoTag).get("upgrades"));
        }
        if (tag.contains("Content")) {
            contentHandler.deserializeNBT(tag.getCompound("Content"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    private void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        saveTagData(infoTag);
        infoTag.put("upgrades", upgradesHandler.serializeNBT());
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
            //requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void toggleSideConfig(int slotId, int direction) {
        contentHandler.toggleSideConfig(slotId, direction);
        setChanged();
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
}
