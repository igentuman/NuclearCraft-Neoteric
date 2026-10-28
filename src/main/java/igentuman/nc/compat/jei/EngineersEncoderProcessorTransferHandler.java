package igentuman.nc.compat.jei;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.network.PacketEncoderFillGrid;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static igentuman.nc.setup.entries.Crafter.ENGINEERS_ENCODER_MENU;

public class EngineersEncoderProcessorTransferHandler implements IRecipeTransferHandler<EngineersEncoderContainer, UniversalProcessorRecipe> {

    private final RecipeType<UniversalProcessorRecipe> recipeType;
    private final IRecipeTransferHandlerHelper helper;

    public EngineersEncoderProcessorTransferHandler(RecipeType<UniversalProcessorRecipe> recipeType, IRecipeTransferHandlerHelper helper) {
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
    public RecipeType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersEncoderContainer container, UniversalProcessorRecipe recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        ResourceLocation id = ProcessorRecipes.idOf(player.level().getRecipeManager(), recipe);
        if (id == null) {
            return helper.createUserErrorWithTooltip(Component.translatable("screen.nuclearcraft.crafter.no_recipe"));
        }
        if (doTransfer) {
            PacketDistributor.sendToServer(new PacketEncoderFillGrid(container.getBlockPos(), id));
        }
        return null;
    }
}
