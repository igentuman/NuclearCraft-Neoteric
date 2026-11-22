package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;


/** JEI category for Kugelblitz transmutation recipes. */
public class KugelblitzRecipeCategory implements IRecipeCategory<RecipeHolder<KugelblitzRecipe>> {

    public static final IRecipeHolderType<KugelblitzRecipe> TYPE =
            IRecipeHolderType.create(NuclearCraft.rl("kugelblitz_chamber"));

    private static final int WIDTH = 124;
    private static final int HEIGHT = 26;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public KugelblitzRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("chamber_terminal").item().get()));
    }

    @Override
    public IRecipeHolderType<KugelblitzRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("screen.nuclearcraft.kugelblitz");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<KugelblitzRecipe> holder, IFocusGroup focuses) {
        KugelblitzRecipe recipe = holder.value();
        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 7, 5);
        if (recipe.input().ingredient().test(recipe.getResultStack())) {
            inputSlot.add(new ItemStack(ModEntries.get("unknown_ingredient").item().get()));
        } else {
            JeiRecipeSlots.addItems(inputSlot, recipe.input());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5)
                .addItemStacks(recipe.output().members());
    }

    @Override
    public void draw(RecipeHolder<KugelblitzRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.text(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8,
                0xFF404040, false);
    }
}
