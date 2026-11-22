package igentuman.nc.block_entity;

import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
        int previousBurnTime = burnTime;
        int previousCookTime = cookTime;
        boolean changed = false;

        if (burnTime > 0) burnTime--;

        boolean redstoneOk = redstoneMode == 0 || level == null || !level.hasNeighborSignal(worldPosition);
        SmeltingRecipe recipe = redstoneOk ? getActiveRecipe() : null;
        boolean canBurn = recipe != null && canFitOutput(recipe);

        if (burnTime <= 0 && canBurn && consumeFuel()) {
            changed = true;
        }

        if (burnTime > 0 && canBurn) {
            cookTime = Math.min(COOK_TIME_TOTAL, cookTime + 1);
            if (cookTime >= COOK_TIME_TOTAL && smeltOne(recipe)) {
                cookTime = 0;
                changed = true;
            }
        } else if (cookTime > 0) {
            cookTime = Math.max(0, cookTime - 2);
        }

        maxProgress = COOK_TIME_TOTAL;
        progress = cookTime;
        if (previousBurnTime != burnTime || previousCookTime != cookTime) setChanged();

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
        if (stack.isEmpty() || !(level instanceof ServerLevel serverLevel)) return false;
        return serverLevel.recipeAccess()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), serverLevel)
                .isPresent();
    }

    private SmeltingRecipe getActiveRecipe() {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        ItemStack input = contentHandler.getItemHandler().getStackInSlot(SMELT_SLOT);
        if (input.isEmpty()) return null;
        Optional<RecipeHolder<SmeltingRecipe>> holder = serverLevel.recipeAccess()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), serverLevel);
        return holder.map(RecipeHolder::value).orElse(null);
    }

    private ItemStack resultOf(SmeltingRecipe recipe) {
        ItemStack input = contentHandler.getItemHandler().getStackInSlot(SMELT_SLOT);
        return recipe.assemble(new SingleRecipeInput(input));
    }

    private boolean canFitOutput(SmeltingRecipe recipe) {
        ItemStack result = resultOf(recipe);
        if (result.isEmpty()) return false;
        try (var transaction = Transaction.openRoot()) {
            return contentHandler.getItemHandler().insert(OUTPUT_SLOT, ItemResource.of(result),
                    result.getCount(), transaction) == result.getCount();
        }
    }

    private boolean consumeFuel() {
        ItemCapabilityHandler handler = contentHandler.getItemHandler();
        ItemStack fuel = handler.getStackInSlot(FUEL_SLOT);
        if (!isFuel(fuel)) return false;
        try (var transaction = Transaction.openRoot()) {
            if (handler.extract(FUEL_SLOT, ItemResource.of(fuel), 1, transaction) != 1) return false;
            transaction.commit();
        }
        burnTime = FUEL_BURN_TIME;
        burnDuration = FUEL_BURN_TIME;
        return true;
    }

    private boolean smeltOne(SmeltingRecipe recipe) {
        ItemCapabilityHandler handler = contentHandler.getItemHandler();
        ItemResource input = handler.getResource(SMELT_SLOT);
        if (input.isEmpty()) return false;
        ItemStack result = resultOf(recipe);
        if (result.isEmpty()) return false;
        try (var transaction = Transaction.openRoot()) {
            if (handler.extract(SMELT_SLOT, input, 1, transaction) != 1
                    || handler.insert(OUTPUT_SLOT, ItemResource.of(result), result.getCount(), transaction)
                    != result.getCount()) return false;
            transaction.commit();
        }
        return true;
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
