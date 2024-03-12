package igentuman.nc.compat.jei;

import igentuman.nc.compat.jei.util.TickTimer;
import igentuman.nc.recipes.type.MekChemicalConversionRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static mezz.jei.api.constants.VanillaTypes.FLUID;
import static net.minecraft.item.Items.BUCKET;
import static net.minecraft.util.text.TextFormatting.AQUA;

@SuppressWarnings("all")
public class MekChemicalConversionCategoryWrapper<T extends MekChemicalConversionRecipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/processor_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    HashMap<Integer, TickTimer> timer = new HashMap<>();
    IDrawable arrow;
    private  IDrawable[] slots;

    IGuiHelper guiHelper;

    public MekChemicalConversionCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = (RecipeType<T>) recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 105, 32);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(BUCKET));
    }

    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull String getTitle() {
        return new TranslationTextComponent("nc_jei_cat.mek_chemical_conversion").getString();
    }

    @Override
    public @NotNull List<ITextComponent> getTooltipStrings(@NotNull T recipe, double mouseX, double mouseY) {
        List<ITextComponent> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 16 && mouseY < 32) {
            lines.add(new TranslationTextComponent("tooltip.nc.jei.gas_to_fluid.desc").withStyle(AQUA));
        }
        return lines;
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
    public ResourceLocation getUid() {
        return recipeType.getUid();
    }

    @Override
    public Class<? extends T> getRecipeClass() {
        return recipeType.getRecipeClass();
    }

    @Override
    public void setIngredients(T t, IIngredients iIngredients) {
        iIngredients.setInputs(FLUID, t.getInputFluids(0));
        iIngredients.setOutputs(FLUID, t.getOutputFluids(0));
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
