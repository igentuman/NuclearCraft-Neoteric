package igentuman.nc.block.entity.fission;

import igentuman.nc.handler.NCItemStackHandler;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.recipes.*;
import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.recipes.cache.InputRecipeCache.SingleItem;
import igentuman.nc.recipes.cache.OneInputCachedRecipe;
import igentuman.nc.recipes.inputs.IInputHandler;
import igentuman.nc.recipes.inputs.InputHelper;
import igentuman.nc.recipes.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import igentuman.nc.recipes.lookup.monitor.RecipeCacheLookupMonitor;
import igentuman.nc.recipes.multiblock.FissionRecipe;
import igentuman.nc.recipes.outputs.IOutputHandler;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

import static igentuman.nc.handler.config.CommonConfig.FissionConfig.*;

public class FissionControllerBE extends FissionBE implements ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {

    public static String NAME = "fission_reactor_controller";
    public final NCItemStackHandler itemHandler = createHandler();
    public final CustomEnergyStorage energyStorage = createEnergy();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;
    @NBTField
    public double heat = 0;
    @NBTField
    public int fuelCellsCount = 0;
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
    public double heatSinkCooling = 0;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double heatMultiplier = 0;
    @NBTField
    public double efficiency = 0;
    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo recipeInfo = new RecipeInfo();
    private Direction facing;
    public FissionRecipe recipe;

    //protected final IInputHandler<@NotNull ItemStack> inputHandler;
    //protected final IOutputHandler<@NotNull ItemStack> outputHandler;

