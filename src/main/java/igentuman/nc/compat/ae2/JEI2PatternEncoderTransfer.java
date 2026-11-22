package igentuman.nc.compat.ae2;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.crafting.pattern.AEProcessingPattern;
import igentuman.nc.network.PacketAE2PatternTransfer;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import igentuman.nc.compat.jei.ContextRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class JEI2PatternEncoderTransfer implements ContextRecipeTransferHandler<PatternEncodingTermMenu, RecipeHolder<UniversalProcessorRecipe>> {

    private final IRecipeHolderType<UniversalProcessorRecipe> recipeType;
    private final IRecipeTransferHandlerHelper transferHelper;

    public JEI2PatternEncoderTransfer(IRecipeHolderType<UniversalProcessorRecipe> recipeType,
                                      IRecipeTransferHandlerHelper transferHelper) {
        this.recipeType = recipeType;
        this.transferHelper = transferHelper;
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
    public IRecipeHolderType<UniversalProcessorRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transfer(PatternEncodingTermMenu container, RecipeHolder<UniversalProcessorRecipe> recipe, IRecipeSlotsView recipeSlots, Player player, boolean doTransfer) {
        UniversalProcessorRecipe value = recipe.value();
        int inputs = value.getItemInputs().size() + value.getFluidInputs().size();
        int outputs = value.getItemOutputs().size() + value.getFluidOutputs().size();
        if (inputs == 0 || inputs > AEProcessingPattern.MAX_INPUT_SLOTS
                || outputs == 0 || outputs > AEProcessingPattern.MAX_OUTPUT_SLOTS) {
            return transferHelper.createInternalError();
        }
        if (doTransfer) {
            ClientPacketDistributor.sendToServer(new PacketAE2PatternTransfer(container.containerId, recipe.id().identifier()));
        }
        return null;
    }
}
