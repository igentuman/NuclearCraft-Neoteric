package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.util.TextUtils.__;

/**
 * EMI category for displaying heat sink placement conditions
 * Based on the JEI HeatSinkPlacementCategory but adapted for EMI
 */
public class HeatSinkPlacementEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("heat_sink_placement"),
            EmiStack.of(new ItemStack(FISSION_BLOCKS.get("empty_heat_sink").get()))
    );

    private final HeatSinkPlacementEmiRecipe recipe;

    public HeatSinkPlacementEmiCategory(HeatSinkPlacementEmiRecipe recipe) {
        super(CATEGORY, recipe.getId(), 176, calculateHeight(recipe));
        this.recipe = recipe;
        
        // Add heat sink as input
        this.inputs.add(EmiStack.of(recipe.getHeatSinkItem()));
        
        // Add all required blocks as outputs
        for (HeatSinkPlacementEmiRecipe.PlacementConditionGroup group : recipe.getConditionGroups()) {
            for (ItemStack blockItem : group.getRequiredBlocks()) {
                this.outputs.add(EmiStack.of(blockItem));
            }
        }
    }

    private static int calculateHeight(HeatSinkPlacementEmiRecipe recipe) {
        int height = 60; // Base height for heat sink slot and heat text
        int maxSlotsPerRow = 9;
        
        for (HeatSinkPlacementEmiRecipe.PlacementConditionGroup group : recipe.getConditionGroups()) {
            int rowsUsed = (group.getRequiredBlocks().size() + maxSlotsPerRow - 1) / maxSlotsPerRow;
            height += rowsUsed * 18 + 20; // 18 pixels per row + 20 for condition text
        }
        
        return Math.max(height, 120); // Minimum height
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Input slot for the heat sink
        widgets.addSlot(inputs.get(0), 8, 8).recipeContext(this);

        // Draw heat value using translation
        String heatText = __("heat_sink.heat.descr", String.valueOf((int)recipe.getHeatSinkDef().getHeat())).getString();
        widgets.addText(Component.literal(heatText), 30, 8, Color.ORANGE.getRGB(), false);
        
        // Output slots for required blocks in groups
        List<HeatSinkPlacementEmiRecipe.PlacementConditionGroup> groups = recipe.getConditionGroups();
        int currentY = 40;
        int maxSlotsPerRow = 9;
        int outputIndex = 0;
        
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            HeatSinkPlacementEmiRecipe.PlacementConditionGroup group = groups.get(groupIndex);
            List<ItemStack> requiredBlocks = group.getRequiredBlocks();
            
            // Add condition text
            String conditionText = group.getConditionText();
            widgets.addText(Component.literal(conditionText), 10, currentY - 12, Color.DARK_GRAY.getRGB(), false);
            
            // Add slots for this group's blocks
            for (int i = 0; i < requiredBlocks.size(); i++) {
                int slotX = 8 + (i % maxSlotsPerRow) * 18;
                int slotY = currentY + (i / maxSlotsPerRow) * 18;
                
                widgets.addSlot(outputs.get(outputIndex), slotX, slotY).recipeContext(this);
                outputIndex++;
            }
            
            // Move to next group position
            int rowsUsed = (requiredBlocks.size() + maxSlotsPerRow - 1) / maxSlotsPerRow;
            currentY += rowsUsed * 18 + 20; // 20 pixels for condition text and spacing
        }
    }

    public static Component getTitle() {
        return Component.translatable("emi.category." + MODID + ".heat_sink_placement");
    }
}