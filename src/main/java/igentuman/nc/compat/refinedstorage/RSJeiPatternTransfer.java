package igentuman.nc.compat.refinedstorage;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.content.Menus;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RSJeiPatternTransfer implements IRecipeTransferHandler<PatternGridContainerMenu, UniversalProcessorRecipe> {

    private final RecipeType<UniversalProcessorRecipe> recipeType;

    public RSJeiPatternTransfer(RecipeType<UniversalProcessorRecipe> recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public Class<PatternGridContainerMenu> getContainerClass() {
        return PatternGridContainerMenu.class;
    }

    @Override
    public Optional<MenuType<PatternGridContainerMenu>> getMenuType() {
        return Optional.of(Menus.INSTANCE.getPatternGrid());
    }

    @Override
    public RecipeType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(PatternGridContainerMenu container, UniversalProcessorRecipe recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) RSPatternTransfer.transfer(container, recipe);
        return null;
    }
}