    @NotNull
    @Override
    public INcRecipeTypeProvider<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return NcRecipeType.RECIPES.get("fission_reactor");
    }

    @Nullable
    @Override
    public ItemStackToItemStackRecipe getRecipe() {
        return findFirstRecipe(itemHandler);
    }


    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
        multiblock = new FissionReactorMultiblock(this);
        //inputHandler = InputHelper.getInputHandler(mainInputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);

    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    private NCItemStackHandler createHandler() {
        return new NCItemStackHandler(3) {

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return true;//todo recipe validator
            }

            @Override
            @NotNull
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                //if(slot != 1) return ItemStack.EMPTY;
                return super.extractItem(slot, amount, simulate);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if(slot != 0) return ItemStack.EMPTY;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(1000000, 0, 1000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public void updateEnergyStorage() {
        energyStorage.setMaxCapacity(Math.max(fuelCellsCount, 1) * 1000000);
        energyStorage.setMaxExtract(Math.max(fuelCellsCount, 1) * 1000000);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    public void tickClient() {
    }

    public void tickServer() {
        if (!multiblock().isFormed()) {
            multiblock().validate();
            isCasingValid = multiblock().isOuterValid();
            isInternalValid = multiblock().isInnerValid();
        }

        if (multiblock().isFormed()) {
            height = multiblock().height();
            width = multiblock().width();
            depth = multiblock().depth();
            processReaction();
            coolDown();
            handleMeltdown();
        }
        refreshCacheFlag = !multiblock().isFormed();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private void handleMeltdown() {
        if (heat >= getMaxHeat()) {
            BlockPos explosionPos = getBlockPos().relative(getFacing(), 2);
            if (EXPLOSION_RADIUS.get() == 0) {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 2F, true, Explosion.BlockInteraction.NONE);
            } else {
                getLevel().explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), EXPLOSION_RADIUS.get().floatValue(), true, Explosion.BlockInteraction.DESTROY);
            }
            for (BlockPos pos : multiblock.fuelCells) {
                getLevel().setBlock(pos, NCFluids.getBlock("corium"), 1);
            }
            getLevel().setBlock(getBlockPos(), NCFluids.getBlock("corium"), 1);
            setRemoved();
            //at any case if reactor still works we punish player
            //heat = getMaxHeat();
            //energyStorage.setEnergy((int) (energyStorage.getEnergyStored() - calculateEnergy()));
        }

    }

    public void setRemoved() {
        super.setRemoved();
        if(multiblock() != null) {
            multiblock().onControllerRemoved();
        }
    }

    private void coolDown() {
        heat -= coolingPerTick();
        heat = Math.max(0, heat);
    }

    private boolean processReaction() {
        heatMultiplier = heatMultiplier() + collectedHeatMultiplier() - 1;
        if (!hasRecipe() && !itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
            updateRecipe();
        }
        if (hasRecipe()) {
            return process();
        }
        return false;
    }

    private boolean process() {
        recipeInfo.process(fuelCellsCount * (heatMultiplier() + collectedHeatMultiplier() - 1));
        if (!recipeInfo.isCompleted()) {
            energyStorage.addEnergy((int) calculateEnergy());
            heat += calculateHeat();
        }

        if (recipeInfo.isCompleted()) {
            handleRecipeOutput();
        }
        efficiency = calculateEfficiency();
        return true;
    }

    private void handleRecipeOutput() {
        if (recipeInfo.recipe != null) {
            if (itemHandler.insertItemInternal(1, recipeInfo.recipe.getResultItem(), true).isEmpty()) {
                itemHandler.insertItemInternal(1, recipeInfo.recipe.getResultItem(), false);
                itemHandler.extractItemInternal(2, 1, false);
                recipeInfo.reset();
            }
        } else {
            itemHandler.extractItemInternal(2, 1, false);
            recipeInfo.reset();
        }

    }

    public double heatMultiplier() {
        double h = heatPerTick();
        double c = Math.max(1, coolingPerTick());
        return Math.log10(h / c) / (1 + Math.exp(h / c * HEAT_MULTIPLIER.get())) + 1;
    }

    public double collectedHeatMultiplier() {
        return Math.min(HEAT_MULTIPLIER_CAP.get(), Math.pow((heat + getMaxHeat() / 8) / getMaxHeat(), 5) + 0.9999694824);
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
        heatPerTick = recipeInfo.heat * fuelCellsCount + moderatorsHeat();
        return heatPerTick;
    }

    private double calculateHeat() {
        return heatPerTick();
    }

    private int calculateEnergy() {
        energyPerTick = (int) ((recipeInfo.energy * fuelCellsCount + moderatorsFE()) * (heatMultiplier() + collectedHeatMultiplier() - 1));
        return energyPerTick;
    }

    public double moderatorsHeat() {
        return recipeInfo.heat * moderatorAttacmentsCount * (MODERATOR_HEAT_MULTIPLIER.get() / 100);
    }

    public double moderatorsFE() {
        return recipeInfo.energy * moderatorAttacmentsCount * (MODERATOR_FE_MULTIPLIER.get() / 100);
    }


    private void updateRecipe() {
        recipe = (FissionRecipe) getRecipe();
        if (recipeIsStuck()) return;


        if (recipe != null && itemHandler.getStackInSlot(2).isEmpty()) {
            ItemStack input = itemHandler.getStackInSlot(0).copy();
            input.setCount(1);
            itemHandler.extractItemInternal(0, 1, false);
            itemHandler.insertItemInternal(2, input, false);
            recipeInfo.setRecipe(recipe);
            recipeInfo.ticks = ((FissionRecipe) recipeInfo.recipe).getDepletionTime();
            recipeInfo.energy = ((FissionRecipe) recipeInfo.recipe).getEnergy();
            recipeInfo.heat = ((FissionRecipe) recipeInfo.recipe).getHeat();
            recipeInfo.radiation = ((FissionRecipe) recipeInfo.recipe).getRadiation();
        }
    }

    public boolean recipeIsStuck() {
        if (recipeInfo.isCompleted() || recipeInfo.recipe == null) {
            handleRecipeOutput();
            return !itemHandler.getStackInSlot(2).isEmpty();
        }
        return false;
    }

    public boolean hasRecipe() {
        return !recipeIsStuck() && recipeInfo.recipe != null;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }

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
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.put("Energy", energyStorage.serializeNBT());
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
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    private void loadClientData(CompoundTag tag) {
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
        tag.put("Info", infoTag);
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        saveTagData(infoTag);
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putInt("validationId", validationResult.id);
        infoTag.putLong("erroredBlock", errorBlockPos.asLong());
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

    public double getDepletionProgress() {
        return recipeInfo.getProgress();
    }

    public double getMaxHeat() {
        return 1000000;
    }

    public double calculateEfficiency() {
        return (double) calculateEnergy() / ((double) recipeInfo.energy / 100);
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

    @Override
    public void onContentsChanged() {

    }
}
