package igentuman.nc.block.entity.processor;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.setup.processors.ProcessorPrefab;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.sided.SidedContentHandler;
import igentuman.nc.util.sided.SlotModePair;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NCProcessorBE<RECIPE extends NcRecipe> extends NuclearCraftBE {

    protected String name;
    public static String NAME;
    public final SidedContentHandler contentHandler;
    protected final CustomEnergyStorage energyStorage;

    public final ItemStackHandler upgradesHandler = createHandler();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> upgradesHandler);

    private RECIPE recipe;

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected final LazyOptional<IEnergyStorage> energy;


    public RecipeInfo recipeInfo = new RecipeInfo();

    public ProcessorPrefab prefab() {
        if(prefab == null) {
            prefab = Processors.all().get(getName());
        }
        return prefab;
    }

    protected ProcessorPrefab prefab;

    private void updateRecipe() {
        recipe = (RECIPE) getRecipe();
        if (recipeIsStuck()) return;

    }

    public RECIPE getRecipe() {
        return recipe;
    }

    private void handleRecipeOutput() {

    }

    public boolean canProcessRecipeOutputs() {
        boolean canProcess = true;


        return canProcess;
    }

    public boolean recipeIsStuck() {
        if (recipeInfo.isCompleted() || recipeInfo.recipe == null) {
            handleRecipeOutput();
        }
        return false;
    }

    public boolean hasRecipe() {
        return !recipeIsStuck() && recipeInfo.recipe != null;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(prefab().config().getPower()*500, 10000, 0) {
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
        contentHandler.setProcessor(this);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    public void tickClient() {
    }

    public void tickServer() {
        contentHandler.tick();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public String getName() {
        return name;
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
}
