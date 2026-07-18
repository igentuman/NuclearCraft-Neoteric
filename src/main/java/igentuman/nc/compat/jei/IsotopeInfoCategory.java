package igentuman.nc.compat.jei;

import igentuman.nc.setup.registration.FissionFuel;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.capitalize;

public class IsotopeInfoCategory implements IRecipeCategory<IsotopeInfoRecipe> {

    public static final RecipeType<IsotopeInfoRecipe> TYPE = RecipeType.create(MODID, "isotope_info", IsotopeInfoRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEADER = 12;
    private static final int SLOT_SIZE = 22;
    private static final int LABEL_GAP = 14;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public IsotopeInfoCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEADER + SLOT_SIZE + LABEL_GAP + 4);
        this.icon = guiHelper.createDrawableItemStack(firstIsotopeStack());
        this.title = Component.translatable("jei.category." + MODID + ".isotope_info");
    }

    @Override
    public RecipeType<IsotopeInfoRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return title; }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

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
    public void draw(IsotopeInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        font.draw(poseStack, __("jei.nuclearcraft.isotope_info.title", capitalize(recipe.getName())), 2f, 1f, 0xFF404040);

        int x = 4;
        for (IsotopeInfoRecipe.Variant v : recipe.getVariants()) {
            poseStack.pushPose();
            poseStack.scale(0.7F, 0.7F, 1F);
            int sx = (int) (x / 0.7F);
            int sy = (int) ((HEADER + SLOT_SIZE + 2) / 0.7F);
            font.draw(poseStack, __(v.labelKey).getString(), (float)sx, (float)sy, 0xFF202020);
            poseStack.popPose();
            x += SLOT_SIZE + 12;
        }
    }

    private static ItemStack firstIsotopeStack() {
        return FissionFuel.NC_ISOTOPES.values().stream().findFirst()
                .map(ro -> new ItemStack(ro.get()))
                .orElse(ItemStack.EMPTY);
    }
}
