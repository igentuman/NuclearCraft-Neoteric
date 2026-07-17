package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.recipes.type.OreVeinRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class OreVeinEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("nc_ore_veins"),
            EmiStack.of(new ItemStack(Items.DIAMOND_PICKAXE))
    );

    private final OreVeinRecipe recipe;

    public OreVeinEmiCategory(OreVeinRecipe recipe) {
        super(CATEGORY, recipe.getId(), 142, 40);
        this.recipe = recipe;
        
        // Add inputs (ore vein ingredients)
        for (int i = 0; i < recipe.getItemIngredients().size(); i++) {
            ItemStack[] items = recipe.getItemIngredients().get(i).getItems();
            this.inputs.add(EmiIngredient.of(Arrays.stream(items).map(EmiStack::of).toList()));
        }
        
        // Add output (result item)
        this.outputs.add(EmiStack.of(recipe.getResultItem()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add input slots
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 5 + 18 * i, 7);
        }
        
        // Add output slot
        widgets.addSlot(outputs.get(0), 142 - 26, 7).recipeContext(this);
        
        // Add tooltip with ore vein information
        widgets.addTooltipText(List.of(
            net.minecraft.network.chat.Component.translatable("ore_vein.rarity_modifier", recipe.rarityModifier)
                .withStyle(net.minecraft.ChatFormatting.YELLOW)
        ), 5, 5, 132, 30);
    }
}