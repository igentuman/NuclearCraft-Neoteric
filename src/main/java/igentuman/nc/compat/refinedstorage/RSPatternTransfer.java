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
import java.util.stream.Collectors;

/** Converts processor recipe stacks to Refined Storage's processing resources. */
public final class RSPatternTransfer {

    // Refined Storage 3.2.1 uses the same capacity for both processing matrices.
    private static final int PROCESSING_MATRIX_SIZE = 81;

    private RSPatternTransfer() {
    }

    /** Whether the recipe has inputs and outputs and fits both processing matrices. */
    public static boolean fits(UniversalProcessorRecipe recipe) {
        int inputs = recipe.getItemInputs().size() + recipe.getFluidInputs().size();
        int outputs = recipe.getItemOutputs().size() + recipe.getFluidOutputs().size();
        return inputs > 0 && inputs <= PROCESSING_MATRIX_SIZE && outputs > 0 && outputs <= PROCESSING_MATRIX_SIZE;
    }

    public static void transfer(PatternGridContainerMenu menu, UniversalProcessorRecipe recipe) {
        transfer(menu, ProcessorRecipeStacks.of(recipe));
    }

    public static void transfer(PatternGridContainerMenu menu, ProcessorRecipeStacks stacks) {
        // The Pattern Grid sorts each slot's alternatives in place on the server.
        List<List<ResourceAmount>> inputs = new ArrayList<>();
        stacks.itemInputs().forEach(options -> inputs.add(options.stream()
                .map(RSPatternTransfer::item).collect(Collectors.toCollection(ArrayList::new))));
        stacks.fluidInputs().forEach(options -> inputs.add(options.stream()
                .map(RSPatternTransfer::fluid).collect(Collectors.toCollection(ArrayList::new))));
        List<List<ResourceAmount>> outputs = new ArrayList<>();
        stacks.itemOutputs().forEach(stack -> outputs.add(new ArrayList<>(List.of(item(stack)))));
        stacks.fluidOutputs().forEach(stack -> outputs.add(new ArrayList<>(List.of(fluid(stack)))));
        menu.transferProcessingRecipe(inputs, outputs);
    }

    private static ResourceAmount item(ItemStack stack) {
        return new ResourceAmount(ItemResource.ofItemStack(stack), stack.getCount());
    }

    private static ResourceAmount fluid(FluidStack stack) {
        return new ResourceAmount(new FluidResource(stack.getFluid(), stack.getComponentsPatch()), stack.getAmount());
    }
}
