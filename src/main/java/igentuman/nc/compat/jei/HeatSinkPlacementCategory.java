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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.util.TextUtils.__;

public class HeatSinkPlacementCategory implements IRecipeCategory<HeatSinkPlacementRecipe> {
    public static final ResourceLocation UID = rl("heat_sink_placement");
    public static final RecipeType<HeatSinkPlacementRecipe> TYPE = RecipeType.create(MODID, "heat_sink_placement", HeatSinkPlacementRecipe.class);
    
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public HeatSinkPlacementCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 120);
        // Use empty heat sink as icon
        ItemStack emptyHeatSink = new ItemStack(FISSION_BLOCKS.get("empty_heat_sink").get());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, emptyHeatSink);
        this.title = Component.translatable("jei.category." + MODID + ".heat_sink_placement");
    }

    @Override
    public RecipeType<HeatSinkPlacementRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, HeatSinkPlacementRecipe recipe, IFocusGroup focuses) {
        // Input slot for the heat sink
        builder.addInputSlot(8, 8)
                .addIngredient(VanillaTypes.ITEM_STACK, recipe.getHeatSinkItem());

        // Output slots for required blocks in groups
        List<HeatSinkPlacementRecipe.PlacementConditionGroup> groups = recipe.getConditionGroups();
        int currentY = 40;
        int maxSlotsPerRow = 9;
        
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            HeatSinkPlacementRecipe.PlacementConditionGroup group = groups.get(groupIndex);
            List<ItemStack> requiredBlocks = group.getRequiredBlocks();
            
            // Add slots for this group's blocks
            for (int i = 0; i < requiredBlocks.size(); i++) {
                int slotX = 8 + (i % maxSlotsPerRow) * 18;
                int slotY = currentY + (i / maxSlotsPerRow) * 18;
                
                builder.addOutputSlot(slotX, slotY)
                        .addIngredient(VanillaTypes.ITEM_STACK, requiredBlocks.get(i));
            }
            
            // Move to next group position
            int rowsUsed = (requiredBlocks.size() + maxSlotsPerRow - 1) / maxSlotsPerRow;
            currentY += rowsUsed * 18 + 20; // 20 pixels for condition text
        }
    }

    @Override
    public void draw(HeatSinkPlacementRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Draw heat value using translation
        String heatText = __("heat_sink.heat.descr", String.valueOf((int)recipe.getHeatSinkDef().getHeat())).getString();
        graphics.drawString(font, heatText, 30, 8, Color.ORANGE.getRGB());
        
        // Draw condition groups
        List<HeatSinkPlacementRecipe.PlacementConditionGroup> groups = recipe.getConditionGroups();
        int currentY = 30;
        int maxSlotsPerRow = 8;

        graphics.pose().pushPose();
        graphics.pose().scale(0.8F, 0.8F, 1F);
        
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            HeatSinkPlacementRecipe.PlacementConditionGroup group = groups.get(groupIndex);
            
            // Draw condition text
            String conditionText = group.getConditionText();
            graphics.drawString(font, conditionText, 10, (int)((currentY + 2) / 0.8F), Color.DARK_GRAY.getRGB(), false);
            
            // Calculate space needed for this group
            int rowsUsed = (group.getRequiredBlocks().size() + maxSlotsPerRow - 1) / maxSlotsPerRow;
            currentY += rowsUsed * 18 + 20; // 25 pixels for condition text and spacing
        }
        
        graphics.pose().popPose();
    }
}