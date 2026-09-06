package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.turbine.TurbineRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/** EMI display for a multiblock turbine recipe. */
public class TurbineEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final TurbineRecipe recipe;
    private final EmiIngredient input;
    private final EmiIngredient outputIngredient;
    private final EmiStack output;

    public TurbineEmiRecipe(EmiRecipeCategory category, ResourceLocation id, TurbineRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;
        this.input = EmiIngredient.of(Arrays.stream(recipe.input().getFluids())
                .map(stack -> (EmiIngredient) EmiStack.of(stack.getFluid(), recipe.input().amount()))
                .toList());
        List<FluidStack> members = recipe.output().members();
        this.outputIngredient = EmiIngredient.of(members.stream()
                .map(stack -> (EmiIngredient) EmiStack.of(stack.getFluid(), stack.getAmount()))
                .toList());
        this.output = members.isEmpty()
                ? EmiStack.EMPTY
                : EmiStack.of(members.getFirst().getFluid(), members.getFirst().getAmount());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(input, 6, 4);
        widgets.addText(Component.literal("→"), WIDTH / 2 - 3, 8, 0xFF404040, false);
        widgets.addSlot(outputIngredient, WIDTH - 24, 4).recipeContext(this);
        widgets.addText(Component.translatable("gui.nuclearcraft.turbine.power",
                Math.round(recipe.powerModifier() * 100) + "%"), 4, 27, 0xFF404040, false);
    }
}
