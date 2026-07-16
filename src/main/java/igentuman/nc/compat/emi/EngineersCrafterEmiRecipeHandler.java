package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.network.toServer.PacketCrafterFillGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

public class EngineersCrafterEmiRecipeHandler implements StandardRecipeHandler<EngineersCrafterContainer> {

    @Override
    public List<Slot> getInputSources(EngineersCrafterContainer handler) {
        return handler.emiInputSlots();
    }

    @Override
    public List<Slot> getCraftingSlots(EngineersCrafterContainer handler) {
        return handler.emiCraftMatrixSlots();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<EngineersCrafterContainer> screen) {
        List<EmiStack> stacks = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Inventory inv = mc.player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty()) stacks.add(EmiStack.of(s, s.getCount()));
            }
        }
        AggregatedInventory agg = new AggregatedInventory(screen.getMenu().blockEntity.containerSlots);
        for (AggregatedInventory.Entry e : agg.entries()) {
            stacks.add(EmiStack.of(e.stack(), e.count()));
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<EngineersCrafterContainer> context) {
        CraftingRecipe craftingRecipe = resolve(recipe);
        return craftingRecipe != null && context.getScreenHandler().canAssemble(craftingRecipe);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<EngineersCrafterContainer> context) {
        CraftingRecipe craftingRecipe = resolve(recipe);
        if (craftingRecipe == null || !context.getScreenHandler().canAssemble(craftingRecipe)) return false;
        NuclearCraft.packetHandler().sendToServer(
                new PacketCrafterFillGrid(context.getScreenHandler().getBlockPos(), recipe.getId()));
        return true;
    }

    private static CraftingRecipe resolve(EmiRecipe recipe) {
        ResourceLocation id = recipe.getId();
        Minecraft mc = Minecraft.getInstance();
        if (id == null || mc.level == null) return null;
        Recipe<?> r = mc.level.getRecipeManager().byKey(id).orElse(null);
        return r instanceof CraftingRecipe craftingRecipe ? craftingRecipe : null;
    }
}
