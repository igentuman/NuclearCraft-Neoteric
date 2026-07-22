package igentuman.nc.handler.crafter;

import igentuman.nc.item.CraftingPatternItem;
import igentuman.nc.setup.Registers;
import igentuman.nc.setup.entries.Crafter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CraftingPattern {

    public static final int GRID_SIZE = 9;

    private final NonNullList<ItemStack> inputs;
    private final ItemStack output;

    public CraftingPattern(NonNullList<ItemStack> inputs, ItemStack output) {
        this.inputs = inputs;
        this.output = output;
    }

    public NonNullList<ItemStack> inputs() {
        return inputs;
    }

    public ItemStack output() {
        return output;
    }

    public static boolean isPatternItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CraftingPatternItem;
    }

    public static boolean isEncoded(ItemStack stack) {
        return isPatternItem(stack) && stack.has(Registers.CRAFTING_PATTERN.get());
    }

    public static boolean isBlank(ItemStack stack) {
        return isPatternItem(stack) && !isEncoded(stack);
    }

    public static boolean isPattern(ItemStack stack) {
        return isEncoded(stack);
    }

    public static ItemStack output(ItemStack stack) {
        CraftingPatternData data = stack.get(Registers.CRAFTING_PATTERN.get());
        return data == null ? ItemStack.EMPTY : data.output().copy();
    }

    public static ItemStack encode(Container grid, ItemStack output) {
        NonNullList<ItemStack> inputs = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < GRID_SIZE && i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.isEmpty()) continue;
            ItemStack one = s.copy();
            one.setCount(1);
            inputs.set(i, one);
        }
        ItemStack encoded = new ItemStack(Crafter.CRAFTING_PATTERN.get());
        encoded.set(Registers.CRAFTING_PATTERN.get(), new CraftingPatternData(inputs, output.copy()));
        return encoded;
    }

    @Nullable
    public static CraftingPattern from(ItemStack stack) {
        CraftingPatternData data = stack.get(Registers.CRAFTING_PATTERN.get());
        if (!isPatternItem(stack) || data == null) return null;
        NonNullList<ItemStack> inputs = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < GRID_SIZE && i < data.inputs().size(); i++) {
            inputs.set(i, data.inputs().get(i).copy());
        }
        ItemStack output = data.output().copy();
        if (output.isEmpty()) return null;
        return new CraftingPattern(inputs, output);
    }
}
