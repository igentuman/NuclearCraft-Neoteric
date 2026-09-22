package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.compat.jei.ParticleInfoRecipe;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.scaledFormat;

public class ParticleInfoEmiRecipe extends BasicEmiRecipe {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;

    private final ParticleInfoRecipe recipe;

    public ParticleInfoEmiRecipe(EmiRecipeCategory category, ParticleInfoRecipe recipe) {
        super(category, recipe.getId(), WIDTH, HEIGHT);
        this.recipe = recipe;
        ParticleEmiStack particleStack = ParticleEmiStack.of(recipe.getStack());
        this.inputs.add(particleStack);
        this.outputs.add(particleStack);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        DecimalFormat df = new DecimalFormat("#.##");
        ParticleDefinition definition = recipe.getDefinition();

        widgets.addSlot(inputs.get(0), 2, 2).recipeContext(this);
        widgets.addText(__(definition.translationKey()), 25, 2, 0xFFFFFFFF, false);

        int yOffset = 20;
        int lineHeight = 10;

        widgets.addText(__("gui.nuclearcraft.jei.particle.mass",
                scaledFormat(definition.massMeV()) + " MeV/c^2"), 0, yOffset, 0xFF404040, false);
        yOffset += lineHeight;

        widgets.addText(__("gui.nuclearcraft.jei.particle.charge", df.format(definition.charge())), 0, yOffset, 0xFF404040, false);
        yOffset += lineHeight;

        widgets.addText(__("gui.nuclearcraft.jei.particle.spin", df.format(definition.spin())), 0, yOffset, 0xFF404040, false);
        yOffset += lineHeight;

        widgets.addText(__("gui.nuclearcraft.jei.particle.colour", recipe.interactsWithStrong()), 0, yOffset, 0xFF404040, false);
        yOffset += lineHeight;

        widgets.addText(__("gui.nuclearcraft.jei.particle.weak", recipe.interactsWithWeak()), 0, yOffset, 0xFF404040, false);
        yOffset += lineHeight;

        String description = __("nuclearcraft.particle." + recipe.getParticleId().getPath() + ".desc").getString();
        for (String line : wrapText(description, 22)) {
            widgets.addText(Component.literal(line), 0, yOffset, 0xFF404040, false);
            yOffset += lineHeight;
        }
    }

    private static List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxCharsPerLine) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                while (word.length() > maxCharsPerLine) {
                    lines.add(word.substring(0, maxCharsPerLine));
                    word = word.substring(maxCharsPerLine);
                }
                if (!word.isEmpty()) currentLine.append(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) lines.add(currentLine.toString());
        return lines;
    }
}
