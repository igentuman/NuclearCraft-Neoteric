package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.jei.FuelInfoRecipe;
import net.minecraft.network.chat.Component;

import static igentuman.nc.util.TextUtils.__;

public class FuelInfoEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 200;
    private static final int ROW_HEIGHT = 34;
    private static final int HEADER = 12;

    private final FuelInfoRecipe recipe;

    public FuelInfoEmiRecipe(EmiRecipeCategory category, FuelInfoRecipe recipe) {
        super(category, recipe.getId(), WIDTH, HEADER + recipe.getVariants().size() * ROW_HEIGHT + 16);
        this.recipe = recipe;

        for (FuelInfoRecipe.Variant v : recipe.getVariants()) {
            this.outputs.add(EmiStack.of(v.item));
        }
        if (!recipe.getBaseItem().isEmpty()) {
            this.catalysts.add(EmiStack.of(recipe.getBaseItem()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(__("jei.nuclearcraft.fuel_info.title", recipe.getName().toUpperCase()),
                2, 1, 0xFF404040, false);

        int y = HEADER;
        int textX = 24;
        int slotIdx = 0;
        for (FuelInfoRecipe.Variant v : recipe.getVariants()) {
            widgets.addSlot(outputs.get(slotIdx++), 2, y).recipeContext(this);

            widgets.addText(__(v.labelKey), textX, y + 3, 0xFF202020, false);

            String stats;
            if (v.triso) {
                stats = __("jei.nuclearcraft.fuel_info.row_triso",
                        v.def.criticality,
                        formatHeat(v.def.heat),
                        v.def.depletion
                ).getString();
            } else {
                stats = __("jei.nuclearcraft.fuel_info.row",
                        v.def.forgeEnergy,
                        formatHeat(v.def.heat),
                        v.def.depletion
                ).getString();
            }
            widgets.addText(Component.literal(stats), textX - 20, y + 19, 0xFF404040, false);

            y += ROW_HEIGHT;
        }
    }

    private static String formatHeat(double heat) {
        if (heat >= 1000) return String.format("%.1fk", heat / 1000);
        return String.format("%.0f", heat);
    }
}
