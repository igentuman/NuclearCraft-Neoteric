package igentuman.nc.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
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
import net.minecraftforge.fluids.FluidStack;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static mezz.jei.api.constants.VanillaTypes.FLUID;
@SuppressWarnings("all")
public class FusionCategoryWrapper<T extends FusionCoreBE.Recipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/processor_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    private IDrawable arrow;
    private IDrawable fluidInSlot;
    private IDrawable fluidOutSlot;
    private static final int[][] IN_POS = {{7, 4}, {7, 25}};
    private static final int[][] OUT_POS = {{92, 4}, {110, 4}, {92, 25}, {110, 25}};

    IGuiHelper guiHelper;

    public FusionCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 136, 45);
        this.fluidInSlot = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
        this.fluidOutSlot = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 36, 18, 18);
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
        List<List<FluidStack>> inputs = new ArrayList<>();
        for(int i = 0; i < t.getInputFluids().length; i++) {
            inputs.add(t.getInputFluids(i));
        }
        iIngredients.setInputLists(FLUID, inputs);
        iIngredients.setOutputs(FLUID, t.getOutputFluids());
    }

    @Override
    public void draw(T recipe, MatrixStack stack, double mouseX, double mouseY) {
        if(arrow != null) arrow.draw(stack, 40, 15);
        int inCount = Math.min(recipe.getInputFluids().length, IN_POS.length);
        for(int i = 0; i < inCount; i++) {
            fluidInSlot.draw(stack, IN_POS[i][0], IN_POS[i][1]);
        }
        int outCount = Math.min(recipe.getOutputFluids().size(), OUT_POS.length);
        for(int i = 0; i < outCount; i++) {
            fluidOutSlot.draw(stack, OUT_POS[i][0], OUT_POS[i][1]);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, T t, IIngredients iIngredients) {
        arrow = guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 0, 36, 15)
                .buildAnimated(new TickTimer(100, 36, true), IDrawableAnimated.StartDirection.LEFT);
        int inCount = Math.min(t.getInputFluids().length, IN_POS.length);
        for(int i = 0; i < inCount; i++) {
            iRecipeLayout.getFluidStacks().init(i, true, IN_POS[i][0] + 1, IN_POS[i][1] + 1);
            iRecipeLayout.getFluidStacks().set(i, t.getInputFluids(i));
        }
        int outCount = Math.min(t.getOutputFluids().size(), OUT_POS.length);
        for(int i = 0; i < outCount; i++) {
            iRecipeLayout.getFluidStacks().init(IN_POS.length + i, false, OUT_POS[i][0] + 1, OUT_POS[i][1] + 1);
            iRecipeLayout.getFluidStacks().set(IN_POS.length + i, t.getOutputFluids(i));
        }
    }



    @Override
    public @NotNull List<ITextComponent> getTooltipStrings(T recipe, double mouseX, double mouseY) {
        List<ITextComponent> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 16 && mouseY < 32) {
            lines.add(new TranslationTextComponent("fusion_core.recipe.duration", (int)recipe.getTimeModifier()).withStyle(TextFormatting.AQUA));
            lines.add(new TranslationTextComponent("fusion_core.recipe.power", (int)recipe.getEnergy()).withStyle(TextFormatting.RED));
            lines.add(new TranslationTextComponent("fusion_core.recipe.radiation", recipe.getRadiation()*1000).withStyle(TextFormatting.GREEN));
            lines.add(new TranslationTextComponent("fusion_core.recipe.temperature", (int)recipe.getOptimalTemperature()).withStyle(TextFormatting.GOLD));
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

}
