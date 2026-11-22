package igentuman.nc.compat.jei;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.network.PacketEncoderFillGrid;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static igentuman.nc.setup.entries.Crafter.ENGINEERS_ENCODER_MENU;

public class EngineersEncoderProcessorTransferHandler implements ContextRecipeTransferHandler<EngineersEncoderContainer, RecipeHolder<UniversalProcessorRecipe>> {

    private final IRecipeHolderType<UniversalProcessorRecipe> recipeType;
    private final IRecipeTransferHandlerHelper helper;

    public EngineersEncoderProcessorTransferHandler(IRecipeHolderType<UniversalProcessorRecipe> recipeType, IRecipeTransferHandlerHelper helper) {
        this.recipeType = recipeType;
        this.helper = helper;
    }

    @Override
    public Class<EngineersEncoderContainer> getContainerClass() {
        return EngineersEncoderContainer.class;
    }

    @Override
    public Optional<MenuType<EngineersEncoderContainer>> getMenuType() {
        return Optional.of(ENGINEERS_ENCODER_MENU.get());
    }

    @Override
    public IRecipeHolderType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transfer(EngineersEncoderContainer container, RecipeHolder<UniversalProcessorRecipe> recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean doTransfer) {
        Identifier id = recipe.id().identifier();
        if (ProcessorRecipes.byId(player.level(), id) == null) {
            return helper.createUserErrorWithTooltip(Component.translatable("screen.nuclearcraft.crafter.no_recipe"));
        }
        if (doTransfer) {
            ClientPacketDistributor.sendToServer(new PacketEncoderFillGrid(container.getBlockPos(), id));
        }
        return null;
    }
}
