package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.compat.jei.util.TickTimer;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.type.MekChemicalConversionRecipe;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static net.minecraft.world.item.Items.BUCKET;

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
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BUCKET));
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return new TranslatableComponent("nc_jei_cat.mek_chemical_conversion");
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 34 && mouseX < 76 && mouseY > 16 && mouseY < 32) {
            lines.add(new TranslatableComponent("tooltip.nc.jei.gas_to_fluid.desc").withStyle(ChatFormatting.AQUA));
        }
        return lines;
    }

    @Override
    public ResourceLocation getUid() {
        return null;
    }

    @Override
    public Class<? extends T> getRecipeClass() {
        return null;
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, T recipe, @NotNull IFocusGroup focuses) {
        slots = new IDrawable[2];

        arrow = guiHelper.drawableBuilder(rl("textures/gui/progress.png"), 0, 0, 36, 15)
                .buildAnimated(new TickTimer(100, 36, true), IDrawableAnimated.StartDirection.LEFT);

        if(recipe.inputChemical instanceof GasStack) {
            builder.addSlot(RecipeIngredientRole.INPUT, 12, 6)
                    .addIngredients(MekanismJEI.TYPE_GAS, List.of((GasStack) recipe.inputChemical));
        }
        if(recipe.inputChemical instanceof SlurryStack) {
            builder.addSlot(RecipeIngredientRole.INPUT, 12, 6)
                    .addIngredients(MekanismJEI.TYPE_SLURRY, List.of((SlurryStack) recipe.inputChemical));
        }
        slots[0] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 6)
                .addIngredients(ForgeTypes.FLUID_STACK, List.of(recipe.outputFluid))
                .setFluidRenderer(1000, false, 16, 16);
        slots[1] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
    }
}
