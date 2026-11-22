package igentuman.nc.handler.crafter;

import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/** Server-resolved encoder presentation, independent of client recipe availability. */
public record EncoderRecipePreview(String processor, List<FluidStack> fluidInputs,
                                   List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
    public static final StreamCodec<RegistryFriendlyByteBuf, EncoderRecipePreview> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EncoderRecipePreview::processor,
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), EncoderRecipePreview::fluidInputs,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), EncoderRecipePreview::itemOutputs,
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), EncoderRecipePreview::fluidOutputs,
            EncoderRecipePreview::new);

    public EncoderRecipePreview {
        fluidInputs = fluidInputs.stream().map(FluidStack::copy).toList();
        itemOutputs = itemOutputs.stream().map(ItemStack::copy).toList();
        fluidOutputs = fluidOutputs.stream().map(FluidStack::copy).toList();
    }

    public static EncoderRecipePreview of(UniversalProcessorRecipe recipe) {
        ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(recipe);
        return new EncoderRecipePreview(recipe.getProcessorName(), stacks.primaryFluidInputs(), stacks.itemOutputs(), stacks.fluidOutputs());
    }
}
