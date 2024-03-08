package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
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

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
@SuppressWarnings("all")
public class FusionCategoryWrapper<T extends FusionCoreBE.Recipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/processor_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    HashMap<Integer, TickTimer> timer = new HashMap<>();
    HashMap<Integer, IDrawable> arrow = new HashMap<>();
    private  IDrawable[] slots;

    IGuiHelper guiHelper;

    public FusionCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 136, 45);
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

    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, T t, IIngredients iIngredients) {

    }



    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 16 && mouseY < 32) {
            lines.add(new TranslatableComponent("fusion_core.recipe.duration", (int)recipe.getTimeModifier()).withStyle(ChatFormatting.AQUA));
            lines.add(new TranslatableComponent("fusion_core.recipe.power", (int)recipe.getEnergy()).withStyle(ChatFormatting.RED));
            lines.add(new TranslatableComponent("fusion_core.recipe.radiation", recipe.getRadiation()*1000).withStyle(ChatFormatting.GREEN));
            lines.add(new TranslatableComponent("fusion_core.recipe.temperature", (int)recipe.getOptimalTemperature()).withStyle(ChatFormatting.GOLD));
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
