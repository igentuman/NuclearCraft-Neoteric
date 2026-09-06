package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.fusion.FusionRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static igentuman.nc.util.TextUtils.scaledFormat;

/** EMI display for a fusion fuel recipe. */
public class FusionEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 142;
    private static final int HEIGHT = 62;
    private static final int ENERGY_TEXT_Y = 49;
    private static final int[][] INPUT_POSITIONS = {{6, 4}, {28, 4}};
    private static final int[][] OUTPUT_POSITIONS = {{100, 4}, {122, 4}, {100, 26}, {122, 26}};

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final FusionRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs = new ArrayList<>();
    private final List<EmiIngredient> outputIngredients = new ArrayList<>();

    public FusionEmiRecipe(EmiRecipeCategory category, ResourceLocation id, FusionRecipe recipe) {
        this.category = category;
        this.id = id;
        this.recipe = recipe;
        this.inputs = List.of(fluidIngredient(recipe.inputA()), fluidIngredient(recipe.inputB()));
        for (FluidOutput output : recipe.outputs()) {
            List<FluidStack> members = output.members();
            outputs.add(members.isEmpty()
                    ? EmiStack.EMPTY
                    : EmiStack.of(members.getFirst().getFluid(), members.getFirst().getAmount()));
            outputIngredients.add(EmiIngredient.of(members.stream()
                    .map(stack -> (EmiIngredient) EmiStack.of(stack.getFluid(), stack.getAmount()))
                    .toList()));
        }
    }

    private static EmiIngredient fluidIngredient(SizedFluidIngredient ingredient) {
        return EmiIngredient.of(Arrays.stream(ingredient.getFluids())
                .map(stack -> (EmiIngredient) EmiStack.of(stack.getFluid(), ingredient.amount()))
                .toList());
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
        return inputs;
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
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), INPUT_POSITIONS[i][0], INPUT_POSITIONS[i][1]);
        }
        for (int i = 0; i < Math.min(outputIngredients.size(), OUTPUT_POSITIONS.length); i++) {
            widgets.addSlot(outputIngredients.get(i), OUTPUT_POSITIONS[i][0], OUTPUT_POSITIONS[i][1])
                    .recipeContext(this);
        }
        widgets.addText(Component.literal("→"), 9, 9, 0xFF404040, false);
        widgets.addText(Component.translatable("gui.nuclearcraft.fusion.energy", scaledFormat(recipe.energy())),
                WIDTH / 2, ENERGY_TEXT_Y, 0xFF404040, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);
        widgets.addTooltipText(List.of(
                Component.translatable("gui.nuclearcraft.fusion.energy", scaledFormat(recipe.energy())),
                Component.translatable("gui.nuclearcraft.fusion.temperature",
                        scaledFormat(recipe.optimalTemperature())),
                Component.translatable("gui.nuclearcraft.recipe.time", recipe.processTime())
        ), 49, 4, 48, 42);
    }
}
