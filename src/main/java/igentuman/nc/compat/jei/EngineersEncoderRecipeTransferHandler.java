package igentuman.nc.compat.jei;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.network.PacketEncoderFillGrid;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static igentuman.nc.setup.entries.Crafter.ENGINEERS_ENCODER_MENU;

public class EngineersEncoderRecipeTransferHandler implements IRecipeTransferHandler<EngineersEncoderContainer, RecipeHolder<CraftingRecipe>> {

    @Override
    public Class<EngineersEncoderContainer> getContainerClass() {
        return EngineersEncoderContainer.class;
    }

    @Override
    public Optional<MenuType<EngineersEncoderContainer>> getMenuType() {
        return Optional.of(ENGINEERS_ENCODER_MENU.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersEncoderContainer container, RecipeHolder<CraftingRecipe> recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            PacketDistributor.sendToServer(new PacketEncoderFillGrid(container.getBlockPos(), recipe.id()));
        }
        return null;
    }
}
