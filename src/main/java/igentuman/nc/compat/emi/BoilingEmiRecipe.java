package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.fission.BoilingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoilingEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final BoilingRecipe recipe;
    private final EmiIngredient input;
    private final EmiStack output;

    public BoilingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, BoilingRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;

        SizedFluidIngredient in = recipe.input();
        List<EmiStack> inFluids = new ArrayList<>();
        for (FluidStack fs : in.getFluids()) {
            inFluids.add(EmiStack.of(fs.getFluid(), in.amount()));
        }
        this.input = EmiIngredient.of(inFluids.stream().map(s -> (EmiIngredient) s).toList());

        List<FluidStack> outMembers = recipe.output().members();
        this.output = outMembers.isEmpty()
                ? EmiStack.EMPTY
                : EmiStack.of(outMembers.getFirst().getFluid(), outMembers.getFirst().getAmount());
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
        widgets.addText(Component.translatable("gui.nuclearcraft.fission_boiling.heat", recipe.heatRequired())
                .withStyle(ChatFormatting.DARK_GRAY), 4, 26, 0xFF404040, false);
    }
}
