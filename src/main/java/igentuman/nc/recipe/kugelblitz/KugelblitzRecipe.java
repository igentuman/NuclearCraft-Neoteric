package igentuman.nc.recipe.kugelblitz;

import igentuman.nc.recipe.MachineRecipe;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.recipe.ProcessorRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record KugelblitzRecipe(
        SizedIngredient input,
        ItemOutput output,
        double timeModifier,
        double powerModifier
) implements MachineRecipe<ProcessorRecipeInput> {

    @Override
    public boolean matches(ProcessorRecipeInput in, Level level) {
        if (level.isClientSide()) return false;
        if (in.size() < 1) return false;
        return input.test(in.getItem(0));
    }

    public int getBaseTime() {
        return (int) (timeModifier * 50);
    }

    public int getEnergy() {
        return (int) (powerModifier * 1000);
    }

    public ItemStack getInputStack(Level level) {
        return input.ingredient().display().resolveForStacks(SlotDisplayContext.fromLevel(level)).stream()
                .filter(stack -> !stack.isEmpty()).findFirst()
                .map(stack -> stack.copyWithCount(input.count())).orElse(ItemStack.EMPTY);
    }

    public ItemStack getResultStack() {
        return output.resolve();
    }

    public boolean isComplete() {
        return !input.ingredient().isEmpty() && output.isComplete();
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput in) {
        return output.resolve();
    }

    @Override
    public RecipeSerializer<KugelblitzRecipe> getSerializer() {
        return KugelblitzRecipes.KUGELBLITZ_SERIALIZER.get();
    }

    @Override
    public RecipeType<KugelblitzRecipe> getType() {
        return KugelblitzRecipes.KUGELBLITZ_TYPE.get();
    }
}
