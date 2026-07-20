package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.bomb.NuclearBlastRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** EMI recipe display for a nuclear-blast block transformation with its conversion chance. */
public class NuclearBlastEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 40;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final NuclearBlastRecipe recipe;
    private final EmiIngredient input;
    private final EmiStack output;

    public NuclearBlastEmiRecipe(EmiRecipeCategory category, ResourceLocation id, NuclearBlastRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;
        this.input = EmiIngredient.of(recipe.input());
        this.output = EmiStack.of(recipe.output().resolve());
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
        widgets.addSlot(output, WIDTH - 24, 4).recipeContext(this);
        widgets.addText(Component.translatable("gui.nuclearcraft.nuclear_blast.chance", (int) Math.round(recipe.chance() * 100) + "%")
                .withStyle(ChatFormatting.DARK_GRAY), 4, 28, 0xFF404040, false);
    }
}
