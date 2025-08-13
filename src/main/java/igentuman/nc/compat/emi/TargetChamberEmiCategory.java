package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static net.minecraft.world.item.Items.BARRIER;

public class TargetChamberEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("target_chamber"),
            EmiStack.of(CATALYSTS.containsKey("target_chamber") ? 
                new ItemStack(TARGET_CHAMBER_BLOCKS.get("target_chamber_controller").get()) :
                new ItemStack(BARRIER))
    );

    private final TargetChamberControllerBE.Recipe recipe;

    public TargetChamberEmiCategory(TargetChamberControllerBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 98, 30);
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
            widgets.addSlot(inputs.get(i), 11 + 18 * i, 7);
        }
        
        // Add progress arrow
        widgets.addFillingArrow(29, 8, (int) (recipe.getTimeModifier() * 2000));
        
        // Add output slots
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), 74 + 18 * i, 7).recipeContext(this);
        }
    }
}