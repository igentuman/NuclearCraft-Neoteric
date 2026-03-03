package igentuman.nc.compat.ae2;

import appeng.menu.me.items.PatternEncodingTermMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.emi.ProcessorEmiCategory;
import igentuman.nc.network.toServer.PacketAE2PatternTransfer;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class ProcessorEmiRecipeHandlerAE2<T extends AbstractContainerMenu> implements StandardRecipeHandler<T> {

    @Override
    public List<Slot> getInputSources(T handler) {
        List<Slot> inputs = new ArrayList<>();

        if(handler instanceof PatternEncodingTermMenu patternEncodingTermMenu) {
            // Add processor input slots
            return List.of(patternEncodingTermMenu.getProcessingInputSlots());
        }

        for (int i = 0; i < handler.slots.size(); i++) {
            inputs.add(handler.getSlot(i));
        }

        return inputs;
    }

    @Override
    public List<Slot> getCraftingSlots(T handler) {
        List<Slot> craftingSlots = new ArrayList<>();
        if(handler instanceof PatternEncodingTermMenu patternEncodingTermMenu) {
            return  List.of(patternEncodingTermMenu.getProcessingInputSlots());
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
        if(!(recipe instanceof ProcessorEmiCategory processorRecipe)) {
            return false;
        }
        
        T handler = context.getScreenHandler();
        if(!(handler instanceof PatternEncodingTermMenu)) {
            return false;
        }

        try {
            NcRecipe ncRecipe = processorRecipe.getRecipe();
            
            // Prepare lists for the packet
            List<ItemStack> inputItems = new ArrayList<>();
            List<FluidStack> inputFluids = new ArrayList<>();
            List<ItemStack> outputItems = new ArrayList<>();
            List<FluidStack> outputFluids = new ArrayList<>();
            
            // Collect item inputs
            for (ItemStackIngredient ingredient : ncRecipe.getInputItems()) {
                List<ItemStack> representations = ingredient.getRepresentations();
                if (!representations.isEmpty()) {
                    ItemStack stack = representations.get(0);
                    if (!stack.isEmpty()) {
                        inputItems.add(stack.copy());
                    }
                }
            }
            
            // Collect fluid inputs
            for (FluidStackIngredient ingredient : ncRecipe.getInputFluids()) {
                List<FluidStack> representations = ingredient.getRepresentations();
                if (!representations.isEmpty()) {
                    FluidStack fluidStack = representations.get(0);
                    if (!fluidStack.isEmpty()) {
                        inputFluids.add(fluidStack.copy());
                    }
                }
            }
            
            // Collect item outputs
            for (ItemStack stack : ncRecipe.getResultItems()) {
                if (!stack.isEmpty()) {
                    outputItems.add(stack.copy());
                }
            }
            
            // Collect fluid outputs
            for (FluidStack fluidStack : ncRecipe.getOutputFluids()) {
                if (!fluidStack.isEmpty()) {
                    outputFluids.add(fluidStack.copy());
                }
            }
            
            // Send packet to server
            PacketAE2PatternTransfer packet = new PacketAE2PatternTransfer(
                inputItems, inputFluids, outputItems, outputFluids
            );
            NuclearCraft.packetHandler().sendToServer(packet);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
