package igentuman.nc.compat.refined_storage;

import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.network.grid.GridProcessingTransferMessage;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import igentuman.nc.compat.emi.ProcessorEmiCategory;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProcessorEmiRecipeHandlerRS<T extends AbstractContainerMenu> implements StandardRecipeHandler<T> {



    @Override
    public List<Slot> getInputSources(T handler) {
        List<Slot> inputs = new ArrayList<>();

        if(handler instanceof NCProcessorContainer<?> processorContainer) {
            // Add processor input slots
            int processorSlots = processorContainer.getProcessor().getSlotsConfig().slotsCount()+2;
            for (int i = processorSlots; i < processorSlots + 36 && i < handler.slots.size(); i++) {
                inputs.add(handler.getSlot(i));
            }
            return inputs;
        }

        for (int i = 0; i < handler.slots.size(); i++) {
            inputs.add(handler.getSlot(i));
        }

        return inputs;
    }

    @Override
    public List<Slot> getCraftingSlots(T handler) {
        List<Slot> craftingSlots = new ArrayList<>();
        if(handler instanceof NCProcessorContainer<?> processorContainer) {
            int processorSlots = processorContainer.getProcessor().getSlotsConfig().getInputItems();
            for (int i = 0; i < processorSlots && i < handler.slots.size(); i++) {
                craftingSlots.add(handler.getSlot(i));
            }
        }

        return craftingSlots;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof ProcessorEmiCategory;
    }

    @Override
    public boolean canCraft(EmiRecipe emiRecipe, EmiCraftContext<T> emiCraftContext) {
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        if(context.getScreen() instanceof GridScreen gridScreen && gridScreen.getGrid().getGridType() == GridType.PATTERN) {
            if(recipe instanceof ProcessorEmiCategory processorRecipe) {
                return transferProcessingRecipe(processorRecipe, context, gridScreen);
            }
        }
        return true;
    }

    private boolean transferProcessingRecipe(ProcessorEmiCategory processorRecipe, EmiCraftContext<T> context, GridScreen gridScreen) {
        // Get the underlying NC recipe
        var ncRecipe = processorRecipe.getRecipe();
        var inputs = ncRecipe.getInputItems();
        var outputs = ncRecipe.getResultItems();
        var fluidInputs = ncRecipe.getInputFluids();
        var fluidOutputs = ncRecipe.getOutputFluids();
        return sendProcessingTransferMessage(inputs, outputs, fluidInputs, fluidOutputs);
    }

    private boolean sendProcessingTransferMessage(ItemStackIngredient[] inputs, List<ItemStack> outputs, FluidStackIngredient[] fluidInputs, List<FluidStack> fluidOutputs) {
        try {
            Collection<ItemStack> inputItems = new ArrayList<>();
            for (ItemStackIngredient ingredient : inputs) {
                inputItems.add(ingredient.getRepresentations().get(0));
            }
            Collection<FluidStack> inputFluids = new ArrayList<>();
            for (FluidStackIngredient ingredient : fluidInputs) {
                inputFluids.add(ingredient.getRepresentations().get(0));
            }

            RS.NETWORK_HANDLER.sendToServer(new GridProcessingTransferMessage(
                    inputItems,
                    outputs,
                    inputFluids,
                    fluidOutputs
            ));

        } catch (Exception ignore) {
            return false;
        }
        return true;
    }

}