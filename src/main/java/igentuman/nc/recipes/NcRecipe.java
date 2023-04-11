package igentuman.nc.recipes;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.multiblock.FissionRecipe;
import igentuman.nc.util.IgnoredIInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class NcRecipe implements Recipe<IgnoredIInventory> {
    private final ResourceLocation id;

    protected ItemStackIngredient input;
    protected FluidStackIngredient inputFluid;
    protected FluidStack outFluid;

    protected ItemStack output;

    protected FluidStackIngredient[] inputFluids;
    protected FluidStack[] outputFluids;

    protected ItemStackIngredient[] inputItems;
    protected ItemStack[] outputItems;

    /**
     * @param id Recipe name.
     */
    protected NcRecipe(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
    }

    public NonNullList<Ingredient> getItemIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStackIngredient inputItem : inputItems) {
            ingredients.add(Ingredient.of(inputItem.getRepresentations().toArray(new ItemStack[inputItem.getRepresentations().size()])));
        }
        return ingredients;
    }

    public abstract void write(FriendlyByteBuf buffer);

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(@NotNull IgnoredIInventory inv, @NotNull Level world) {
        return !isIncomplete();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public abstract boolean isIncomplete();

    @NotNull
    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @NotNull
    public FluidStack getResultFluid() {
        return FluidStack.EMPTY;
    }

    public List<ItemStack> getResultItems() {
        return List.of(getResultItem());
    }

    public List<FluidStack> getInputFluids(int id) {
        NonNullList<FluidStack> ingredients = NonNullList.create();
        int i = 0;
        for (FluidStackIngredient inFluid : inputFluids) {
            if(i == id) ingredients.addAll(inFluid.getRepresentations());
            i++;
        }
        return ingredients;
    }

    public List<FluidStack> getOutputFluids(int id) {
        NonNullList<FluidStack> ingredients = NonNullList.create();
        int i = 0;
        for (FluidStack outFluid : outputFluids) {
            if(i == id) ingredients.add(outFluid);
            i++;
        }
        return ingredients;
    }
}