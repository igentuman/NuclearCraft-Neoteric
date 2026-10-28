package igentuman.nc.handler.crafter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public record CraftingPatternData(List<ItemStack> inputs,
                                  ItemStack output,
                                  String processor,
                                  Optional<ResourceLocation> recipeId,
                                  List<FluidStack> fluidInputs,
                                  List<ItemStack> extraOutputs,
                                  List<FluidStack> fluidOutputs) {

    public CraftingPatternData(List<ItemStack> inputs, ItemStack output) {
        this(inputs, output, "", Optional.empty(), List.of(), List.of(), List.of());
    }

    public boolean isProcessing() {
        return processor != null && !processor.isEmpty();
    }

    public static final Codec<CraftingPatternData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inputs").forGetter(CraftingPatternData::inputs),
            ItemStack.OPTIONAL_CODEC.fieldOf("output").forGetter(CraftingPatternData::output),
            Codec.STRING.optionalFieldOf("processor", "").forGetter(CraftingPatternData::processor),
            ResourceLocation.CODEC.optionalFieldOf("recipe").forGetter(CraftingPatternData::recipeId),
            FluidStack.OPTIONAL_CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(CraftingPatternData::fluidInputs),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("extra_outputs", List.of()).forGetter(CraftingPatternData::extraOutputs),
            FluidStack.OPTIONAL_CODEC.listOf().optionalFieldOf("fluid_outputs", List.of()).forGetter(CraftingPatternData::fluidOutputs)
    ).apply(inst, CraftingPatternData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingPatternData> STREAM_CODEC =
            StreamCodec.of(CraftingPatternData::write, CraftingPatternData::read);

    private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> ITEMS =
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUIDS =
            FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list());

    private static void write(RegistryFriendlyByteBuf buf, CraftingPatternData d) {
        ITEMS.encode(buf, d.inputs);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, d.output);
        buf.writeUtf(d.processor == null ? "" : d.processor);
        buf.writeBoolean(d.recipeId.isPresent());
        d.recipeId.ifPresent(buf::writeResourceLocation);
        FLUIDS.encode(buf, d.fluidInputs);
        ITEMS.encode(buf, d.extraOutputs);
        FLUIDS.encode(buf, d.fluidOutputs);
    }

    private static CraftingPatternData read(RegistryFriendlyByteBuf buf) {
        List<ItemStack> inputs = ITEMS.decode(buf);
        ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        String processor = buf.readUtf();
        Optional<ResourceLocation> recipe = buf.readBoolean() ? Optional.of(buf.readResourceLocation()) : Optional.empty();
        List<FluidStack> fluidInputs = FLUIDS.decode(buf);
        List<ItemStack> extraOutputs = ITEMS.decode(buf);
        List<FluidStack> fluidOutputs = FLUIDS.decode(buf);
        return new CraftingPatternData(inputs, output, processor, recipe, fluidInputs, extraOutputs, fluidOutputs);
    }
}
