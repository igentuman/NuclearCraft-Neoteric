package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.PlacementRuleRecipe;
import net.minecraft.network.chat.Component;

public class PlacementRuleEmiRecipe extends BasicEmiRecipe {
    private final PlacementRuleRecipe rule;

    public PlacementRuleEmiRecipe(EmiRecipeCategory category, PlacementRuleRecipe rule) {
        super(category, rule.id(), 176, 122);
        this.rule = rule;
        this.inputs.add(EmiStack.of(rule.part()));
        for (var condition : rule.conditions()) {
            for (var stack : condition.alternatives()) this.catalysts.add(EmiStack.of(stack));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 4, 4).recipeContext(this);
        widgets.addText(Component.translatable("heat_sink.heat.descr", rule.cooling()), 27, 8, 0xFFCC8A20, false);
        for (int i = 0; i < rule.conditions().size(); i++) {
            var condition = rule.conditions().get(i);
            if (!condition.alternatives().isEmpty()) {
                var ingredient = EmiIngredient.of(condition.alternatives().stream().map(EmiStack::of).map(s -> (EmiIngredient) s).toList());
                widgets.addSlot(ingredient, 4, 29 + i * 22).recipeContext(this);
            }
            widgets.addText(condition.description(), 27, 34 + i * 22, 0xFF404040, false);
        }
    }
}
