package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ProcessorCategoryLayout;
import igentuman.nc.compat.ProcessorCategoryLayout.Slot;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.ProgressBar;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

/** EMI recipe display for a universal processor recipe, laid out to match the in-game GUI. */
public class ProcessorEmiRecipe implements EmiRecipe {

    private static final Identifier SLOTS_TEX = rl("textures/gui/slots.png");
    private static final Identifier PROGRESS_TEX = rl("textures/gui/progress_bars.png");

    private final EmiRecipeCategory category;
    private final RecipeHolder<UniversalProcessorRecipe> holder;
    private final UniversalProcessorRecipe recipe;
    private final ProcessorCategoryLayout layout;
    private final int processTime;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();
    private final List<EmiIngredient> outputIngredients = new ArrayList<>();

    public ProcessorEmiRecipe(EmiRecipeCategory category, RecipeHolder<UniversalProcessorRecipe> holder, ModEntry entry) {
        this.category = category;
        this.holder = holder;
        this.recipe = holder.value();
        this.layout = new ProcessorCategoryLayout(entry);
        this.processTime = recipe.getProcessTime();

        List<SizedIngredient> itemInputs = recipe.getItemInputs();
        for (int i = 0; i < layout.itemInputCount; i++) {
            if (i < itemInputs.size()) {
                inputs.add(EmiIngredients.items(itemInputs.get(i)));
            } else {
                inputs.add(EmiIngredient.of(List.of()));
            }
        }

        List<SizedFluidIngredient> fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < layout.fluidInputCount; i++) {
            if (i < fluidInputs.size()) {
                inputs.add(EmiIngredients.fluids(fluidInputs.get(i)));
            } else {
                inputs.add(EmiIngredient.of(List.of()));
            }
        }

        List<ItemOutput> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < layout.itemOutputCount; i++) {
            if (i < itemOutputs.size()) {
                List<ItemStack> members = itemOutputs.get(i).members();
                outputs.add(EmiIngredients.firstItem(members));
                outputIngredients.add(EmiIngredients.itemMembers(members));
            } else {
                outputs.add(EmiStack.EMPTY);
                outputIngredients.add(EmiStack.EMPTY);
            }
        }

        List<FluidOutput> fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < layout.fluidOutputCount; i++) {
            if (i < fluidOutputs.size()) {
                List<FluidStack> members = fluidOutputs.get(i).members();
                outputs.add(EmiIngredients.firstFluid(members));
                outputIngredients.add(EmiIngredients.fluidMembers(members));
            } else {
                outputs.add(EmiStack.EMPTY);
                outputIngredients.add(EmiStack.EMPTY);
            }
        }
    }

    public UniversalProcessorRecipe recipe() {
        return recipe;
    }

    public RecipeHolder<UniversalProcessorRecipe> holder() {
        return holder;
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable Identifier getId() {
        return holder.id().identifier();
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
}
