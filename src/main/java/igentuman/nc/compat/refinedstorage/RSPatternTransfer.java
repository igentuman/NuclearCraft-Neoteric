package igentuman.nc.compat.refinedstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class RSPatternTransfer {

    private RSPatternTransfer() {
    }

    public static void transfer(PatternGridContainerMenu menu, UniversalProcessorRecipe recipe) {
        ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(recipe);
        List<List<ResourceAmount>> inputs = new ArrayList<>();
        stacks.itemInputs().forEach(options -> inputs.add(options.stream().map(RSPatternTransfer::item).toList()));
        stacks.fluidInputs().forEach(options -> inputs.add(options.stream().map(RSPatternTransfer::fluid).toList()));
        List<List<ResourceAmount>> outputs = new ArrayList<>();
        stacks.itemOutputs().forEach(stack -> outputs.add(List.of(item(stack))));
        stacks.fluidOutputs().forEach(stack -> outputs.add(List.of(fluid(stack))));
        menu.transferProcessingRecipe(inputs, outputs);
    }

    private static ResourceAmount item(ItemStack stack) {
        return new ResourceAmount(ItemResource.ofItemStack(stack), stack.getCount());
    }

    private static ResourceAmount fluid(FluidStack stack) {
        return new ResourceAmount(new FluidResource(stack.getFluid(), stack.getComponentsPatch()), stack.getAmount());
    }
}
