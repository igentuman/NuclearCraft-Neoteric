package igentuman.nc.compat.refinedstorage;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.content.Menus;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.compat.emi.ProcessorEmiRecipe;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;

/** Sends NuclearCraft's processing recipes from EMI through Refined Storage's Pattern Grid transfer. */
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
        return recipe instanceof ProcessorEmiRecipe processorRecipe && RSPatternTransfer.fits(processorRecipe.recipe());
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<PatternGridContainerMenu> context) {
        if (!(recipe instanceof ProcessorEmiRecipe processorRecipe)) return false;
        UniversalProcessorRecipe value = processorRecipe.recipe();
        Minecraft mc = Minecraft.getInstance();
        if (!RSPatternTransfer.fits(value) || mc.level == null) return false;
        ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(value, mc.level.registryAccess());
        if (!stacks.resolvesAllSlots(value)) return false;
        RSPatternTransfer.transfer(context.getScreenHandler(), stacks);
        return true;
    }
}
