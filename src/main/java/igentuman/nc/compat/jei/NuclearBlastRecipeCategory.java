package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.bomb.NuclearBlastRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** JEI category displaying nuclear-blast block transformations with their conversion chance. */
public class NuclearBlastRecipeCategory implements IRecipeCategory<NuclearBlastRecipe> {

    public static final RecipeType<NuclearBlastRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "nuclear_blast", NuclearBlastRecipe.class);

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public NuclearBlastRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModEntries.get("pu_239_bomb").block().get()));
    }

    @Override
    public RecipeType<NuclearBlastRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.nuclearcraft.nuclear_blast");
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
    public void setRecipe(IRecipeLayoutBuilder builder, NuclearBlastRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 5).addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5).addItemStacks(recipe.output().members());
    }

    @Override
    public void draw(NuclearBlastRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8, 0x404040, false);
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("gui.nuclearcraft.nuclear_blast.chance", (int) Math.round(recipe.chance() * 100) + "%")
                        .withStyle(ChatFormatting.DARK_GRAY), 4, 28, 0x404040, false);
    }
}
