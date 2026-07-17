package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static net.minecraft.world.item.Items.BARRIER;

public class AcceleratorCoolantEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("accelerator_coolant"),
            EmiStack.of(CATALYSTS.containsKey("accelerator_coolant") ? 
                new ItemStack(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get()) : 
                new ItemStack(BARRIER))
    );

    private final LinearAcceleratorControllerBE.CoolantRecipe recipe;

    public AcceleratorCoolantEmiCategory(LinearAcceleratorControllerBE.CoolantRecipe recipe) {
        super(CATEGORY, recipe.getId(), 136, 45);
        this.recipe = recipe;
        
        // Add fluid inputs
        for (int i = 0; i < recipe.getInputFluids().length && i < 2; i++) {
            this.inputs.add(EmiIngredient.of(recipe.getInputFluids(i).stream()
                    .map(fluid -> EmiStack.of(fluid.getFluid(), fluid.getAmount())).toList()));
        }
        
        // Add fluid outputs
        for (int i = 0; i < recipe.getOutputFluids().size() && i < 4; i++) {
            FluidStack fluid = recipe.getOutputFluids().get(i);
            this.outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add input fluid slots
        for (int i = 0; i < 2 && i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 12 + 10 * i, 6);
        }

        // Add progress arrow
        widgets.addFillingArrow(34, 6, (int) (recipe.getTimeModifier()+1) * 200);

        // Add output fluid slots
        for (int i = 0; i < 4 && i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), 75 + 10 * i, 6).recipeContext(this);
        }
    }
}