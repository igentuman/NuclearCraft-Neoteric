package igentuman.nc.recipe;

import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.setup.Registers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ResetNbtRecipe extends CustomRecipe {

    public ResetNbtRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !findTarget(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registers.RESET_NBT_SERIALIZER.get();
    }
}
