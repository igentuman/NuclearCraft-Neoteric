package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import igentuman.nc.container.NCProcessorContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class ProcessorEmiRecipeHandler<T extends AbstractContainerMenu> implements StandardRecipeHandler<T> {


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
        return StandardRecipeHandler.super.craft(recipe, context);
    }

}