package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.network.toServer.PacketEncoderFillGrid;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_ENCODER_CONTAINER;

public class EngineersEncoderRecipeTransferHandler implements IRecipeTransferHandler<EngineersEncoderContainer, CraftingRecipe> {

    @Override
    public Class<EngineersEncoderContainer> getContainerClass() {
        return EngineersEncoderContainer.class;
    }

    @Override
    public Optional<MenuType<EngineersEncoderContainer>> getMenuType() {
        return Optional.of(ENGINEERS_ENCODER_CONTAINER.get());
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersEncoderContainer container, CraftingRecipe recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            NuclearCraft.packetHandler().sendToServer(
                    new PacketEncoderFillGrid(container.getBlockPos(), recipe.getId()));
        }
        return null;
    }
}
