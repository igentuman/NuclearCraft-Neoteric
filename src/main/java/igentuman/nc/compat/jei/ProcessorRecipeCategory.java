package igentuman.nc.compat.jei;

import igentuman.nc.Main;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.util.caps.FluidCapDefinition;
import igentuman.nc.util.caps.ItemCapDefinition;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class ProcessorRecipeCategory implements IRecipeCategory<UniversalProcessorRecipe> {

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 20;
    private static final int START_X = 1;
    private static final int START_Y = 1;
    private static final int OUTPUT_GAP = 30;

    public final RecipeType<UniversalProcessorRecipe> recipeType;
    private final ModEntry modEntry;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    private final int itemInputCount;
    private final int fluidInputCount;
    private final int itemOutputCount;
    private final int fluidOutputCount;

    public ProcessorRecipeCategory(IGuiHelper guiHelper, ModEntry entry, RecipeType<UniversalProcessorRecipe> recipeType) {
        this.recipeType = recipeType;
        this.modEntry = entry;
        this.title = __("block." + Main.MODID + "." + entry.name());

        ItemCapDefinition itemCap = entry.itemCap();
        FluidCapDefinition fluidCap = entry.fluidCap();

        this.itemInputCount = itemCap != null ? itemCap.inputSlots : 0;
        this.fluidInputCount = fluidCap != null ? fluidCap.inputTanks.size() : 0;
        this.itemOutputCount = itemCap != null ? itemCap.outputSlots : 0;
        this.fluidOutputCount = fluidCap != null ? fluidCap.outputTanks.size() : 0;

        int totalInputs = itemInputCount + fluidInputCount;
        int totalOutputs = itemOutputCount + fluidOutputCount;
        int totalSlots = totalInputs + totalOutputs;

        int width = START_X + totalSlots * SLOT_SPACING + (totalOutputs > 0 ? OUTPUT_GAP : 0) + 4;
        int height = START_Y + SLOT_SPACING + 4;

        this.background = guiHelper.createBlankDrawable(Math.max(width, 40), Math.max(height, 24));

        if (entry.hasItem()) {
            this.icon = guiHelper.createDrawableItemStack(new ItemStack(entry.item().get()));
        } else {
            this.icon = null;
        }
    }

    @Override
    public RecipeType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UniversalProcessorRecipe recipe, IFocusGroup focuses) {
        int x = START_X;
        int y = START_Y;

        // Item input slots
        List<SizedIngredient> itemInputs = recipe.getItemInputs();
        for (int i = 0; i < itemInputCount; i++) {
            if (i < itemInputs.size()) {
                SizedIngredient si = itemInputs.get(i);
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addItemStacks(Arrays.stream(si.ingredient().getItems())
                                .map(stack -> {
                                    ItemStack copy = stack.copy();
                                    copy.setCount(si.count());
                                    return copy;
                                })
                                .toList());
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y);
            }
            x += SLOT_SPACING;
        }

        // Fluid input slots
        List<SizedFluidIngredient> fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < fluidInputCount; i++) {
            if (i < fluidInputs.size()) {
                SizedFluidIngredient sfi = fluidInputs.get(i);
                var slot = builder.addSlot(RecipeIngredientRole.INPUT, x, y);
                for (FluidStack fs : sfi.getFluids()) {
                    FluidStack copy = fs.copy();
                    copy.setAmount(sfi.amount());
                    slot.addFluidStack(copy.getFluid(), copy.getAmount());
                }
                slot.setFluidRenderer(sfi.amount(), false, SLOT_SIZE - 2, SLOT_SIZE - 2);
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y);
            }
            x += SLOT_SPACING;
        }

        // Gap before outputs
        x += OUTPUT_GAP;

        // Item output slots (tag outputs show all members, primary first)
        List<ItemOutput> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputCount; i++) {
            if (i < itemOutputs.size()) {
                List<ItemStack> members = itemOutputs.get(i).members();
                var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
                if (!members.isEmpty()) slot.addItemStacks(members);
            } else {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
            }
            x += SLOT_SPACING;
        }

        // Fluid output slots (tag outputs show all members, primary first)
        List<FluidOutput> fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < fluidOutputCount; i++) {
            if (i < fluidOutputs.size()) {
                List<FluidStack> members = fluidOutputs.get(i).members();
                var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
                int amount = members.isEmpty() ? 0 : members.getFirst().getAmount();
                for (FluidStack fs : members) {
                    slot.addFluidStack(fs.getFluid(), fs.getAmount());
                }
                if (amount > 0) {
                    slot.setFluidRenderer(amount, false, SLOT_SIZE - 2, SLOT_SIZE - 2);
                }
            } else {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
            }
            x += SLOT_SPACING;
        }
    }
}
