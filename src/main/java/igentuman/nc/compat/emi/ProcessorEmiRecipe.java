package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.util.caps.FluidCapDefinition;
import igentuman.nc.util.caps.ItemCapDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessorEmiRecipe implements EmiRecipe {

    private static final int SLOT_SPACING = 20;
    private static final int START_X = 1;
    private static final int START_Y = 1;
    private static final int OUTPUT_GAP = 30;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final ModEntry modEntry;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();
    private final List<EmiIngredient> outputIngredients = new ArrayList<>();
    private final int width;
    private final int height;

    private final int itemInputCount;
    private final int fluidInputCount;
    private final int itemOutputCount;
    private final int fluidOutputCount;

    public ProcessorEmiRecipe(EmiRecipeCategory category, ResourceLocation id, UniversalProcessorRecipe recipe, ModEntry entry) {
        this.category = category;
        this.id = id;
        this.modEntry = entry;

        ItemCapDefinition itemCap = entry.itemCap();
        FluidCapDefinition fluidCap = entry.fluidCap();

        itemInputCount = itemCap != null ? itemCap.inputSlots : 0;
        fluidInputCount = fluidCap != null ? fluidCap.inputTanks.size() : 0;
        itemOutputCount = itemCap != null ? itemCap.outputSlots : 0;
        fluidOutputCount = fluidCap != null ? fluidCap.outputTanks.size() : 0;

        List<SizedIngredient> itemInputs = recipe.getItemInputs();
        for (int i = 0; i < itemInputCount; i++) {
            if (i < itemInputs.size()) {
                SizedIngredient si = itemInputs.get(i);
                inputs.add(EmiIngredient.of(
                    Arrays.stream(si.ingredient().getItems())
                        .map(stack -> {
                            ItemStack copy = stack.copy();
                            copy.setCount(si.count());
                            return (EmiIngredient) EmiStack.of(copy);
                        })
                        .toList()
                ));
            } else {
                inputs.add(EmiIngredient.of(List.of()));
            }
        }

        List<SizedFluidIngredient> fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < fluidInputCount; i++) {
            if (i < fluidInputs.size()) {
                SizedFluidIngredient sfi = fluidInputs.get(i);
                inputs.add(EmiIngredient.of(
                    Arrays.stream(sfi.getFluids())
                        .map(fs -> (EmiIngredient) EmiStack.of(fs.getFluid(), sfi.amount()))
                        .toList()
                ));
            } else {
                inputs.add(EmiIngredient.of(List.of()));
            }
        }

        List<ItemOutput> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputCount; i++) {
            if (i < itemOutputs.size()) {
                List<ItemStack> members = itemOutputs.get(i).members();
                outputs.add(members.isEmpty() ? EmiStack.EMPTY : EmiStack.of(members.getFirst()));
                outputIngredients.add(ingredientOf(members.stream()
                        .map(s -> (EmiStack) EmiStack.of(s))
                        .toList()));
            } else {
                outputs.add(EmiStack.EMPTY);
                outputIngredients.add(EmiStack.EMPTY);
            }
        }

        List<FluidOutput> fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < fluidOutputCount; i++) {
            if (i < fluidOutputs.size()) {
                List<FluidStack> members = fluidOutputs.get(i).members();
                outputs.add(members.isEmpty()
                        ? EmiStack.EMPTY
                        : EmiStack.of(members.getFirst().getFluid(), members.getFirst().getAmount()));
                outputIngredients.add(ingredientOf(members.stream()
                        .map(fs -> (EmiStack) EmiStack.of(fs.getFluid(), fs.getAmount()))
                        .toList()));
            } else {
                outputs.add(EmiStack.EMPTY);
                outputIngredients.add(EmiStack.EMPTY);
            }
        }

        int totalInputs = itemInputCount + fluidInputCount;
        int totalOutputs = itemOutputCount + fluidOutputCount;
        width = Math.max(START_X + totalInputs * SLOT_SPACING + (totalOutputs > 0 ? OUTPUT_GAP : 0) + totalOutputs * SLOT_SPACING + 4, 40);
        height = Math.max(START_Y + SLOT_SPACING + 4, 24);
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
        return width;
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int x = START_X;
        int y = START_Y;
        int idx = 0;

        for (int i = 0; i < itemInputCount; i++, idx++) {
            widgets.addSlot(inputs.get(idx), x, y);
            x += SLOT_SPACING;
        }

        for (int i = 0; i < fluidInputCount; i++, idx++) {
            widgets.addSlot(inputs.get(idx), x, y).drawBack(true);
            x += SLOT_SPACING;
        }

        x += OUTPUT_GAP;

        for (int i = 0; i < itemOutputCount; i++) {
            widgets.addSlot(outputIngredients.get(i), x, y).recipeContext(this);
            x += SLOT_SPACING;
        }

        for (int i = 0; i < fluidOutputCount; i++) {
            widgets.addSlot(outputIngredients.get(itemOutputCount + i), x, y).drawBack(true).recipeContext(this);
            x += SLOT_SPACING;
        }
    }

    private static EmiIngredient ingredientOf(List<EmiStack> stacks) {
        if (stacks.isEmpty()) return EmiStack.EMPTY;
        if (stacks.size() == 1) return stacks.get(0);
        return EmiIngredient.of(stacks.stream().map(s -> (EmiIngredient) s).toList());
    }
}
