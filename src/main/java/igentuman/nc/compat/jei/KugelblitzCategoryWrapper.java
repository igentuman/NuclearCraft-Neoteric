package igentuman.nc.compat.jei;

import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.compat.jei.util.TickTimer;
import igentuman.nc.recipes.ingredient.NcIngredient;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.UNKNOWN_INGREDIENT;
import static igentuman.nc.util.StackUtils.resolveStackByModPriority;
import static igentuman.nc.util.TextUtils.__;
import static net.minecraft.world.item.Items.BARRIER;

@SuppressWarnings("removal")
public class KugelblitzCategoryWrapper<T extends ChamberTerminalBE.Recipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/fission/jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;
    HashMap<Integer, TickTimer> timer = new HashMap<>();
    HashMap<Integer, IDrawable> arrow = new HashMap<>();

    IGuiHelper guiHelper;

    public KugelblitzCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 98, 30);
        if(CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get()));
        } else{
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BARRIER));
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 29 && mouseX < 65 && mouseY > 8 && mouseY < 24) {
            lines.add(__("processor.recipe.duration", (int)(recipe.getTimeModifier()*20)).withStyle(ChatFormatting.AQUA));
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
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
                     double mouseY) {
        if(arrow.containsKey(recipe.getTimeModifier())) {
            arrow.get(recipe.getTimeModifier()).draw(graphics, 29, 8);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        int d = (int) (100 * recipe.getTimeModifier());
        if(!timer.containsKey(d)) {
            timer.put(d, new TickTimer((int) (recipe.getTimeModifier() * d) / 50, 36, true));
        }
        if(!arrow.containsKey(d)) {
            arrow.put(d, guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 186, 36, 15)
                    .buildAnimated(timer.get(d), IDrawableAnimated.StartDirection.LEFT));
        }
        for(int i = 0; i < recipe.getItemIngredients().size(); i++) {
            if(recipe.getResultItem().is(resolveStackByModPriority(recipe.getItemIngredients().get(i).getItems()).getItem())) {
                builder.addSlot(RecipeIngredientRole.INPUT, 11+18*i, 7).addIngredients(NcIngredient.of(UNKNOWN_INGREDIENT.get()));
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, 11+18*i, 7).addIngredients(recipe.getItemIngredients().get(i));
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 7).addItemStack(recipe.getResultItem());
    }
}
