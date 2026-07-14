package igentuman.nc.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/** RecipeInput providing both item and fluid access for universal processor recipe matching. */
public class ProcessorRecipeInput implements RecipeInput {

    private final List<ItemStack> items;
    private final List<FluidStack> fluids;

    public ProcessorRecipeInput(List<ItemStack> items, List<FluidStack> fluids) {
        this.items = items;
        this.fluids = fluids;
    }

    public ProcessorRecipeInput(List<ItemStack> items) {
        this(items, List.of());
    }

    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    public FluidStack getFluid(int index) {
        if (index < 0 || index >= fluids.size()) {
            return FluidStack.EMPTY;
        }
        return fluids.get(index);
    }

    public int fluidSize() {
        return fluids.size();
    }
}
