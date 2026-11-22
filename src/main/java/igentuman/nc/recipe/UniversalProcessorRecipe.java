package igentuman.nc.recipe;

import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.api.recipe.ProcessorRecipeView;
import igentuman.nc.config.Processors;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import igentuman.nc.setup.Registers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/** Shared recipe implementation for every processor, holding item/fluid inputs and outputs, process time, and energy. */
public class UniversalProcessorRecipe implements Recipe<ProcessorRecipeInput>, ProcessorRecipeView {

    private final String processorName;
    private final List<SizedIngredient> itemInputs;
    private final List<SizedFluidIngredient> fluidInputs;
    private final List<ItemOutput> itemOutputs;
    private final List<FluidOutput> fluidOutputs;
    private final int processTime;
    private final int energyPerTick;

    public UniversalProcessorRecipe(
            String processorName,
            List<SizedIngredient> itemInputs,
            List<SizedFluidIngredient> fluidInputs,
            List<ItemOutput> itemOutputs,
            List<FluidOutput> fluidOutputs,
            int processTime,
            int energyPerTick
    ) {
        this.processorName = processorName;
        this.itemInputs = List.copyOf(itemInputs);
        this.fluidInputs = List.copyOf(fluidInputs);
        this.itemOutputs = List.copyOf(itemOutputs);
        this.fluidOutputs = List.copyOf(fluidOutputs);
        this.processTime = processTime;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(ProcessorRecipeInput input, Level level) {
        if (level.isClientSide()) return false;

        boolean[] usedItemSlots = new boolean[input.size()];
        for (SizedIngredient ingredient : itemInputs) {
            int match = -1;
            for (int slot = 0; slot < input.size(); slot++) {
                if (usedItemSlots[slot]) continue;
                if (ingredient.test(input.getItem(slot))) {
                    match = slot;
                    break;
                }
            }
            if (match == -1) return false;
            usedItemSlots[match] = true;
        }

        boolean[] usedFluidSlots = new boolean[input.fluidSize()];
        for (SizedFluidIngredient ingredient : fluidInputs) {
            int match = -1;
            for (int slot = 0; slot < input.fluidSize(); slot++) {
                if (usedFluidSlots[slot]) continue;
                if (ingredient.test(input.getFluid(slot))) {
                    match = slot;
                    break;
                }
            }
            if (match == -1) return false;
            usedFluidSlots[match] = true;
        }

        return true;
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput input) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst().resolve();
    }

    /** First deterministic output for machine and encoder presentation. */
    public ItemStack getResultItem() {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst().resolve();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return Registers.PROCESSOR_RECIPE_CATEGORY.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<UniversalProcessorRecipe> getSerializer() {
        return (RecipeSerializer<UniversalProcessorRecipe>) ModEntries.get(processorName).recipeSerializer().get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<UniversalProcessorRecipe> getType() {
        return (RecipeType<UniversalProcessorRecipe>) ModEntries.get(processorName).recipeType().get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // --- Getters ---

    @Override
    public String getProcessorName() {
        return processorName;
    }

    @Override
    public List<SizedIngredient> getItemInputs() {
        return itemInputs;
    }

    @Override
    public List<SizedFluidIngredient> getFluidInputs() {
        return fluidInputs;
    }

    @Override
    public List<ItemOutput> getItemOutputs() {
        return itemOutputs;
    }

    @Override
    public List<FluidOutput> getFluidOutputs() {
        return fluidOutputs;
    }

    /** Resolves item output {@code i} to a concrete stack at production time. */
    public ItemStack getOutputStack(int i) {
        return itemOutputs.get(i).resolve();
    }

    /** Resolves fluid output {@code i} to a concrete stack at production time. */
    public FluidStack getOutputFluidStack(int i) {
        return fluidOutputs.get(i).resolve();
    }

    @Override
    public int getBaseProcessTime() {
        return processTime;
    }

    @Override
    public int getBaseEnergyPerTick() {
        return energyPerTick;
    }

    @Override
    public int getProcessTime() {
        return Processors.processTime(processorName, processTime);
    }

    @Override
    public int getEnergyPerTick() {
        return Processors.energyPerTick(processorName, energyPerTick);
    }

    public boolean isComplete() {
        for (SizedIngredient si : itemInputs) {
            if (si.ingredient().isEmpty()) return false;
        }
        for (SizedFluidIngredient sfi : fluidInputs) {
            if (sfi.ingredient().fluids().isEmpty()) return false;
        }
        for (ItemOutput output : itemOutputs) {
            if (!output.isComplete()) return false;
        }
        for (FluidOutput output : fluidOutputs) {
            if (!output.isComplete()) return false;
        }
        return true;
    }

    /** Describes inputs and outputs that decoded successfully but resolve to no registered content. */
    public List<String> getResolutionProblems() {
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < itemInputs.size(); i++) {
            SizedIngredient input = itemInputs.get(i);
            if (input.ingredient().isEmpty()) {
                problems.add("item_inputs[" + i + "] resolved to no items: " + input);
            }
        }
        for (int i = 0; i < fluidInputs.size(); i++) {
            SizedFluidIngredient input = fluidInputs.get(i);
            if (input.ingredient().fluids().isEmpty()) {
                problems.add("fluid_inputs[" + i + "] resolved to no fluids: " + input);
            }
        }
        for (int i = 0; i < itemOutputs.size(); i++) {
            ItemOutput output = itemOutputs.get(i);
            if (!output.isComplete()) {
                problems.add("item_outputs[" + i + "] could not be resolved: " + output);
            }
        }
        for (int i = 0; i < fluidOutputs.size(); i++) {
            FluidOutput output = fluidOutputs.get(i);
            if (!output.isComplete()) {
                problems.add("fluid_outputs[" + i + "] could not be resolved: " + output);
            }
        }
        return problems;
    }
}
