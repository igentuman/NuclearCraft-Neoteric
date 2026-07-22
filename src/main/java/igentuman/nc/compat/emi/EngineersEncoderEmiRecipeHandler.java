package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.widget.Widget;
import igentuman.nc.container.EngineersEncoderContainer;
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
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context) {
        return resolve(recipe) != null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context) {
        if (resolve(recipe) == null) return false;
        PacketDistributor.sendToServer(new PacketEncoderFillGrid(context.getScreenHandler().getBlockPos(), recipe.getId()));
        return true;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context,
                       List<Widget> widgets, GuiGraphics graphics) {
        // Ghost template: no items are required, so skip the missing-ingredient overlay.
    }

    private static CraftingRecipe resolve(EmiRecipe recipe) {
        ResourceLocation id = recipe.getId();
        Minecraft mc = Minecraft.getInstance();
        if (id == null || mc.level == null) return null;
        return mc.level.getRecipeManager().byKey(id)
                .map(RecipeHolder::value)
                .filter(r -> r instanceof CraftingRecipe)
                .map(r -> (CraftingRecipe) r)
                .orElse(null);
    }
}
