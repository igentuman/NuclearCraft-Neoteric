package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.network.toServer.PacketRecipeTransfer;
import igentuman.nc.recipes.type.NcRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ProcessorRecipeTransferHandler<T extends NcRecipe> implements IRecipeTransferHandler<NCProcessorContainer<?>, T> {
    
    private final Class<NCProcessorContainer<?>> containerClass;
    private final RecipeType<T> recipeType;
    
    @SuppressWarnings("unchecked")
    public ProcessorRecipeTransferHandler(RecipeType<T> recipeType) {
        this.containerClass = (Class<NCProcessorContainer<?>>) (Class<?>) NCProcessorContainer.class;
        this.recipeType = recipeType;
    }
    
    @Override
    public Class<NCProcessorContainer<?>> getContainerClass() {
        return containerClass;
    }
    
    @Override
    public Optional<MenuType<NCProcessorContainer<?>>> getMenuType() {
        return Optional.empty();
    }
    
    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }
    
    @Override
    public @Nullable IRecipeTransferError transferRecipe(NCProcessorContainer<?> container, T recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            // Check if the player has the required ingredients
            return checkIngredients(container, recipe, recipeSlots, player);
        }
        
        // Perform the actual transfer
        return performTransfer(container, recipe, recipeSlots, player, maxTransfer);
    }
    
    private @Nullable IRecipeTransferError checkIngredients(NCProcessorContainer<?> container, T recipe, IRecipeSlotsView recipeSlots, Player player) {
        // Get all input ingredients from the recipe
        List<List<ItemStack>> inputs = recipeSlots.getSlotViews(mezz.jei.api.recipe.RecipeIngredientRole.INPUT)
                .stream()
                .map(slotView -> slotView.getIngredients(VanillaTypes.ITEM_STACK).toList())
                .toList();
        
        // Check if player has required items in inventory
        for (List<ItemStack> ingredientOptions : inputs) {
            if (ingredientOptions.isEmpty()) continue;
            
            boolean hasIngredient = false;
            for (ItemStack option : ingredientOptions) {
                if (hasItemInInventory(player, option)) {
                    hasIngredient = true;
                    break;
                }
            }
            
            if (!hasIngredient) {
                // Return an error indicating missing ingredients
                return new ProcessorRecipeTransferError("Missing ingredients in inventory");
            }
        }
        
        return null; // No error, transfer is possible
    }
    
    private @Nullable IRecipeTransferError performTransfer(NCProcessorContainer<?> container, T recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer) {
        // Send packet to server to handle the transfer
        NuclearCraft.packetHandler().sendToServer(new PacketRecipeTransfer(container.getPosition(), recipe.getId()));
        
        return null; // Success - actual transfer will be handled on server
    }
    
    private boolean hasItemInInventory(Player player, ItemStack required) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required) && stack.getCount() >= required.getCount()) {
                return true;
            }
        }
        return false;
    }
    
    // Custom error class for recipe transfer errors
    private static class ProcessorRecipeTransferError implements IRecipeTransferError {
        private final String message;
        
        public ProcessorRecipeTransferError(String message) {
            this.message = message;
        }
        
        @Override
        public Type getType() {
            return Type.USER_FACING;
        }
    }
}