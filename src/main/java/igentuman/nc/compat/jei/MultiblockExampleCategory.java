package igentuman.nc.compat.jei;

import net.minecraft.client.Minecraft;
import igentuman.nc.NuclearCraft;
import igentuman.nc.util.StructurePreviewRenderer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** JEI category rendering an interactive 3D preview of a multiblock's example structure. */
public class MultiblockExampleCategory implements IRecipeCategory<MultiblockStructureRecipe> {

    public static final IRecipeType<MultiblockStructureRecipe> TYPE =
            IRecipeType.create(NuclearCraft.MODID, "multiblock_examples", MultiblockStructureRecipe.class);

    private static final int WIDTH = 140;
    private static final int HEIGHT = 140;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public MultiblockExampleCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.title = Component.translatable("category." + NuclearCraft.MODID + ".multiblock_examples");
        this.icon = guiHelper.createDrawableItemStack(defaultIcon());
    }

    private static ItemStack defaultIcon() {
        return new ItemStack(net.minecraft.world.level.block.Blocks.IRON_BLOCK);
    }

    @Override
    public IRecipeType<MultiblockStructureRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return title; }

    @Override
    public @Nullable IDrawable getIcon() { return icon; }

    @Override
    public int getWidth() { return background.getWidth(); }

    @Override
    public int getHeight() { return background.getHeight(); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockStructureRecipe recipe, IFocusGroup focuses) {
    }

    @Override
    public void draw(MultiblockStructureRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        Component name = Component.translatable("multiblock." + NuclearCraft.MODID + "." + recipe.getName());
        graphics.text(net.minecraft.client.Minecraft.getInstance().font, name, 2, 2, 0xFFFFFFFF, false);

        if (recipe.getStructure() != null) {
            StructurePreviewRenderer.render(graphics, recipe.getStructure(), 0, 12, WIDTH, HEIGHT - 14, mouseX, mouseY);

            Component dims = Component.literal(
                    recipe.getStructure().getWidth() + "x" +
                    recipe.getStructure().getHeight() + "x" +
                    recipe.getStructure().getDepth());
            graphics.text(net.minecraft.client.Minecraft.getInstance().font, dims,
                    WIDTH - net.minecraft.client.Minecraft.getInstance().font.width(dims) - 2,
                    HEIGHT - 10, 0xFFAAAAAA, false);
        }
    }
}
