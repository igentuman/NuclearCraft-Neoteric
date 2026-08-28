package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.jei.IsotopeInfoRecipe;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.convertToName;

public class IsotopeInfoEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 160;
    private static final int HEADER = 12;
    private static final int SLOT_SIZE = 22;
    private static final int LABEL_GAP = 14;

    private final IsotopeInfoRecipe recipe;

    public IsotopeInfoEmiRecipe(EmiRecipeCategory category, IsotopeInfoRecipe recipe) {
        super(category, recipe.getId(), WIDTH, HEADER + SLOT_SIZE + LABEL_GAP + 4);
        this.recipe = recipe;

        for (IsotopeInfoRecipe.Variant v : recipe.getVariants()) {
            this.outputs.add(EmiStack.of(v.item));
        }
        if (!recipe.getBaseItem().isEmpty()) {
            this.catalysts.add(EmiStack.of(recipe.getBaseItem()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(__("jei.nuclearcraft.isotope_info.title", convertToName(recipe.getName())),
                2, 1, 0xFF404040, false);

        int x = 4;
        int idx = 0;
        for (IsotopeInfoRecipe.Variant v : recipe.getVariants()) {
            widgets.addSlot(outputs.get(idx++), x, HEADER).recipeContext(this);
            widgets.addText(__(v.labelKey), x, HEADER + SLOT_SIZE + 2, 0xFF202020, false);
            x += SLOT_SIZE + 12;
        }
    }
}
