package igentuman.nc.handler.crafter;

import igentuman.nc.api.crafting.CraftingPatternData;
import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.item.CraftingPatternItem;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.setup.entries.Crafter;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingPattern {

    public static final int GRID_SIZE = 9;

    private final NonNullList<ItemStack> inputs;
    private final List<FluidStack> fluidInputs;
    private final List<ItemStack> itemOutputs;
    private final List<FluidStack> fluidOutputs;
    private final String processor;
    @Nullable
    private final Identifier recipeId;

    public CraftingPattern(NonNullList<ItemStack> inputs, ItemStack output) {
        this(inputs, List.of(), output.isEmpty() ? List.of() : List.of(output), List.of(), "", null);
    }

    public CraftingPattern(NonNullList<ItemStack> inputs, List<FluidStack> fluidInputs, List<ItemStack> itemOutputs,
                           List<FluidStack> fluidOutputs, String processor, @Nullable Identifier recipeId) {
        this.inputs = inputs;
        this.fluidInputs = fluidInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.processor = processor == null ? "" : processor;
        this.recipeId = recipeId;
    }

    /** Item inputs. For crafting patterns this is the 3x3 grid (count 1 each); for processing, sized stacks. */
    public NonNullList<ItemStack> inputs() {
        return inputs;
    }

    public List<FluidStack> fluidInputs() {
        return fluidInputs;
    }

    /** Primary item output, or EMPTY when the pattern only produces fluids. */
    public ItemStack output() {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst();
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
    public Identifier recipeId() {
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
        return isPatternItem(stack) && stack.has(CraftingPatternData.componentType());
    }

    public static boolean isBlank(ItemStack stack) {
        return isPatternItem(stack) && !isEncoded(stack);
    }

    public static boolean isPattern(ItemStack stack) {
        return isEncoded(stack);
    }

    public static boolean isProcessing(ItemStack stack) {
        CraftingPatternData data = stack.get(CraftingPatternData.componentType());
        return data != null && data.isProcessing();
    }

    public static ItemStack output(ItemStack stack) {
        CraftingPatternData data = stack.get(CraftingPatternData.componentType());
        return data == null ? ItemStack.EMPTY : data.createOutput();
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
        ItemStack encoded = new ItemStack(Crafter.CRAFTING_PATTERN.get());
        encoded.set(CraftingPatternData.componentType(), CraftingPatternData.fromStacks(inputs, output));
        return encoded;
    }

    /** Encodes a processor recipe: representative item inputs with counts, fluids and every output. */
    public static ItemStack encodeProcessing(UniversalProcessorRecipe recipe, Identifier recipeId) {
        if (!ProcessorRecipes.isAllowed(recipe.getProcessorName())) return ItemStack.EMPTY;
        ProcessorRecipeStacks stacks = ProcessorRecipeStacks.of(recipe);
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack s : stacks.primaryItemInputs()) inputs.add(s.copy());
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack s : stacks.itemOutputs()) outputs.add(s.copy());
        ItemStack primary = outputs.isEmpty() ? ItemStack.EMPTY : outputs.removeFirst();
        List<FluidStack> fluidIn = new ArrayList<>();
        for (FluidStack f : stacks.primaryFluidInputs()) fluidIn.add(f.copy());
        List<FluidStack> fluidOut = new ArrayList<>();
        for (FluidStack f : stacks.fluidOutputs()) fluidOut.add(f.copy());

        ItemStack encoded = new ItemStack(Crafter.CRAFTING_PATTERN.get());
        encoded.set(CraftingPatternData.componentType(), CraftingPatternData.fromStacks(
                inputs, primary, recipe.getProcessorName(), Optional.ofNullable(recipeId), fluidIn, outputs, fluidOut));
        return encoded;
    }

    @Nullable
    public static CraftingPattern from(ItemStack stack) {
        CraftingPatternData data = stack.get(CraftingPatternData.componentType());
        if (!isPatternItem(stack) || data == null) return null;

        if (data.isProcessing()) {
            if (!ProcessorRecipes.isAllowed(data.processor())) return null;
            NonNullList<ItemStack> inputs = NonNullList.create();
            for (var template : data.inputs()) {
                template.ifPresent(value -> inputs.add(CraftingPatternData.createItem(value)));
            }
            List<ItemStack> outputs = new ArrayList<>();
            data.output().ifPresent(value -> outputs.add(CraftingPatternData.createItem(value)));
            for (var template : data.extraOutputs()) {
                template.ifPresent(value -> outputs.add(CraftingPatternData.createItem(value)));
            }
            List<FluidStack> fluidIn = new ArrayList<>();
            for (var template : data.fluidInputs()) {
                template.ifPresent(value -> fluidIn.add(value.create()));
            }
            List<FluidStack> fluidOut = new ArrayList<>();
            for (var template : data.fluidOutputs()) {
                template.ifPresent(value -> fluidOut.add(value.create()));
            }
            if (outputs.isEmpty() && fluidOut.isEmpty()) return null;
            if (inputs.isEmpty() && fluidIn.isEmpty()) return null;
            return new CraftingPattern(inputs, fluidIn, outputs, fluidOut, data.processor(), data.recipeId().orElse(null));
        }

        NonNullList<ItemStack> inputs = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < GRID_SIZE && i < data.inputs().size(); i++) {
            inputs.set(i, data.inputs().get(i).map(CraftingPatternData::createItem).orElse(ItemStack.EMPTY));
        }
        ItemStack output = data.createOutput();
        if (output.isEmpty()) return null;
        return new CraftingPattern(inputs, output);
    }
}
