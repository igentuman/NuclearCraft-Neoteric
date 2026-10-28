package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.widget.Widget;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.network.PacketEncoderFillGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class EngineersEncoderEmiRecipeHandler implements StandardRecipeHandler<EngineersEncoderContainer> {

    @Override
    public List<Slot> getInputSources(EngineersEncoderContainer handler) {
        return List.of();
    }

    @Override
    public List<Slot> getCraftingSlots(EngineersEncoderContainer handler) {
        return handler.emiGhostMatrixSlots();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING || recipe instanceof ProcessorEmiRecipe;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context) {
        return resolveId(recipe) != null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context) {
        ResourceLocation id = resolveId(recipe);
        if (id == null) return false;
        PacketDistributor.sendToServer(new PacketEncoderFillGrid(context.getScreenHandler().getBlockPos(), id));
        return true;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context,
                       List<Widget> widgets, GuiGraphics graphics) {
        // Ghost template: no items are required, so skip the missing-ingredient overlay.
    }

    private static ResourceLocation resolveId(EmiRecipe recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        if (recipe instanceof ProcessorEmiRecipe per) {
            return ProcessorRecipes.idOf(mc.level.getRecipeManager(), per.recipe());
        }
        ResourceLocation id = recipe.getId();
        if (id == null) return null;
        return mc.level.getRecipeManager().byKey(id)
                .map(RecipeHolder::value)
                .filter(r -> r instanceof CraftingRecipe)
                .map(r -> id)
                .orElse(null);
    }
}
