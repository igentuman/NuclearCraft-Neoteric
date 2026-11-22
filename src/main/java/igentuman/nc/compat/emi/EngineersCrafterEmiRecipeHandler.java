package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.network.PacketCrafterFillGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import igentuman.nc.recipe.RecipeSources;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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
        RecipeHolder<CraftingRecipe> holder = craftingHolder(recipe);
        return holder != null && context.getScreenHandler().canAssemble(holder.value());
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<EngineersCrafterContainer> context) {
        RecipeHolder<CraftingRecipe> holder = craftingHolder(recipe);
        if (holder == null || !context.getScreenHandler().canAssemble(holder.value())) return false;
        ClientPacketDistributor.sendToServer(new PacketCrafterFillGrid(context.getScreenHandler().getBlockPos(), holder.id().identifier()));
        return true;
    }

    /** The backing holder when EMI supplies one, otherwise the synchronized client recipe with the same ID. */
    static RecipeHolder<CraftingRecipe> craftingHolder(EmiRecipe recipe) {
        RecipeHolder<?> holder = recipe.getBackingRecipe();
        Minecraft mc = Minecraft.getInstance();
        if (holder == null && recipe.getId() != null && mc.level != null) {
            holder = RecipeSources.byId(mc.level, recipe.getId()).orElse(null);
        }
        if (holder == null || !(holder.value() instanceof CraftingRecipe)) return null;
        @SuppressWarnings("unchecked")
        RecipeHolder<CraftingRecipe> crafting = (RecipeHolder<CraftingRecipe>) holder;
        return crafting;
    }
}
