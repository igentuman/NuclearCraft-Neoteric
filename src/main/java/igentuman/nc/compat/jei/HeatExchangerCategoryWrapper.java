package igentuman.nc.compat.jei;

import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import igentuman.nc.compat.jei.util.TickTimer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static net.minecraft.world.item.Items.BARRIER;

@SuppressWarnings("removal")
public class HeatExchangerCategoryWrapper<T extends HeatExchangerControllerBE.Recipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/processor_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    IDrawable arrow;
    private IDrawable[] slots;

    IGuiHelper guiHelper;

    public HeatExchangerCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 105, 32);
        if (CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(HX_BLOCKS.get("heat_exchanger_controller").get()));
        } else {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BARRIER));
        }
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return __("nc_jei_cat." + getRecipeType().getUid().getPath());
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
                     double mouseY) {
        arrow.draw(graphics, 34, 6);

        slots[0].draw(graphics, 11, 5);
        slots[1].draw(graphics, 74, 5);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> list = new ArrayList<>();
        double h = recipe.getHeat();
        if (h > 0) {
            list.add(__("heat_exchanger_controller.recipe.heat_add", (int) h).withStyle(net.minecraft.ChatFormatting.RED));
        } else if (h < 0) {
            list.add(__("heat_exchanger_controller.recipe.heat_remove", (int) Math.abs(h)).withStyle(net.minecraft.ChatFormatting.AQUA));
        }
        return list;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, T recipe, @NotNull IFocusGroup focuses) {
        slots = new IDrawable[2];
        arrow = guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 0, 36, 15)
                .buildAnimated(new TickTimer(100, 36, true), IDrawableAnimated.StartDirection.LEFT);

        builder.addSlot(RecipeIngredientRole.INPUT, 12, 6)
                .addIngredients(ForgeTypes.FLUID_STACK, recipe.getInputFluids(0))
                .setFluidRenderer(recipe.getInputFluids()[0].getAmount(), false, 16, 16);
        slots[0] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 6)
                .addIngredients(ForgeTypes.FLUID_STACK, recipe.getOutputFluids(0))
                .setFluidRenderer(recipe.getOutputFluids().get(0).getAmount(), false, 16, 16);
        slots[1] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
    }
}
