package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.fusion.FusionCoolantRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** EMI display for a fusion coolant recipe. */
public class FusionCoolantEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final Identifier id;
    private final FusionCoolantRecipe recipe;
    private final EmiIngredient input;
    private final EmiIngredient outputIngredient;
    private final EmiStack output;

    public FusionCoolantEmiRecipe(EmiRecipeCategory category, Identifier id, FusionCoolantRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;
        this.input = EmiIngredients.fluids(recipe.input());
        List<FluidStack> members = recipe.output().members();
        this.outputIngredient = EmiIngredients.fluidMembers(members);
        this.output = EmiIngredients.firstFluid(members);
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
        widgets.addSlot(outputIngredient, WIDTH - 24, 4).recipeContext(this);
        widgets.addText(Component.translatable("gui.nuclearcraft.fusion_coolant.cooling", recipe.coolingRate()),
                4, 27, 0xFF404040, false);
    }
}
