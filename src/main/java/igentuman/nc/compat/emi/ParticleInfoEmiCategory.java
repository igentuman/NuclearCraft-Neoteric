package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.emi.ingredient.ParticleEmiStack;
import igentuman.nc.compat.jei.ParticleRecipe;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.util.Units;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/**
 * EMI category for displaying particle information
 * Based on the JEI ParticleInfoCategory but adapted for EMI
 */
public class ParticleInfoEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("particle_info"),
            ParticleEmiStack.of(new ParticleStack(Particles.proton))
    );

    private final ParticleRecipe recipe;

    public ParticleInfoEmiCategory(ParticleRecipe recipe) {
        super(CATEGORY, recipe.getId(), 160, 180);
        this.recipe = recipe;
        
        // Add the particle as both input and output for visibility
        ParticleEmiStack particleStack = ParticleEmiStack.of(recipe.getIngredient());
        this.inputs.add(particleStack);
        this.outputs.add(particleStack);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add particle slot
        widgets.addSlot(inputs.get(0), 2, 2).recipeContext(this);
        
        // Add particle information as text widgets
        DecimalFormat df = new DecimalFormat("#.##");
        
        // Particle name
        widgets.addText(__(recipe.getName()), 25, 2, Color.WHITE.getRGB(), false);
        
        // Particle properties with smaller scale
        int yOffset = 20;
        int lineHeight = 10;

        // Mass
        widgets.addText(__("gui.nuclearcraft.jei.particle.mass", 
            Units.getSIFormat(recipe.getMass(), 6, "eV/c^2")), 
            0, yOffset, Color.DARK_GRAY.getRGB(), false);
        yOffset += lineHeight;
        
        // Charge
        widgets.addText(__("gui.nuclearcraft.jei.particle.charge", df.format(recipe.getCharge())), 
            0, yOffset, Color.DARK_GRAY.getRGB(), false);
        yOffset += lineHeight;
        
        // Spin
        widgets.addText(__("gui.nuclearcraft.jei.particle.spin", recipe.getSpin()), 
            0, yOffset, Color.DARK_GRAY.getRGB(), false);
        yOffset += lineHeight;
        
/*        // Strong interaction
        widgets.addText(__("gui.nuclearcraft.jei.particle.colour", recipe.interactsWithStrong()), 
            0, yOffset, Color.DARK_GRAY.getRGB(), false);
        yOffset += lineHeight;
        
        // Weak interaction
        widgets.addText(__("gui.nuclearcraft.jei.particle.weak", recipe.interactsWithWeak()), 
            0, yOffset, Color.DARK_GRAY.getRGB(), false);
        yOffset += lineHeight + 5;*/
        
        // Description - wrapped text
        String description = __("nuclearcraft.particle." + recipe.output.getParticle().getName() + ".desc").getString();
        List<String> wrappedLines = wrapText(description, 22); // Approximate character limit per line
        for (String line : wrappedLines) {
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
        return Component.translatable("jei.category.nuclearcraft.particle_info");
    }
}