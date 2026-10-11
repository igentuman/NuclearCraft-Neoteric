package igentuman.nc.compat.ae2;

import appeng.menu.me.items.PatternEncodingTermMenu;
import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.network.PacketAE2PatternTransfer;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** JEI transfer handler that encodes a processor recipe into an AE2 pattern encoding terminal. */
public class JEI2PatternEncoderTransfer implements IRecipeTransferHandler<PatternEncodingTermMenu, UniversalProcessorRecipe> {

    private final RecipeType<UniversalProcessorRecipe> recipeType;

    public JEI2PatternEncoderTransfer(RecipeType<UniversalProcessorRecipe> recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public Class<PatternEncodingTermMenu> getContainerClass() {
        return PatternEncodingTermMenu.class;
    }

    @Override
    public Optional<MenuType<PatternEncodingTermMenu>> getMenuType() {
        return Optional.of(PatternEncodingTermMenu.TYPE);
    }

    @Override
    public RecipeType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(PatternEncodingTermMenu container, UniversalProcessorRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }

        ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(recipe);
        PacketDistributor.sendToServer(new PacketAE2PatternTransfer(
                stacks.primaryItemInputs(), stacks.primaryFluidInputs(), stacks.itemOutputs(), stacks.fluidOutputs()));
        return null;
    }
}
