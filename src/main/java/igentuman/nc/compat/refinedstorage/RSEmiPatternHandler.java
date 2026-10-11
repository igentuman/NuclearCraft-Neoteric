package igentuman.nc.compat.refinedstorage;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.content.Menus;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import igentuman.nc.compat.emi.ProcessorEmiRecipe;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;

public class RSEmiPatternHandler implements EmiRecipeHandler<PatternGridContainerMenu> {

    public static void register(EmiRegistry registry) {
        registry.addRecipeHandler(Menus.INSTANCE.getPatternGrid(), new RSEmiPatternHandler());
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<PatternGridContainerMenu> screen) {
        return new EmiPlayerInventory(List.of());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof ProcessorEmiRecipe;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<PatternGridContainerMenu> context) {
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<PatternGridContainerMenu> context) {
        if (!(recipe instanceof ProcessorEmiRecipe processorRecipe)) return false;
        RSPatternTransfer.transfer(context.getScreenHandler(), processorRecipe.recipe());
        return true;
    }
}
