package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.convertToName;

public class IsotopeInfoCategory implements IRecipeCategory<IsotopeInfoRecipe> {

    public static final RecipeType<IsotopeInfoRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "isotope_info", IsotopeInfoRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEADER = 12;
    private static final int SLOT_SIZE = 22;
    private static final int LABEL_GAP = 14;

    private final IDrawable icon;
    private final IDrawable slot;

    public IsotopeInfoCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(firstIsotopeStack());
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<IsotopeInfoRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category." + NuclearCraft.MODID + ".isotope_info");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEADER + SLOT_SIZE + LABEL_GAP + 4; }

    @Override
    public @Nullable IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IsotopeInfoRecipe recipe, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addItemStack(recipe.getBaseItem());

        int x = 4;
        for (IsotopeInfoRecipe.Variant v : recipe.getVariants()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, HEADER)
                    .addItemStack(v.item);
            x += SLOT_SIZE + 12;
        }
    }

    @Override
    public void draw(IsotopeInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, __("jei.nuclearcraft.isotope_info.title", convertToName(recipe.getName())), 2, 1, 0xFF404040, false);

        int x = 4;
        for (IsotopeInfoRecipe.Variant v : recipe.getVariants()) {
            slot.draw(graphics, x - 1, HEADER - 1);
            graphics.pose().pushPose();
            graphics.pose().scale(0.7F, 0.7F, 1F);
            int sx = (int) (x / 0.7F);
            int sy = (int) ((HEADER + SLOT_SIZE + 2) / 0.7F);
            graphics.drawString(font, __(v.labelKey).getString(), sx, sy, 0xFF202020, false);
            graphics.pose().popPose();
            x += SLOT_SIZE + 12;
        }
    }

    private static ItemStack firstIsotopeStack() {
        return ModEntries.ISOTOPES.values().stream().findFirst()
                .map(e -> new ItemStack(e.base().get()))
                .orElse(ItemStack.EMPTY);
    }
}
