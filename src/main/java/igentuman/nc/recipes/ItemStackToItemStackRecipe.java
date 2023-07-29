package igentuman.nc.recipes;

import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@NothingNullByDefault
public abstract class ItemStackToItemStackRecipe extends NcRecipe implements Predicate<@NotNull ItemStack> {

    public ItemStackToItemStackRecipe(
            ResourceLocation id, ItemStackIngredient input, ItemStack output,
            double timeModifier, double powerModifier, double radiationModifier) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
        inputItems = new ItemStackIngredient[]{input};
        outputItems = new ItemStack[]{output};
        this.timeModifier = timeModifier;
        this.powerModifier = powerModifier;
        this.radiationModifier = radiationModifier;
    }


    @Override
    public RecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return NcRecipeSerializers.SERIALIZERS.get(ID).get();
    }

    @Override
    public String getGroup() {
        return NCProcessors.PROCESSORS.get(ID).get().getName().getString();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(NCProcessors.PROCESSORS.get(ID).get());
    }

    @Override
    public boolean test(ItemStack input) {
        return this.input.test(input);
    }

    /**
     * Gets the input ingredient.
     */
    public ItemStackIngredient getInput() {
        return input;
    }

    public ItemStack getFirstInputStack() {
        return input.getRepresentations().get(0);
    }

    @Contract(value = "_ -> new", pure = true)
    public ItemStack getOutput(ItemStack input) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public RecipeType<ItemStackToItemStackRecipe> getType() {
        return NcRecipeType.RECIPES.get(ID).get();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        buffer.writeItem(output);
    }

}