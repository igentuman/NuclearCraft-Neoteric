package igentuman.nc.compat.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

/**
 * JEI transfer handler on the context entry point. JEI announces every performed transfer to its listeners
 * before calling the handler, so a performed transfer always reports its result back through the context.
 */
public interface ContextRecipeTransferHandler<C extends AbstractContainerMenu, R> extends IRecipeTransferHandler<C, R> {

    /** Checks the transfer and, when {@code doTransfer} is set, performs it. Returns {@code null} on success. */
    @Nullable IRecipeTransferError transfer(C container, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean doTransfer);

    @Override
    default @Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<R, C> context, boolean doTransfer) {
        IRecipeTransferError error = transfer(context.getContainer(), context.getRecipe(), context.getRecipeSlots(),
                context.getPlayer(), doTransfer);
        if (doTransfer) {
            context.completeRecipeTransfer(error == null ? RecipeTransferResult.SUCCESS : RecipeTransferResult.REJECTED);
        }
        return error;
    }

    /** JEI still declares this legacy entry point abstract; delete this override once JEI removes it. */
    @Override
    @SuppressWarnings("removal")
    default @Nullable IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlots, Player player,
                                                          boolean maxTransfer, boolean doTransfer) {
        return transfer(container, recipe, recipeSlots, player, doTransfer);
    }
}
