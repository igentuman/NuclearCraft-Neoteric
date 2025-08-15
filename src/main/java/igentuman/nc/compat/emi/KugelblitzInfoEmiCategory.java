package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.UNKNOWN_INGREDIENT;
import static igentuman.nc.util.TextUtils.__;

/**
 * EMI category for displaying Kugelblitz chamber information
 * Equivalent to JEI's addIngredientInfo functionality
 */
public class KugelblitzInfoEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("kugelblitz_info"),
            EmiStack.of(new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get()))
    );

    private final ItemStack itemStack;
    private final boolean isUnknownIngredient;

    public KugelblitzInfoEmiCategory(ResourceLocation id, ItemStack itemStack) {
        super(CATEGORY, id, 160, 160);
        this.itemStack = itemStack;
        this.isUnknownIngredient = itemStack.getItem() == UNKNOWN_INGREDIENT.get();
        
        // Add the item as input for visibility
        EmiStack emiStack = EmiStack.of(itemStack);
        this.inputs.add(emiStack);
        this.outputs.add(emiStack);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add item slot
        widgets.addSlot(inputs.get(0), 2, 2).recipeContext(this);
        
        // Add item name
        String itemName = isUnknownIngredient ? "Unknown Ingredient" : itemStack.getHoverName().getString();
        widgets.addText(Component.literal(itemName), 25, 2, Color.WHITE.getRGB(), false);
        
        // Add information text with smaller scale
        int yOffset = 20;
        int lineHeight = 10;

        // Description
        String description = __("jei.info.nuclearcraft.kugelblitz.description").getString();
        List<String> descriptionLines = wrapText(description, 30);
        for (String line : descriptionLines) {
            widgets.addText(Component.literal(line), 0, yOffset, Color.DARK_GRAY.getRGB(), false);
            yOffset += lineHeight;
        }
        
        yOffset += 5; // Extra spacing
        
        // Problem
        String problem = __("jei.info.nuclearcraft.kugelblitz.problem").getString();
        List<String> problemLines = wrapText(problem, 30);
        for (String line : problemLines) {
            widgets.addText(Component.literal(line), 0, yOffset, Color.DARK_GRAY.getRGB(), false);
            yOffset += lineHeight;
        }
        
        yOffset += 5; // Extra spacing
        
        // Input/Output info
        String inputOutput = __("jei.info.nuclearcraft.kugelblitz.input_output").getString();
        List<String> inputOutputLines = wrapText(inputOutput, 30);
        for (String line : inputOutputLines) {
            widgets.addText(Component.literal(line), 0, yOffset, Color.DARK_GRAY.getRGB(), false);
            yOffset += lineHeight;
        }
    }

    /**
     * Wraps text to fit within a specified character limit per line
     */
    private List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            // Check if adding this word would exceed the limit
            if (currentLine.length() + word.length() + 1 > maxCharsPerLine) {
                // If current line is not empty, add it to lines
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                
                // If the word itself is longer than the limit, break it
                if (word.length() > maxCharsPerLine) {
                    while (word.length() > maxCharsPerLine) {
                        lines.add(word.substring(0, maxCharsPerLine));
                        word = word.substring(maxCharsPerLine);
                    }
                    if (!word.isEmpty()) {
                        currentLine.append(word);
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                // Add word to current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        // Add the last line if it's not empty
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }

    public static Component getTitle() {
        return Component.translatable("emi.category.nuclearcraft.kugelblitz_info");
    }
}