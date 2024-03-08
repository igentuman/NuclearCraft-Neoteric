package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.compat.jei.util.TickTimer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.*;
import static igentuman.nc.compat.GlobalVars.*;
import static igentuman.nc.util.TextUtils.numberFormat;
@SuppressWarnings("all")
public class FissionCategoryWrapper<T extends FissionControllerBE.Recipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/fission/jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    HashMap<Integer, TickTimer> timer = new HashMap<>();
    HashMap<Integer, IDrawable> arrow = new HashMap<>();

    IGuiHelper guiHelper;

    public FissionCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 98, 30);
        if(CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(CATALYSTS.get(getRecipeType().getUid().getPath()).get(0));
        } else{
            this.icon = guiHelper.createDrawableIngredient(ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 29 && mouseX < 65 && mouseY > 8 && mouseY < 24) {
            lines.add(new TranslatableComponent("fission.recipe.duration", (int)((double)recipe.getDepletionTime()/20)).withStyle(ChatFormatting.AQUA));
            lines.add(new TranslatableComponent("fission.recipe.power", (int)recipe.getEnergy()).withStyle(ChatFormatting.RED));
            lines.add(new TranslatableComponent("fission.recipe.radiation", numberFormat(recipe.getRadiation()*1000000)).withStyle(ChatFormatting.GREEN));
            lines.add(new TranslatableComponent("fission.recipe.heat", (int)recipe.getHeat()).withStyle(ChatFormatting.GOLD));
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

    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return new TranslatableComponent("nc_jei_cat."+getRecipeType().getUid().getPath());
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
        iIngredients.setInput(VanillaTypes.ITEM, t.getFirstItemStackIngredient(0));
        iIngredients.setOutput(VanillaTypes.ITEM, t.getResultItem());
    }

    @Override
    public void draw(T recipe, PoseStack stack, double mouseX, double mouseY) {
        if(arrow.containsKey(recipe.getDepletionTime())) {
            arrow.get(recipe.getDepletionTime()).draw(stack, 29, 8);
        }
    }


    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, T t, IIngredients iIngredients) {
        int d = t.getDepletionTime();
        if(!timer.containsKey(d)) {
            timer.put(d, new TickTimer((int) (t.getTimeModifier() * d) / 50, 36, true));
        }
        if(!arrow.containsKey(d)) {
            arrow.put(d, guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 186, 36, 15)
                    .buildAnimated(timer.get(d), IDrawableAnimated.StartDirection.LEFT));
        }
        iRecipeLayout.getItemStacks().init(0, true, 10, 7);
        iRecipeLayout.getItemStacks().init(1, false, 73, 7);
        iRecipeLayout.getItemStacks().set(0, iIngredients.getInputs(VanillaTypes.ITEM).get(0));
        iRecipeLayout.getItemStacks().set(1, iIngredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

}
