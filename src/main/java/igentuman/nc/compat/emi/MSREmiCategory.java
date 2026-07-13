package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static net.minecraft.world.item.Items.BARRIER;

public class MSREmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("msr_controller"),
            EmiStack.of(CATALYSTS.containsKey("msr_controller") ?
                new ItemStack(FISSION_BLOCKS.get("msr_controller").get()) :
                new ItemStack(BARRIER))
    );

    private final MSRControllerBE.Recipe recipe;

    public MSREmiCategory(MSRControllerBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 98, 22);
        this.recipe = recipe;
        
        // Add inputs
        for (int i = 0; i < recipe.getItemIngredients().size(); i++) {
            ItemStack[] items = recipe.getItemIngredients().get(i).getItems();
            this.inputs.add(EmiIngredient.of(Arrays.stream(items).map(EmiStack::of).toList()));
        }
        
        // Add outputs
        for (int i = 0; i < recipe.getResultItems().size(); i++) {
            this.outputs.add(EmiStack.of(recipe.getResultItems().get(i)));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add input slots
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 11, 5);
        }
        
        // Add progress arrow
        widgets.addFillingArrow(33, 5, (int) (recipe.getDepletionTime()));
        
        // Add output slots
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), 74 + 18 * i, 5).recipeContext(this);
        }
    }
}