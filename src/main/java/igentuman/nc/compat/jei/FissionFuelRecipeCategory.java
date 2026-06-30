package igentuman.nc.compat.jei;

import igentuman.nc.Main;
import igentuman.nc.recipe.fission.FissionFuelRecipe;
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

public class FissionFuelRecipeCategory implements IRecipeCategory<FissionFuelRecipe> {

    public static final RecipeType<FissionFuelRecipe> TYPE =
            RecipeType.create(Main.MODID, "fission_fuel", FissionFuelRecipe.class);

    private static final int WIDTH = 124;
    private static final int HEIGHT = 56;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public FissionFuelRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModEntries.get("fission_reactor_controller").item().get()));
    }

    @Override
    public RecipeType<FissionFuelRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.nuclearcraft.fission_fuel");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FissionFuelRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 5).addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5).addItemStacks(recipe.output().members());
    }

    @Override
    public void draw(FissionFuelRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8, 0x404040, false);

        var font = Minecraft.getInstance().font;
        int y = 26;
        graphics.drawString(font, Component.translatable("tooltip.nuclearcraft.fuel.forge_energy", recipe.power() + " FE/t")
                .withStyle(ChatFormatting.DARK_GRAY), 4, y, 0x404040, false);
        graphics.drawString(font, Component.translatable("tooltip.nuclearcraft.fuel.heat", recipe.heat())
                .withStyle(ChatFormatting.DARK_GRAY), 4, y + 10, 0x404040, false);
        graphics.drawString(font, Component.translatable("tooltip.nuclearcraft.fuel.depletion", recipe.processTime())
                .withStyle(ChatFormatting.DARK_GRAY), 4, y + 20, 0x404040, false);
    }
}
