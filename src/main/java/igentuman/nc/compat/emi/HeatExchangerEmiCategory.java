package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_BLOCKS;
import static net.minecraft.world.item.Items.BARRIER;

public class HeatExchangerEmiCategory extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("heat_exchanger_controller"),
            EmiStack.of(CATALYSTS.containsKey("heat_exchanger_controller") ?
                new ItemStack(HX_BLOCKS.get("heat_exchanger_controller").get()) :
                new ItemStack(BARRIER))
    );

    private final HeatExchangerControllerBE.Recipe recipe;

    public HeatExchangerEmiCategory(HeatExchangerControllerBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 136, 45);
        this.recipe = recipe;

        for (int i = 0; i < recipe.getInputFluids().length && i < 2; i++) {
            this.inputs.add(EmiIngredient.of(recipe.getInputFluids(i).stream()
                    .map(fluid -> EmiStack.of(fluid.getFluid(), fluid.getAmount())).toList()));
        }

        for (int i = 0; i < recipe.getOutputFluids().size() && i < 4; i++) {
            FluidStack fluid = recipe.getOutputFluids().get(i);
            this.outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < 2 && i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 12 + 10 * i, 6);
        }

        widgets.addFillingArrow(34, 16, (int) recipe.getTimeModifier() * 10);

        for (int i = 0; i < 4 && i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), 75 + 10 * i, 6).recipeContext(this);
        }

        java.util.List<net.minecraft.network.chat.Component> tip = new java.util.ArrayList<>();
        tip.add(net.minecraft.network.chat.Component.translatable("heat_exchanger_controller.recipe.duration",
                (int) recipe.getTimeModifier()).withStyle(net.minecraft.ChatFormatting.AQUA));
        double h = recipe.getHeat();
        if (h > 0) {
            tip.add(net.minecraft.network.chat.Component.translatable("heat_exchanger_controller.recipe.heat_add",
                    (int) h).withStyle(net.minecraft.ChatFormatting.RED));
        } else if (h < 0) {
            tip.add(net.minecraft.network.chat.Component.translatable("heat_exchanger_controller.recipe.heat_remove",
                    (int) Math.abs(h)).withStyle(net.minecraft.ChatFormatting.AQUA));
        }
        widgets.addTooltipText(tip, 34, 16, 42, 16);
    }
}
