package igentuman.nc.compat.jei;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;

/** Fills JEI slots from native ingredient displays while preserving counts and components. */
final class JeiRecipeSlots {
    private JeiRecipeSlots() {}

    static IRecipeSlotBuilder addItems(IRecipeSlotBuilder slot, SizedIngredient ingredient) {
        return slot.addItemStacks(ingredient.ingredient().display().resolveForStacks(slot.getContextMap()).stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(ingredient.count()))
                .toList());
    }

    static void addFluids(IRecipeSlotBuilder slot, SizedFluidIngredient ingredient) {
        ingredient.ingredient().display().resolve(slot.getContextMap(), FluidStackContentsFactory.INSTANCE)
                .filter(stack -> !stack.isEmpty())
                .forEach(stack -> slot.add(stack.getFluid(), ingredient.amount(), stack.getComponentsPatch()));
    }

    static IRecipeSlotBuilder addFluid(IRecipeSlotBuilder slot, FluidStack stack) {
        return slot.add(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
    }
}
