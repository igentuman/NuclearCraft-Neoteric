package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ProcessorCategoryLayout;
import igentuman.nc.compat.ProcessorCategoryLayout.Slot;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.ProgressBar;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class ProcessorRecipeCategory implements IRecipeCategory<UniversalProcessorRecipe> {

    private static final ResourceLocation SLOTS_TEX = rl("textures/gui/slots.png");
    private static final ResourceLocation PROGRESS_TEX = rl("textures/gui/progress_bars.png");

    public final RecipeType<UniversalProcessorRecipe> recipeType;
    private final IGuiHelper guiHelper;
    private final ProcessorCategoryLayout layout;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    private final IDrawableStatic arrowBackground;
    private final Map<Integer, IDrawableAnimated> arrowCache = new HashMap<>();

    public ProcessorRecipeCategory(IGuiHelper guiHelper, ModEntry entry, RecipeType<UniversalProcessorRecipe> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.title = __("block." + NuclearCraft.MODID + "." + entry.name());
        this.layout = new ProcessorCategoryLayout(entry);
        this.background = guiHelper.createBlankDrawable(layout.width, layout.height);

        if (layout.hasArrow) {
            int[] bar = ProgressBar.bars.get(layout.progressBar);
            this.arrowBackground = guiHelper.createDrawable(PROGRESS_TEX, bar[0], bar[1], ProcessorCategoryLayout.BAR_W, layout.barH);
        } else {
            this.arrowBackground = null;
        }

        this.icon = entry.hasItem() ? guiHelper.createDrawableItemStack(new ItemStack(entry.item().get())) : null;
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
        List<SizedIngredient> itemInputs = recipe.getItemInputs();
        List<SizedFluidIngredient> fluidInputs = recipe.getFluidInputs();
        List<ItemOutput> itemOutputs = recipe.getItemOutputs();
        List<FluidOutput> fluidOutputs = recipe.getFluidOutputs();
        int itemIn = 0, fluidIn = 0, itemOut = 0, fluidOut = 0;

        for (Slot s : layout.slots) {
            switch (s.type) {
                case ITEM_IN -> {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, s.ingX, s.ingY);
                    if (itemIn < itemInputs.size()) {
                        SizedIngredient si = itemInputs.get(itemIn);
                        slot.addItemStacks(Arrays.stream(si.ingredient().getItems())
                                .map(stack -> {
                                    ItemStack copy = stack.copy();
                                    copy.setCount(si.count());
                                    return copy;
                                })
                                .toList());
                    }
                    itemIn++;
                }
                case FLUID_IN -> {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, s.ingX, s.ingY);
                    if (fluidIn < fluidInputs.size()) {
                        SizedFluidIngredient sfi = fluidInputs.get(fluidIn);
                        for (FluidStack fs : sfi.getFluids()) {
                            FluidStack copy = fs.copy();
                            copy.setAmount(sfi.amount());
                            slot.addFluidStack(copy.getFluid(), copy.getAmount());
                        }
                        slot.setFluidRenderer(sfi.amount(), false, ProcessorCategoryLayout.SLOT - 2, ProcessorCategoryLayout.SLOT - 2);
                    }
                    fluidIn++;
                }
                case ITEM_OUT -> {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, s.ingX, s.ingY);
                    if (itemOut < itemOutputs.size()) {
                        List<ItemStack> members = itemOutputs.get(itemOut).members();
                        if (!members.isEmpty()) slot.addItemStacks(members);
                    }
                    itemOut++;
                }
                case FLUID_OUT -> {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, s.ingX, s.ingY);
                    if (fluidOut < fluidOutputs.size()) {
                        List<FluidStack> members = fluidOutputs.get(fluidOut).members();
                        int amount = members.isEmpty() ? 0 : members.getFirst().getAmount();
                        for (FluidStack fs : members) {
                            slot.addFluidStack(fs.getFluid(), fs.getAmount());
                        }
                        if (amount > 0) {
                            slot.setFluidRenderer(amount, false, ProcessorCategoryLayout.SLOT - 2, ProcessorCategoryLayout.SLOT - 2);
                        }
                    }
                    fluidOut++;
                }
            }
        }
    }

    @Override
    public void draw(UniversalProcessorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        for (Slot s : layout.slots) {
            graphics.blit(SLOTS_TEX, s.spriteX, s.spriteY, s.type.u, s.type.v, ProcessorCategoryLayout.SLOT, ProcessorCategoryLayout.SLOT);
        }
        if (layout.hasArrow) {
            arrowBackground.draw(graphics, layout.arrowX, layout.arrowY);
            arrow(recipe.getProcessTime()).draw(graphics, layout.arrowX, layout.arrowY);
        }
    }

    private IDrawableAnimated arrow(int processTime) {
        return arrowCache.computeIfAbsent(Math.max(1, processTime), ticks -> {
            int[] bar = ProgressBar.bars.get(layout.progressBar);
            IDrawableStatic fill = guiHelper.createDrawable(PROGRESS_TEX, bar[0], bar[1] - layout.barH - 1, ProcessorCategoryLayout.BAR_W, layout.barH);
            return guiHelper.createAnimatedDrawable(fill, ticks, IDrawableAnimated.StartDirection.LEFT, false);
        });
    }
}
