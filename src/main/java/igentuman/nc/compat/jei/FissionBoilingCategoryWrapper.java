package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.compat.jei.util.TickTimer;
import igentuman.nc.datagen.recipes.recipes.FissionBoilingRecipes;
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;

public class FissionBoilingCategoryWrapper<T extends FissionControllerBE.FissionBoilingRecipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/processor_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    IDrawable arrow;
    private  IDrawable[] slots;

    IGuiHelper guiHelper;

    public FissionBoilingCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 105, 32);
        if(CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CATALYSTS.get(getRecipeType().getUid().getPath()).get(0));
        } else{
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("nc_jei_cat."+getRecipeType().getUid().getPath());
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
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        arrow.draw(stack, 34, 6);

        slots[0].draw(stack, 11, 5);
        slots[1].draw(stack, 74, 5);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 6 && mouseY < 20) {
            lines.add(Component.translatable("boiling.recipe.heat_required", (int)recipe.conversionRate()).withStyle(ChatFormatting.GOLD));
        }
        return lines;
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
