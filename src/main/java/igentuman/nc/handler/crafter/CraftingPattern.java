package igentuman.nc.handler.crafter;

import igentuman.nc.item.CraftingPatternItem;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.setup.registration.NCCrafter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class CraftingPattern {

    public static final String TAG = "NCPattern";
    public static final int GRID_SIZE = 9;

    private final NonNullList<ItemStack> inputs;
    private final List<FluidStack> fluidInputs;
    private final List<ItemStack> itemOutputs;
    private final List<FluidStack> fluidOutputs;
    private final String processor;
    @Nullable
    private final ResourceLocation recipeId;

    public CraftingPattern(NonNullList<ItemStack> inputs, ItemStack output) {
        this(inputs, List.of(), output.isEmpty() ? List.of() : List.of(output), List.of(), "", null);
    }

    public CraftingPattern(NonNullList<ItemStack> inputs, List<FluidStack> fluidInputs, List<ItemStack> itemOutputs,
                           List<FluidStack> fluidOutputs, String processor, @Nullable ResourceLocation recipeId) {
        this.inputs = inputs;
        this.fluidInputs = fluidInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.processor = processor == null ? "" : processor;
        this.recipeId = recipeId;
    }

    public NonNullList<ItemStack> inputs() {
        return inputs;
    }

    public List<FluidStack> fluidInputs() {
        return fluidInputs;
    }

    public ItemStack output() {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0);
    }

    public List<ItemStack> itemOutputs() {
        return itemOutputs;
    }

    public List<FluidStack> fluidOutputs() {
        return fluidOutputs;
    }

    public String processor() {
        return processor;
    }

    public boolean isProcessing() {
        return !processor.isEmpty();
    }

    @Nullable
    public ResourceLocation recipeId() {
        return recipeId;
    }

    public boolean usesItems() {
        for (ItemStack s : inputs) if (!s.isEmpty()) return true;
        return !itemOutputs.isEmpty();
    }

    public boolean usesFluids() {
        return !fluidInputs.isEmpty() || !fluidOutputs.isEmpty();
    }

    public boolean hasAnyOutput() {
        return !itemOutputs.isEmpty() || !fluidOutputs.isEmpty();
    }

    public static boolean isPatternItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CraftingPatternItem;
    }

    public static boolean isEncoded(ItemStack stack) {
        return isPatternItem(stack) && stack.hasTag() && stack.getTag().contains(TAG);
    }

    public static boolean isBlank(ItemStack stack) { return isPatternItem(stack) && !isEncoded(stack); }
    public static boolean isPattern(ItemStack stack) { return isEncoded(stack); }
    public static boolean isProcessing(ItemStack stack) {
        return isEncoded(stack) && !stack.getTag().getCompound(TAG).getString("Processor").isEmpty();
    }
    public static ItemStack output(ItemStack stack) {
        return isEncoded(stack) ? ItemStack.of(stack.getTag().getCompound(TAG).getCompound("Output")) : ItemStack.EMPTY;
    }

    public static ItemStack encode(Container grid, ItemStack output) {
        NonNullList<ItemStack> inputs = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < GRID_SIZE && i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.isEmpty()) continue;
            ItemStack one = s.copy();
            one.setCount(1);
            inputs.set(i, one);
        }
        ItemStack encoded = new ItemStack(NCCrafter.CRAFTING_PATTERN.get());
        write(encoded, inputs, output, "", null, List.of(), List.of(), List.of());
        return encoded;
    }

    public static ItemStack encodeProcessing(NcRecipe recipe, ResourceLocation recipeId) {
        if (!ProcessorRecipes.isAllowed(recipe.getCodeId()) || recipe.isIncomplete()) return ItemStack.EMPTY;
        List<ItemStack> inputs = new ArrayList<>();
        for (var ingredient : recipe.getInputItems()) inputs.add(ingredient.getRepresentations().get(0).copy());
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack stack : recipe.getResultItems()) if (!stack.isEmpty()) outputs.add(stack.copy());
        ItemStack primary = outputs.isEmpty() ? ItemStack.EMPTY : outputs.remove(0);
        List<FluidStack> fluidIn = new ArrayList<>();
        for (var ingredient : recipe.getInputFluids()) fluidIn.add(ingredient.getRepresentations().get(0).copy());
        List<FluidStack> fluidOut = new ArrayList<>();
        for (FluidStack fluid : recipe.getOutputFluids()) if (!fluid.isEmpty()) fluidOut.add(fluid.copy());
        ItemStack encoded = new ItemStack(NCCrafter.CRAFTING_PATTERN.get());
        write(encoded, inputs, primary, recipe.getCodeId(), recipeId, fluidIn, outputs, fluidOut);
        return encoded;
    }

    @Nullable
    public static CraftingPattern from(ItemStack stack) {
        if (!isEncoded(stack)) return null;
        PatternData data = read(stack.getTag().getCompound(TAG));

        if (data.isProcessing()) {
            if (!ProcessorRecipes.isAllowed(data.processor())) return null;
            NonNullList<ItemStack> inputs = NonNullList.create();
            for (ItemStack s : data.inputs()) {
                if (!s.isEmpty()) inputs.add(s.copy());
            }
            List<ItemStack> outputs = new ArrayList<>();
            if (!data.output().isEmpty()) outputs.add(data.output().copy());
            for (ItemStack s : data.extraOutputs()) {
                if (!s.isEmpty()) outputs.add(s.copy());
            }
            List<FluidStack> fluidIn = new ArrayList<>();
            for (FluidStack f : data.fluidInputs()) {
                if (!f.isEmpty()) fluidIn.add(f.copy());
            }
            List<FluidStack> fluidOut = new ArrayList<>();
            for (FluidStack f : data.fluidOutputs()) {
                if (!f.isEmpty()) fluidOut.add(f.copy());
            }
            if (outputs.isEmpty() && fluidOut.isEmpty()) return null;
            if (inputs.isEmpty() && fluidIn.isEmpty()) return null;
            return new CraftingPattern(inputs, fluidIn, outputs, fluidOut, data.processor(), data.recipeId().orElse(null));
        }

        NonNullList<ItemStack> inputs = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < GRID_SIZE && i < data.inputs().size(); i++) {
            inputs.set(i, data.inputs().get(i).copy());
        }
        ItemStack output = data.output().copy();
        if (output.isEmpty()) return null;
        return new CraftingPattern(inputs, output);
    }

    private record PatternData(List<ItemStack> inputs, ItemStack output, String processor,
                               Optional<ResourceLocation> recipeId, List<FluidStack> fluidInputs,
                               List<ItemStack> extraOutputs, List<FluidStack> fluidOutputs) {
        boolean isProcessing() { return !processor.isEmpty(); }
    }

    private static void write(ItemStack encoded, List<ItemStack> inputs, ItemStack output, String processor,
                              ResourceLocation recipe, List<FluidStack> fluids, List<ItemStack> extras,
                              List<FluidStack> fluidOutputs) {
        CompoundTag tag = new CompoundTag();
        tag.put("Inputs", writeItems(inputs));
        tag.put("Output", output.save(new CompoundTag()));
        tag.putString("Processor", processor);
        if (recipe != null) tag.putString("Recipe", recipe.toString());
        tag.put("FluidInputs", writeFluids(fluids));
        tag.put("ExtraOutputs", writeItems(extras));
        tag.put("FluidOutputs", writeFluids(fluidOutputs));
        encoded.getOrCreateTag().put(TAG, tag);
    }

    private static ListTag writeItems(List<ItemStack> stacks) {
        ListTag list = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i).isEmpty()) continue;
            CompoundTag entry = stacks.get(i).save(new CompoundTag());
            entry.putByte("Slot", (byte) i);
            list.add(entry);
        }
        return list;
    }

    private static ListTag writeFluids(List<FluidStack> stacks) {
        ListTag list = new ListTag();
        for (FluidStack stack : stacks) if (!stack.isEmpty()) list.add(stack.writeToNBT(new CompoundTag()));
        return list;
    }

    private static List<ItemStack> readItems(CompoundTag tag, String name) {
        List<ItemStack> stacks = new ArrayList<>();
        ListTag list = tag.getList(name, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            while (stacks.size() <= slot) stacks.add(ItemStack.EMPTY);
            stacks.set(slot, ItemStack.of(entry));
        }
        return stacks;
    }

    private static List<FluidStack> readFluids(CompoundTag tag, String name) {
        List<FluidStack> fluids = new ArrayList<>();
        ListTag list = tag.getList(name, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) fluids.add(FluidStack.loadFluidStackFromNBT(list.getCompound(i)));
        return fluids;
    }

    private static PatternData read(CompoundTag tag) {
        return new PatternData(readItems(tag, "Inputs"), ItemStack.of(tag.getCompound("Output")),
                tag.getString("Processor"), Optional.ofNullable(ResourceLocation.tryParse(tag.getString("Recipe"))),
                readFluids(tag, "FluidInputs"), readItems(tag, "ExtraOutputs"), readFluids(tag, "FluidOutputs"));
    }
}
