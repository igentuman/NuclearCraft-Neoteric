package igentuman.nc.compat.refinedstorage;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.content.Menus;
import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import igentuman.nc.compat.jei.ContextRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Sends NuclearCraft's processing recipes through Refined Storage's Pattern Grid transfer. */
public class RSJeiPatternTransfer implements ContextRecipeTransferHandler<PatternGridContainerMenu, RecipeHolder<UniversalProcessorRecipe>> {

    private final IRecipeHolderType<UniversalProcessorRecipe> recipeType;
    private final IRecipeTransferHandlerHelper transferHelper;

    public RSJeiPatternTransfer(IRecipeHolderType<UniversalProcessorRecipe> recipeType,
                                IRecipeTransferHandlerHelper transferHelper) {
        this.recipeType = recipeType;
        this.transferHelper = transferHelper;
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
    public IRecipeHolderType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transfer(PatternGridContainerMenu container, RecipeHolder<UniversalProcessorRecipe> recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean doTransfer) {
        UniversalProcessorRecipe value = recipe.value();
        if (!RSPatternTransfer.fits(value)) {
            return transferHelper.createInternalError();
        }
        if (doTransfer) {
            ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(value, player.registryAccess());
            if (!stacks.resolvesAllSlots(value)) return transferHelper.createInternalError();
            RSPatternTransfer.transfer(container, stacks);
        }
        return null;
    }
}
