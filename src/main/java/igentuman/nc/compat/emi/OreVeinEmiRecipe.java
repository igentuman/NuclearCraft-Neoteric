package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.OreVeinRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** EMI display for an in-situ ore vein's weighted output pool. */
public class OreVeinEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final OreVeinRecipe recipe;
    private final List<EmiStack> outputs = new ArrayList<>();
    private final List<EmiIngredient> outputIngredients = new ArrayList<>();

    public OreVeinEmiRecipe(EmiRecipeCategory category, ResourceLocation id, OreVeinRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;
        for (OreVeinRecipe.OreEntry ore : recipe.getOres()) {
            List<EmiStack> members = Arrays.stream(ore.ingredient().ingredient().getItems())
                    .map(stack -> EmiStack.of(stack.copyWithCount(ore.ingredient().count())))
                    .toList();
            outputs.add(members.isEmpty() ? EmiStack.EMPTY : members.getFirst());
            outputIngredients.add(EmiIngredient.of(members.stream()
                    .map(stack -> (EmiIngredient) stack)
                    .toList()));
        }
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
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
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
        for (int i = 0; i < outputIngredients.size(); i++) {
            widgets.addSlot(outputIngredients.get(i), 6 + i * 20, 4).recipeContext(this);
            widgets.addTooltipText(List.of(Component.translatable("gui.nuclearcraft.ore_veins.weight",
                    recipe.getOres().get(i).weight())), 6 + i * 20, 4, 18, 18);
        }
        widgets.addText(Component.translatable("nc.ore_vein." + recipe.getId().getPath()),
                4, 27, 0xFF404040, false);
    }
}
