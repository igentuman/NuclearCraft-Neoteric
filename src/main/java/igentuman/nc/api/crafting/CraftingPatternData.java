package igentuman.nc.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.setup.Registers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.List;
import java.util.Optional;

/** Immutable pattern contents; empty slots retain their positions in the saved grid. */
public record CraftingPatternData(List<Optional<ItemStackTemplate>> inputs,
                                  Optional<ItemStackTemplate> output,
                                  String processor,
                                  Optional<Identifier> recipeId,
                                  List<Optional<FluidStackTemplate>> fluidInputs,
                                  List<Optional<ItemStackTemplate>> extraOutputs,
                                  List<Optional<FluidStackTemplate>> fluidOutputs) {

    public CraftingPatternData {
        inputs = List.copyOf(inputs);
        processor = processor == null ? "" : processor;
        fluidInputs = List.copyOf(fluidInputs);
        extraOutputs = List.copyOf(extraOutputs);
        fluidOutputs = List.copyOf(fluidOutputs);
    }

    /** Returns the component for encoded contents of nuclearcraft:crafting_pattern items after registry initialization. */
    public static DataComponentType<CraftingPatternData> componentType() {
        return Registers.CRAFTING_PATTERN.get();
    }

    public static CraftingPatternData fromStacks(List<ItemStack> inputs, ItemStack output) {
        return fromStacks(inputs, output, "", Optional.empty(), List.of(), List.of(), List.of());
    }

    public static CraftingPatternData fromStacks(List<ItemStack> inputs, ItemStack output, String processor,
                                                 Optional<Identifier> recipeId, List<FluidStack> fluidInputs,
                                                 List<ItemStack> extraOutputs, List<FluidStack> fluidOutputs) {
        return new CraftingPatternData(inputs.stream().map(CraftingPatternData::itemTemplate).toList(),
                itemTemplate(output), processor, recipeId,
                fluidInputs.stream().map(CraftingPatternData::fluidTemplate).toList(),
                extraOutputs.stream().map(CraftingPatternData::itemTemplate).toList(),
                fluidOutputs.stream().map(CraftingPatternData::fluidTemplate).toList());
    }

    private static Optional<ItemStackTemplate> itemTemplate(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    private static Optional<FluidStackTemplate> fluidTemplate(FluidStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(FluidStackTemplate.fromNonEmptyStack(stack));
    }

    public ItemStack createOutput() {
        return output.map(CraftingPatternData::createItem).orElse(ItemStack.EMPTY);
    }

    /** Pattern quantities can exceed an item's normal stack limit, as in the old runtime copies. */
    public static ItemStack createItem(ItemStackTemplate template) {
        // ItemStackTemplate.create() validates stack limits and would erase oversized recipe quantities.
        return new ItemStack(template.item(), template.count(), template.components());
    }

    public boolean isProcessing() {
        return !processor.isEmpty();
    }

    // The old optional stack codecs used {} for empty entries. Keep that disk schema,
    // including grid holes and absent optional processing fields, without creating live stacks.
    private static final Codec<Optional<ItemStackTemplate>> ITEM = ExtraCodecs.optionalEmptyMap(ItemStackTemplate.CODEC);
    private static final Codec<Optional<FluidStackTemplate>> FLUID = ExtraCodecs.optionalEmptyMap(FluidStackTemplate.CODEC);

    public static final Codec<CraftingPatternData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ITEM.listOf().fieldOf("inputs").forGetter(CraftingPatternData::inputs),
            ITEM.fieldOf("output").forGetter(CraftingPatternData::output),
            Codec.STRING.optionalFieldOf("processor", "").forGetter(CraftingPatternData::processor),
            Identifier.CODEC.optionalFieldOf("recipe").forGetter(CraftingPatternData::recipeId),
            FLUID.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(CraftingPatternData::fluidInputs),
            ITEM.listOf().optionalFieldOf("extra_outputs", List.of()).forGetter(CraftingPatternData::extraOutputs),
            FLUID.listOf().optionalFieldOf("fluid_outputs", List.of()).forGetter(CraftingPatternData::fluidOutputs)
    ).apply(inst, CraftingPatternData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStackTemplate>> ITEM_STREAM =
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Optional<ItemStackTemplate>>> ITEMS =
            ITEM_STREAM.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Optional<FluidStackTemplate>>> FLUIDS =
            FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional).apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingPatternData> STREAM_CODEC =
            StreamCodec.of(CraftingPatternData::write, CraftingPatternData::read);

    private static void write(RegistryFriendlyByteBuf buf, CraftingPatternData d) {
        ITEMS.encode(buf, d.inputs);
        ITEM_STREAM.encode(buf, d.output);
        buf.writeUtf(d.processor);
        buf.writeBoolean(d.recipeId.isPresent());
        d.recipeId.ifPresent(buf::writeIdentifier);
        FLUIDS.encode(buf, d.fluidInputs);
        ITEMS.encode(buf, d.extraOutputs);
        FLUIDS.encode(buf, d.fluidOutputs);
    }

    private static CraftingPatternData read(RegistryFriendlyByteBuf buf) {
        var inputs = ITEMS.decode(buf);
        var output = ITEM_STREAM.decode(buf);
        String processor = buf.readUtf();
        Optional<Identifier> recipe = buf.readBoolean() ? Optional.of(buf.readIdentifier()) : Optional.empty();
        var fluidInputs = FLUIDS.decode(buf);
        var extraOutputs = ITEMS.decode(buf);
        var fluidOutputs = FLUIDS.decode(buf);
        return new CraftingPatternData(inputs, output, processor, recipe, fluidInputs, extraOutputs, fluidOutputs);
    }
}
