package igentuman.nc.compat.jei;

import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.network.PacketCrafterFillGrid;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static igentuman.nc.setup.entries.Crafter.ENGINEERS_CRAFTING_TABLE_MENU;

public class EngineersCrafterRecipeTransferHandler implements IRecipeTransferHandler<EngineersCrafterContainer, RecipeHolder<CraftingRecipe>> {

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
        return Optional.of(ENGINEERS_CRAFTING_TABLE_MENU.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersCrafterContainer container, RecipeHolder<CraftingRecipe> recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (!container.canAssemble(recipe.value())) {
            return helper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.transfer.no.ingredients"));
        }
        if (doTransfer) {
            PacketDistributor.sendToServer(new PacketCrafterFillGrid(container.getBlockPos(), recipe.id()));
        }
        return null;
    }
}
