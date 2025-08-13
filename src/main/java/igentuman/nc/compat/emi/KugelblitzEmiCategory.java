package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.UNKNOWN_INGREDIENT;
import static igentuman.nc.util.StackUtils.resolveStackByModPriority;
import static net.minecraft.world.item.Items.BARRIER;

public class KugelblitzEmiCategory extends BasicEmiRecipe {
    public static final ResourceLocation TEXTURE = rl("textures/gui/fission/jei.png");
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("kugelblitz_chamber"),
            EmiStack.of(CATALYSTS.containsKey("kugelblitz_chamber") ? 
                new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get()) : 
                new ItemStack(BARRIER))
    );

    private final ChamberTerminalBE.Recipe recipe;

    public KugelblitzEmiCategory(ChamberTerminalBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 98, 30);
        this.recipe = recipe;
        
        // Add inputs
        for (int i = 0; i < recipe.getItemIngredients().size(); i++) {
            if (recipe.getResultItem().is(resolveStackByModPriority(recipe.getItemIngredients().get(i).getItems()).getItem())) {
                ItemStack[] unknownItems = NcIngredient.of(UNKNOWN_INGREDIENT.get()).getItems();
                this.inputs.add(EmiIngredient.of(java.util.Arrays.stream(unknownItems)
                        .map(EmiStack::of).toList()));
            } else {
                ItemStack[] items = recipe.getItemIngredients().get(i).getItems();
                this.inputs.add(EmiIngredient.of(java.util.Arrays.stream(items)
                        .map(EmiStack::of).toList()));
            }
        }
        
        // Add output
        this.outputs.add(EmiStack.of(recipe.getResultItem()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add input slots
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 11 + 18 * i, 7);
        }
        
        // Add progress arrow
        widgets.addFillingArrow(29, 8, (int) (recipe.getTimeModifier() * 2000));
        
        // Add output slot
        widgets.addSlot(outputs.get(0), 74, 7).recipeContext(this);
    }
}