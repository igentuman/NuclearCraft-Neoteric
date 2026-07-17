package igentuman.nc.compat.jei;

import igentuman.nc.recipes.type.NuclearBlastRecipe;
import igentuman.nc.setup.registration.NCBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.__;

public class NuclearBlastCategoryWrapper<T extends NuclearBlastRecipe> implements IRecipeCategory<T> {
    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;

    public NuclearBlastCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.background = guiHelper.createBlankDrawable(120, 50);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(NCBlocks.PU_239_BOMB_ITEM.get()));
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return __("nc_jei_cat.nuclear_blast");
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
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        if (recipe.getInputItems().length > 0 && recipe.getInputItems()[0] != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 15)
                    .addItemStacks(recipe.getInputItems()[0].getRepresentations());
        }
        if (recipe.getOutputItems().length > 0 && recipe.getOutputItems()[0] != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 15)
                    .addItemStacks(recipe.getOutputItems()[0].getRepresentations());
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "==>", 48, 19, 0x404040, false);
        String chanceText = String.format("%.0f%%", recipe.getChance() * 100);
        int width = font.width(chanceText);
        guiGraphics.drawString(font, chanceText, 60 - width / 2, 5, 0x404040, false);
    }
}
