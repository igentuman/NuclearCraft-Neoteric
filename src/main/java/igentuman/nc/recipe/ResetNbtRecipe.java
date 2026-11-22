package igentuman.nc.recipe;

import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.setup.Registers;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Clears modified machine, battery and barrel components through single-item crafting. */
public class ResetNbtRecipe extends CustomRecipe {

    /** Stateless; StreamCodec.unit compares by equals(), so every load must yield this same instance. */
    public static final ResetNbtRecipe INSTANCE = new ResetNbtRecipe();

    public static RecipeSerializer<ResetNbtRecipe> createSerializer() {
        return new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !findTarget(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack target = findTarget(input);
        return target.isEmpty() ? ItemStack.EMPTY : new ItemStack(target.getItem());
    }

    private static ItemStack findTarget(CraftingInput input) {
        if (input.ingredientCount() != 1) return ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            return isResettable(stack) && !stack.getComponentsPatch().isEmpty() ? stack : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isResettable(ItemStack stack) {
        return stack.getItem() instanceof BatteryBlockItem
                || stack.getItem() instanceof BarrelBlockItem
                || stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof UniversalProcessorBlock;
    }

    @Override
    public RecipeSerializer<ResetNbtRecipe> getSerializer() {
        return Registers.RESET_NBT_SERIALIZER.get();
    }
}
