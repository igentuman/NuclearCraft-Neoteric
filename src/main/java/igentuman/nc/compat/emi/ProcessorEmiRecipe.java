package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ProcessorCategoryLayout;
import igentuman.nc.compat.ProcessorCategoryLayout.Slot;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.ProgressBar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class ProcessorEmiRecipe implements EmiRecipe {

    private static final ResourceLocation SLOTS_TEX = rl("textures/gui/slots.png");
    private static final ResourceLocation PROGRESS_TEX = rl("textures/gui/progress_bars.png");

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final ProcessorCategoryLayout layout;
    private final int processTime;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();
    private final List<EmiIngredient> outputIngredients = new ArrayList<>();

    public ProcessorEmiRecipe(EmiRecipeCategory category, ResourceLocation id, UniversalProcessorRecipe recipe, ModEntry entry) {
        this.category = category;
        this.id = id;
        this.layout = new ProcessorCategoryLayout(entry);
        this.processTime = recipe.getProcessTime();

        List<SizedIngredient> itemInputs = recipe.getItemInputs();
        for (int i = 0; i < layout.itemInputCount; i++) {
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
        for (int i = 0; i < layout.fluidInputCount; i++) {
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
        for (int i = 0; i < layout.itemOutputCount; i++) {
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
        for (int i = 0; i < layout.fluidOutputCount; i++) {
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
        return layout.width;
    }

    @Override
    public int getDisplayHeight() {
        return layout.height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int inIdx = 0;
        int outIdx = 0;
        for (Slot s : layout.slots) {
            widgets.addTexture(SLOTS_TEX, s.spriteX, s.spriteY, ProcessorCategoryLayout.SLOT, ProcessorCategoryLayout.SLOT, s.type.u, s.type.v);
            if (s.type.output) {
                widgets.addSlot(outputIngredients.get(outIdx++), s.spriteX, s.spriteY).drawBack(false).recipeContext(this);
            } else {
                widgets.addSlot(inputs.get(inIdx++), s.spriteX, s.spriteY).drawBack(false);
            }
        }

        if (layout.hasArrow) {
            int[] bar = ProgressBar.bars.get(layout.progressBar);
            widgets.addTexture(PROGRESS_TEX, layout.arrowX, layout.arrowY, ProcessorCategoryLayout.BAR_W, layout.barH, bar[0], bar[1]);
            widgets.addAnimatedTexture(PROGRESS_TEX, layout.arrowX, layout.arrowY, ProcessorCategoryLayout.BAR_W, layout.barH,
                    bar[0], bar[1] - layout.barH - 1, Math.max(1, processTime) * 50, true, false, false);
        }
    }

    private static EmiIngredient ingredientOf(List<EmiStack> stacks) {
        if (stacks.isEmpty()) return EmiStack.EMPTY;
        if (stacks.size() == 1) return stacks.get(0);
        return EmiIngredient.of(stacks.stream().map(s -> (EmiIngredient) s).toList());
    }
}
