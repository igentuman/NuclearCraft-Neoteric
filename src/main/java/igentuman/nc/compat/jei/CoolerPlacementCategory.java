package igentuman.nc.compat.jei;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.util.TextUtils.__;

/**
 * JEI category for displaying accelerator cooler placement conditions
 * Based on the HeatSinkPlacementCategory but adapted for accelerator coolers
 */
public class CoolerPlacementCategory implements IRecipeCategory<CoolerPlacementRecipe> {
    public static final ResourceLocation UID = rl("cooler_placement");
    public static final RecipeType<CoolerPlacementRecipe> TYPE = RecipeType.create(MODID, "cooler_placement", CoolerPlacementRecipe.class);
    
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public CoolerPlacementCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 120);
        // Use empty cooler as icon
        ItemStack emptyCooler = new ItemStack(ACCELERATOR_BLOCKS.get("empty_cooler").get());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, emptyCooler);
        this.title = Component.translatable("jei.category." + MODID + ".cooler_placement");
    }

    @Override
    public RecipeType<CoolerPlacementRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CoolerPlacementRecipe recipe, IFocusGroup focuses) {
        // Input slot for the cooler
        // 1.19.2 JEI: use addSlot(role, x, y) instead of addInputSlot(x, y)
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 8)
                .addIngredient(VanillaTypes.ITEM_STACK, recipe.getCoolerItem());

        // Output slots for required blocks in groups
        List<CoolerPlacementRecipe.PlacementConditionGroup> groups = recipe.getConditionGroups();
        int currentY = 40;
        int maxSlotsPerRow = 9;

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            CoolerPlacementRecipe.PlacementConditionGroup group = groups.get(groupIndex);
            List<ItemStack> requiredBlocks = group.getRequiredBlocks();

            // Add slots for this group's blocks
            for (int i = 0; i < requiredBlocks.size(); i++) {
                int slotX = 8 + (i % maxSlotsPerRow) * 18;
                int slotY = currentY + (i / maxSlotsPerRow) * 18;

                builder.addSlot(RecipeIngredientRole.OUTPUT, slotX, slotY)
                        .addIngredient(VanillaTypes.ITEM_STACK, requiredBlocks.get(i));
            }

            // Move to next group position
            int rowsUsed = (requiredBlocks.size() + maxSlotsPerRow - 1) / maxSlotsPerRow;
            currentY += rowsUsed * 18 + 20; // 20 pixels for condition text
        }
    }

    @Override
    public void draw(CoolerPlacementRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Draw heat value using translation
        String heatText = __("heat_sink.heat.descr", String.valueOf((int)recipe.getCoolerDef().getHeat())).getString();
        font.draw(poseStack, heatText, 30f, 8f, Color.CYAN.getRGB());

        // Draw condition groups
        List<CoolerPlacementRecipe.PlacementConditionGroup> groups = recipe.getConditionGroups();
        int currentY = 30;
        int maxSlotsPerRow = 8;

        poseStack.pushPose();
        poseStack.scale(0.8F, 0.8F, 1F);

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            CoolerPlacementRecipe.PlacementConditionGroup group = groups.get(groupIndex);

            // Draw condition text
            String conditionText = group.getConditionText();
            font.draw(poseStack, conditionText, 10f, (float)((currentY + 2) / 0.8F), Color.DARK_GRAY.getRGB());

            // Calculate space needed for this group
            int rowsUsed = (group.getRequiredBlocks().size() + maxSlotsPerRow - 1) / maxSlotsPerRow;
            currentY += rowsUsed * 18 + 20; // 25 pixels for condition text and spacing
        }

        poseStack.popPose();
    }
}