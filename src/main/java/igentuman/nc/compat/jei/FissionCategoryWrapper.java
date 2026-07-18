package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.compat.jei.util.TickTimer;
import mezz.jei.api.constants.VanillaTypes;
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
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.*;
import static igentuman.nc.compat.GlobalVars.*;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.numberFormat;
import static net.minecraft.world.item.Items.BARRIER;

@SuppressWarnings("removal")
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
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get()));
        } else{
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BARRIER));
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 29 && mouseX < 65 && mouseY > 8 && mouseY < 24) {
            lines.add(__("fission.recipe.duration", (int)((double)recipe.getDepletionTime()/20)).withStyle(ChatFormatting.AQUA));
            lines.add(__("fission.recipe.power", (int)recipe.getEnergy()).withStyle(ChatFormatting.RED));
            lines.add(__("fission.recipe.radiation", numberFormat(recipe.getRadiation()*1000000)).withStyle(ChatFormatting.GREEN));
            lines.add(__("fission.recipe.heat", (int)recipe.getHeat()).withStyle(ChatFormatting.GOLD));
        }
        return lines;
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return __("nc_jei_cat."+getRecipeType().getUid().getPath());
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
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
                     double mouseY) {
        if(arrow.containsKey(recipe.getDepletionTime())) {
            arrow.get(recipe.getDepletionTime()).draw(stack, 29, 8);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        int d = recipe.getDepletionTime();
        if(!timer.containsKey(d)) {
            timer.put(d, new TickTimer((int) (recipe.getTimeModifier() * d) / 50, 36, true));
        }
        if(!arrow.containsKey(d)) {
            arrow.put(d, guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 186, 36, 15)
                    .buildAnimated(timer.get(d), IDrawableAnimated.StartDirection.LEFT));
        }
        for(int i = 0; i < recipe.getItemIngredients().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11+18*i, 7).addIngredients(recipe.getItemIngredients().get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 7).addItemStack(recipe.getResultItem());
    }
}
