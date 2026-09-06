package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.OreVeinRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/** JEI category describing the weighted ore pools used by in-situ veins. */
public class OreVeinRecipeCategory implements IRecipeCategory<OreVeinRecipe> {

    public static final RecipeType<OreVeinRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "nc_ore_veins", OreVeinRecipe.class);

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawable icon;

    public OreVeinRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.DIAMOND_PICKAXE));
    }

    @Override
    public RecipeType<OreVeinRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.nuclearcraft.ore_veins");
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
    public void setRecipe(IRecipeLayoutBuilder builder, OreVeinRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.getOres().size(); i++) {
            OreVeinRecipe.OreEntry ore = recipe.getOres().get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 7 + i * 20, 5)
                    .addItemStacks(Arrays.stream(ore.ingredient().ingredient().getItems())
                            .map(stack -> stack.copyWithCount(ore.ingredient().count()))
                            .toList())
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                            Component.translatable("gui.nuclearcraft.ore_veins.weight", ore.weight())));
        }
    }

    @Override
    public void draw(OreVeinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Component name = Component.translatable("nc.ore_vein." + recipe.getId().getPath());
        graphics.drawString(Minecraft.getInstance().font, name.copy().withStyle(ChatFormatting.DARK_GRAY),
                4, 27, 0x404040, false);
    }
}
