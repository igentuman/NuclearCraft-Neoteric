package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ParticleSourceRecipe;
import net.minecraft.network.chat.Component;

public class ParticleSourceEmiRecipe extends BasicEmiRecipe {

    public ParticleSourceEmiRecipe(EmiRecipeCategory category, ParticleSourceRecipe recipe) {
        super(category, recipe.id(), 90, 40);
        inputs.add(recipe.item().isEmpty()
                ? EmiIngredients.fluid(recipe.fluid())
                : EmiIngredients.item(recipe.item()));
        outputs.add(ParticleEmiStack.of(recipe.output()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.getFirst(), 10, 10);
        widgets.addText(Component.literal("→"), 42, 14, 0xFF404040, false);
        widgets.addSlot(outputs.getFirst(), 62, 10).recipeContext(this);
    }
}
