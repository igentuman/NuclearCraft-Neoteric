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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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
        Identifier id = resolveId(recipe);
        if (id == null) return false;
        ClientPacketDistributor.sendToServer(new PacketEncoderFillGrid(context.getScreenHandler().getBlockPos(), id));
        return true;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<EngineersEncoderContainer> context,
                       List<Widget> widgets, GuiGraphicsExtractor graphics) {
        // Ghost template: no items are required, so skip the missing-ingredient overlay.
    }

    private static Identifier resolveId(EmiRecipe recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        if (recipe instanceof ProcessorEmiRecipe per) {
            Identifier id = per.holder().id().identifier();
            return ProcessorRecipes.byId(mc.level, id) == null ? null : id;
        }
        RecipeHolder<CraftingRecipe> holder = EngineersCrafterEmiRecipeHandler.craftingHolder(recipe);
        return holder == null ? null : holder.id().identifier();
    }
}
