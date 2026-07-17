package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.jei.IsotopeInfoRecipe;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.capitalize;

public class IsotopeInfoEmiCategory extends BasicEmiRecipe {

    private static final int WIDTH = 160;
    private static final int HEADER = 12;
    private static final int SLOT_SIZE = 22;
    private static final int LABEL_GAP = 14;

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("isotope_info"),
            EmiStack.of(firstIsotopeStack())
    );

    private final IsotopeInfoRecipe recipe;

    public IsotopeInfoEmiCategory(IsotopeInfoRecipe recipe) {
        super(CATEGORY, rl("/" + recipe.getId().getPath()), WIDTH, HEADER + SLOT_SIZE + LABEL_GAP + 4);
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
        widgets.addText(__("jei.nuclearcraft.isotope_info.title", capitalize(recipe.getName())),
                2, 1, 0xFF404040, false);

        int x = 4;
        int idx = 0;
        for (IsotopeInfoRecipe.Variant v : recipe.getVariants()) {
            widgets.addSlot(outputs.get(idx++), x, HEADER).recipeContext(this);
            widgets.addText(__(v.labelKey), x, HEADER + SLOT_SIZE + 2, 0xFF202020, false);
            x += SLOT_SIZE + 12;
        }
    }

    private static ItemStack firstIsotopeStack() {
        return FissionFuel.NC_ISOTOPES.values().stream().findFirst()
                .map(ro -> new ItemStack(ro.get()))
                .orElse(ItemStack.EMPTY);
    }

    public static Component getTitle() {
        return Component.translatable("emi.category." + MODID + ".isotope_info");
    }
}
