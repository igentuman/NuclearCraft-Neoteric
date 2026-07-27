package igentuman.nc.block_entity;

import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class NuclearFurnaceBE extends UniversalProcessorBE {

    public static final int COOK_TIME_TOTAL = 40;
    public static final int FUEL_BURN_TIME = 500;

    private static final int SMELT_SLOT = 0;
    private static final int FUEL_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    private static final TagKey<Item> URANIUM_INGOTS = TagUtil.ingotTag("uranium");

    @NBTField(syncToClient = true)
    public int burnTime = 0;
    @NBTField(syncToClient = true)
    public int burnDuration = 0;
    @NBTField(syncToClient = true)
    public int cookTime = 0;

    private boolean furnaceValidatorsInstalled = false;

    public NuclearFurnaceBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public boolean supportRecipes() {
        return false;
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        installFurnaceValidators();
        contentHandler.tick();

        boolean wasLit = burnTime > 0;
        boolean changed = false;

        if (burnTime > 0) burnTime--;

        boolean redstoneOk = redstoneMode == 0 || level == null || !level.hasNeighborSignal(worldPosition);
        SmeltingRecipe recipe = redstoneOk ? getActiveRecipe() : null;
        boolean canBurn = recipe != null && canFitOutput(recipe);

        if (burnTime <= 0 && canBurn && consumeFuel()) {
            changed = true;
        }

        if (burnTime > 0 && canBurn) {
            cookTime++;
            if (cookTime >= COOK_TIME_TOTAL) {
                cookTime = 0;
                smeltOne(recipe);
                changed = true;
            }
        } else if (cookTime > 0) {
            cookTime = Math.max(0, cookTime - 2);
        }

        maxProgress = COOK_TIME_TOTAL;
        progress = cookTime;

        if (wasLit != (burnTime > 0)) {
            updateLitState(burnTime > 0);
            changed = true;
        }

        if (changed) wasChanged = true;
        if (wasChanged) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
            wasChanged = false;
        }
    }

    private void installFurnaceValidators() {
        if (furnaceValidatorsInstalled || level == null) return;
        ItemCapabilityHandler handler = contentHandler.getItemHandler();
        handler.setSlotValidator(SMELT_SLOT, stack -> hasSmeltingRecipe(stack));
        handler.setSlotValidator(FUEL_SLOT, NuclearFurnaceBE::isFuel);
        handler.setSideInsertLocked(false);
        furnaceValidatorsInstalled = true;
    }

    private static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && stack.is(URANIUM_INGOTS);
    }

    private boolean hasSmeltingRecipe(ItemStack stack) {
        if (stack.isEmpty() || level == null) return false;
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .isPresent();
    }

    private SmeltingRecipe getActiveRecipe() {
        if (level == null) return null;
        ItemStack input = contentHandler.getItemHandler().getStackInSlot(SMELT_SLOT);
        if (input.isEmpty()) return null;
        Optional<RecipeHolder<SmeltingRecipe>> holder = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        return holder.map(RecipeHolder::value).orElse(null);
    }

    private ItemStack resultOf(SmeltingRecipe recipe) {
        ItemStack input = contentHandler.getItemHandler().getStackInSlot(SMELT_SLOT);
        return recipe.assemble(new SingleRecipeInput(input), level.registryAccess()).copy();
    }

    private boolean canFitOutput(SmeltingRecipe recipe) {
        ItemStack result = resultOf(recipe);
        if (result.isEmpty()) return false;
        ItemStack existing = contentHandler.getItemHandler().getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private boolean consumeFuel() {
        ItemCapabilityHandler handler = contentHandler.getItemHandler();
        ItemStack fuel = handler.getStackInSlot(FUEL_SLOT);
        if (!isFuel(fuel)) return false;
        handler.extractItem(FUEL_SLOT, 1, false);
        burnTime = FUEL_BURN_TIME;
        burnDuration = FUEL_BURN_TIME;
        return true;
    }

    private void smeltOne(SmeltingRecipe recipe) {
        ItemCapabilityHandler handler = contentHandler.getItemHandler();
        ItemStack result = resultOf(recipe);
        ItemStack existing = handler.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            handler.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            existing.grow(result.getCount());
        }
        handler.extractItem(SMELT_SLOT, 1, false);
    }

    private void updateLitState(boolean lit) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (!state.hasProperty(UniversalProcessorBlock.POWERED)) return;
        if (state.getValue(UniversalProcessorBlock.POWERED) != lit) {
            level.setBlock(worldPosition, state.setValue(UniversalProcessorBlock.POWERED, lit), Block.UPDATE_ALL);
        }
    }
}
