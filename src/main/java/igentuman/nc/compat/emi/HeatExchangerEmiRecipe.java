package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** EMI recipe display for a heat exchanger recipe with signed heat transfer. */
public class HeatExchangerEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final Identifier id;
    private final HeatExchangerRecipe recipe;
    private final EmiIngredient input;
    private final EmiStack output;

    public HeatExchangerEmiRecipe(EmiRecipeCategory category, Identifier id, HeatExchangerRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;

        this.input = EmiIngredients.fluids(recipe.input());
        this.output = EmiIngredients.fluid(recipe.output().resolve());
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
        Component heat = recipe.heat() >= 0
                ? Component.translatable("gui.nuclearcraft.heat_exchanger.heat_add", recipe.heat())
                : Component.translatable("gui.nuclearcraft.heat_exchanger.heat_remove", -recipe.heat());
        widgets.addText(heat.copy().withStyle(ChatFormatting.DARK_GRAY), 4, 26, 0xFF404040, false);
    }
}
