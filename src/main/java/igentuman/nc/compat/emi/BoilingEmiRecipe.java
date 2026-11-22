package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.fission.BoilingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** EMI recipe display for a fission reactor coolant boiling recipe. */
public class BoilingEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final Identifier id;
    private final BoilingRecipe recipe;
    private final EmiIngredient input;
    private final EmiStack output;

    public BoilingEmiRecipe(EmiRecipeCategory category, Identifier id, BoilingRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;

        this.input = EmiIngredients.fluids(recipe.input());
        this.output = EmiIngredients.firstFluid(recipe.output().members());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable Identifier getId() {
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
        widgets.addSlot(output, WIDTH - 24, 4).recipeContext(this);
        widgets.addText(Component.translatable("gui.nuclearcraft.fission_boiling.heat", recipe.heatRequired())
                .withStyle(ChatFormatting.DARK_GRAY), 4, 26, 0xFF404040, false);
    }
}
