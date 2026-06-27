package igentuman.nc.recipe;

import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

/**
 * Universal recipe class that works for any processor.
 * Each processor registers its own RecipeType and RecipeSerializer,
 * but they all share this single Recipe implementation.
 */
public class UniversalProcessorRecipe implements Recipe<ProcessorRecipeInput> {

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
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.processTime = processTime;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(ProcessorRecipeInput input, Level level) {
        if (level.isClientSide()) return false;

        // Check all item inputs match
        for (int i = 0; i < itemInputs.size(); i++) {
            if (i >= input.size()) return false;
            if (!itemInputs.get(i).test(input.getItem(i))) return false;
        }

        // Check all fluid inputs match
        for (int i = 0; i < fluidInputs.size(); i++) {
            if (i >= input.fluidSize()) return false;
            if (!fluidInputs.get(i).test(input.getFluid(i))) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput input, HolderLookup.Provider registries) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst().resolve();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.getFirst().resolve();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient si : itemInputs) {
            list.add(si.ingredient());
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModEntries.get(processorName).recipeSerializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModEntries.get(processorName).recipeType().get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // --- Getters ---

    public String getProcessorName() {
        return processorName;
    }

    public List<SizedIngredient> getItemInputs() {
        return itemInputs;
    }

    public List<SizedFluidIngredient> getFluidInputs() {
        return fluidInputs;
    }

    public List<ItemOutput> getItemOutputs() {
        return itemOutputs;
    }

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

    public int getProcessTime() {
        return processTime;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public boolean isComplete() {
        for (SizedIngredient si : itemInputs) {
            if (si.ingredient().isEmpty()) return false;
        }
        for (SizedFluidIngredient sfi : fluidInputs) {
            if (sfi.ingredient().isEmpty()) return false;
        }
        for (ItemOutput output : itemOutputs) {
            if (!output.isComplete()) return false;
        }
        for (FluidOutput output : fluidOutputs) {
            if (!output.isComplete()) return false;
        }
        return true;
    }
}
