package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.util.TextUtils.scaledFormat;

/** EMI display for a Kugelblitz transmutation recipe. */
public class KugelblitzEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final KugelblitzRecipe recipe;
    private final EmiIngredient input;
    private final EmiIngredient outputIngredient;
    private final EmiStack output;

    public KugelblitzEmiRecipe(EmiRecipeCategory category, ResourceLocation id, KugelblitzRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;
        this.input = EmiIngredient.of(Arrays.stream(recipe.input().ingredient().getItems())
                .map(stack -> (EmiIngredient) EmiStack.of(stack.copyWithCount(recipe.input().count())))
                .toList());
        List<ItemStack> members = recipe.output().members();
        this.outputIngredient = EmiIngredient.of(members.stream()
                .map(stack -> (EmiIngredient) EmiStack.of(stack))
                .toList());
        this.output = members.isEmpty() ? EmiStack.EMPTY : EmiStack.of(members.getFirst());
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
        widgets.addText(Component.translatable("gui.nuclearcraft.kugelblitz.energy",
                scaledFormat(recipe.getEnergy())), 4, 27, 0xFF404040, false);
    }
}
