package igentuman.nc.compat.jei;

import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.util.TextUtils;
import mezz.jei.api.constants.VanillaTypes;
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

import java.awt.Color;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.__;

public class FuelInfoCategory implements IRecipeCategory<FuelInfoRecipe> {

    public static final RecipeType<FuelInfoRecipe> TYPE = RecipeType.create(MODID, "fuel_info", FuelInfoRecipe.class);

    private static final int WIDTH = 176;
    private static final int ROW_HEIGHT = 26;
    private static final int HEADER = 12;
    private static final int MAX_ROWS = 5;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final IDrawable slot;

    public FuelInfoCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEADER + MAX_ROWS * ROW_HEIGHT + 10);
        this.icon = guiHelper.createDrawableItemStack(firstFuelStack());
        this.title = Component.translatable("jei.category." + MODID + ".fuel_info");
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<FuelInfoRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return title; }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FuelInfoRecipe recipe, IFocusGroup focuses) {
        // Add base item as catalyst-like input so it shows in lookup
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addItemStack(recipe.getBaseItem());

        int y = HEADER;
        for (FuelInfoRecipe.Variant v : recipe.getVariants()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 2, y)
                    .addItemStack(v.item);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public void draw(FuelInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        font.draw(poseStack, __("jei.nuclearcraft.fuel_info.title", recipe.getName().toUpperCase()), 2f, 1f, 0xFF404040);

        int y = HEADER;
        int textX = 24;
        for (FuelInfoRecipe.Variant v : recipe.getVariants()) {
            poseStack.pushPose();

            font.draw(poseStack, __(v.labelKey).getString(), (float)textX, (float)(y+3), 0xFF202020);
            poseStack.scale(0.7F, 0.7F, 1F);
            int sx = (int)(textX-12 / 0.7F);
            int sy = (int)((y + 10) / 0.7F);
            String stats;
            if (v.triso) {
                stats = __("jei.nuclearcraft.fuel_info.row_triso",
                        v.def.criticality,
                        formatHeat(v.def.getHeatFEMode()),
                        v.def.depletion
                ).getString();
            } else {
                stats = __("jei.nuclearcraft.fuel_info.row",
                        v.def.forge_energy,
                        formatHeat(v.def.getHeatFEMode()),
                        v.def.depletion,
                        TextUtils.numberFormat(Math.log((ItemRadiation.byItem(v.item.getItem())+0.01)*10000)*(Math.pow(v.def.heat / 100 +  200 / (double)v.def.depletion + 0.5, 1.5)*2))
                ).getString();
            }
            font.draw(poseStack, stats, (float)sx, (float)(sy + 10), Color.darkGray.getRGB());
            poseStack.popPose();
            y += ROW_HEIGHT;
        }
    }

    private static ItemStack firstFuelStack() {
        return FissionFuel.NC_FUEL.values().stream().findFirst()
                .map(ro -> new ItemStack(ro.get()))
                .orElse(ItemStack.EMPTY);
    }

    private static String formatHeat(double heat) {
        if (heat >= 1000) return String.format("%.1fk", heat / 1000);
        return String.format("%.0f", heat);
    }
}
