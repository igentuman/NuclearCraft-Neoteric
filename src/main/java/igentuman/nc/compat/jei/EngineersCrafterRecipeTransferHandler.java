package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.network.toServer.PacketCrafterFillGrid;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_CONTAINER;

public class EngineersCrafterRecipeTransferHandler implements IRecipeTransferHandler<EngineersCrafterContainer, CraftingRecipe> {

    private final IRecipeTransferHandlerHelper helper;

    public EngineersCrafterRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<EngineersCrafterContainer> getContainerClass() {
        return EngineersCrafterContainer.class;
    }

    @Override
    public Optional<MenuType<EngineersCrafterContainer>> getMenuType() {
        return Optional.of(ENGINEERS_CRAFTING_TABLE_CONTAINER.get());
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersCrafterContainer container, CraftingRecipe recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (!container.canAssemble(recipe)) {
            return helper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.transfer.no.ingredients"));
        }
        if (doTransfer) {
            NuclearCraft.packetHandler().sendToServer(
                    new PacketCrafterFillGrid(container.getBlockPos(), recipe.getId()));
        }
        return null;
    }
}
