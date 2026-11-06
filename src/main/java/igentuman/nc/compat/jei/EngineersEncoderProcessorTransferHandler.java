package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.network.toServer.PacketEncoderFillGrid;
import igentuman.nc.recipes.type.NcRecipe;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_ENCODER_CONTAINER;

public class EngineersEncoderProcessorTransferHandler<T extends NcRecipe> implements IRecipeTransferHandler<EngineersEncoderContainer, T> {

    private final RecipeType<T> recipeType;
    private final IRecipeTransferHandlerHelper helper;

    public EngineersEncoderProcessorTransferHandler(RecipeType<T> recipeType, IRecipeTransferHandlerHelper helper) {
        this.recipeType = recipeType;
        this.helper = helper;
    }

    @Override
    public Class<EngineersEncoderContainer> getContainerClass() {
        return EngineersEncoderContainer.class;
    }

    @Override
    public Optional<MenuType<EngineersEncoderContainer>> getMenuType() {
        return Optional.of(ENGINEERS_ENCODER_CONTAINER.get());
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersEncoderContainer container, T recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        ResourceLocation id = ProcessorRecipes.idOf(player.level().getRecipeManager(), recipe);
        if (id == null) {
            return helper.createUserErrorWithTooltip(Component.translatable("gui.nc.crafter.no_recipe"));
        }
        if (doTransfer) {
            NuclearCraft.packetHandler().sendToServer(new PacketEncoderFillGrid(container.getBlockPos(), id));
        }
        return null;
    }
}
