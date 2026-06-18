package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipes.type.NuclearBlastRecipe;
import igentuman.nc.setup.registration.NCBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

import static igentuman.nc.NuclearCraft.rl;

public class NuclearBlastEmiCategory extends BasicEmiRecipe {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("nuclear_blast"),
            EmiStack.of(new ItemStack(NCBlocks.PU_239_BOMB_ITEM.get()))
    );

    private final NuclearBlastRecipe recipe;

    public NuclearBlastEmiCategory(NuclearBlastRecipe recipe) {
        super(CATEGORY, recipe.getId(), 120, 50);
        this.recipe = recipe;
        if (recipe.getInputItems().length > 0 && recipe.getInputItems()[0] != null) {
            List<EmiStack> representations = recipe.getInputItems()[0].getRepresentations().stream().map(EmiStack::of).collect(Collectors.toList());
            this.inputs.add(EmiIngredient.of(representations));
        }
        if (recipe.getOutputItems().length > 0 && recipe.getOutputItems()[0] != null) {
            List<EmiStack> representations = recipe.getOutputItems()[0].getRepresentations().stream().map(EmiStack::of).collect(Collectors.toList());
            if (!representations.isEmpty()) {
                this.outputs.add(representations.get(0));
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (!inputs.isEmpty()) {
            widgets.addSlot(inputs.get(0), 10, 15);
        }

        if (!outputs.isEmpty()) {
            widgets.addSlot(outputs.get(0), 90, 15).recipeContext(this);
        }

        widgets.addText(Component.literal("==>"), 48, 19, 0x404040, false);

        String chanceText = String.format("%.0f%%", recipe.getChance() * 100);
        var font = Minecraft.getInstance().font;
        int width = font.width(chanceText);
        widgets.addText(Component.literal(chanceText), 60 - width / 2, 5, 0x404040, false);
    }
}
