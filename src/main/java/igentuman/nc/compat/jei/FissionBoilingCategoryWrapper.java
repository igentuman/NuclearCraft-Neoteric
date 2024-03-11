package igentuman.nc.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.compat.jei.util.TickTimer;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import org.antlr.v4.runtime.misc.NotNull;


import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static mezz.jei.api.constants.VanillaTypes.FLUID;

@SuppressWarnings("all")
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
            this.icon = guiHelper.createDrawableIngredient(CATALYSTS.get(getRecipeType().getUid().getPath()).get(0));
        } else{
            this.icon = guiHelper.createDrawableIngredient(ItemStack.EMPTY);
        }

    }

    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull String getTitle() {
        return new TranslationTextComponent("nc_jei_cat."+getRecipeType().getUid().getPath()).getString();
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
    public void setIngredients(T t, IIngredients iIngredients) {
        iIngredients.setInputs(FLUID, t.getInputFluids(0));
        iIngredients.setOutputs(FLUID, t.getOutputFluids(0));
    }


    @Override
    public void draw(T recipe, MatrixStack stack, double mouseX, double mouseY) {
        arrow.draw(stack, 34, 6);

    }

    @Override
    public @NotNull List<ITextComponent> getTooltipStrings(@NotNull T recipe, double mouseX, double mouseY) {
        List<ITextComponent> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 6 && mouseY < 20) {
            lines.add(new TranslationTextComponent("boiling.recipe.heat_required", (int)recipe.conversionRate()).withStyle(TextFormatting.GOLD));
        }
        return lines;
    }

    @Override
    public ResourceLocation getUid() {
        return recipeType.getUid();
    }

    @Override
    public Class<? extends T> getRecipeClass() {
        return recipeType.getRecipeClass();
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, T t, IIngredients iIngredients) {
        slots = new IDrawable[2];
        arrow = guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 0, 36, 15)
                .buildAnimated(new TickTimer(100, 36, true), IDrawableAnimated.StartDirection.LEFT);


        iIngredients.setInputs(FLUID, t.getInputFluids(0));
        iIngredients.setOutputs(FLUID, t.getOutputFluids(0));
    }

}
