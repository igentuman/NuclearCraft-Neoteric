package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
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
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        if(arrow.containsKey((int)recipe.getTimeModifier())) {
            arrow.get((int)recipe.getTimeModifier()).draw(graphics, 34, 16);
        }
        int idx = 0;
        for(int i = 0; i < 2; i++) {
            slots[idx].draw(graphics, 11+10*i, 5);
            idx++;
        }
        for(int i = 0; i < 4; i++) {
            slots[idx].draw(graphics, 74+10*i, 5);
            idx++;
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 16 && mouseY < 32) {
            lines.add(Component.translatable("fusion_core.recipe.duration", (int)recipe.getTimeModifier()).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("fusion_core.recipe.power", (int)recipe.getEnergy()).withStyle(ChatFormatting.RED));
            lines.add(Component.translatable("fusion_core.recipe.radiation", recipe.getRadiation()*1000).withStyle(ChatFormatting.GREEN));
            lines.add(Component.translatable("fusion_core.recipe.temperature", (int)recipe.getOptimalTemperature()).withStyle(ChatFormatting.GOLD));
        }
        return lines;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        int d = (int)recipe.getTimeModifier();
        slots = new IDrawable[6];
        if(!timer.containsKey(d)) {
            timer.put(d, new TickTimer((int) (recipe.getTimeModifier() * d) / 200, 36, true));
        }
        if(!arrow.containsKey(d)) {
            arrow.put(d, guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 0, 36, 15)
                    .buildAnimated(timer.get(d), IDrawableAnimated.StartDirection.LEFT));
        }
        int idx = 0;
        for(int i = 0; i < 2; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 12+10*i, 6)
                    .addIngredients(ForgeTypes.FLUID_STACK, recipe.getInputFluids(i))
                    .setFluidRenderer((recipe.getInputFluids()[i].getAmount())/2, false, 6, 34);
            slots[idx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 34, 90, 8, 36);
            idx++;
        }

        for(int i = 0; i < 4; i++) {
            if(recipe.getOutputFluids().length > i) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, 75 + 10 * i, 6)
                        .addIngredients(ForgeTypes.FLUID_STACK, recipe.getOutputFluids(i))
                        .setFluidRenderer((recipe.getOutputFluids()[i].getAmount()) / 2, false, 6, 34);
            }
            slots[idx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 34, 90, 8, 38);
            idx++;
        }
    }
}
