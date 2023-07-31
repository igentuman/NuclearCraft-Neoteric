package igentuman.nc.recipes.type;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@NothingNullByDefault
public abstract class ItemStackToItemStackRecipe extends NcRecipe implements Predicate<@NotNull ItemStack> {

    public ItemStackToItemStackRecipe(
            ResourceLocation id, ItemStackIngredient input, ItemStack[] output,
            double timeModifier, double powerModifier, double radiationModifier) {
        super(id);
        inputItems = new ItemStackIngredient[1];
        inputItems[0] = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.length == 0) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        outputItems = output;
        this.timeModifier = timeModifier;
        this.powerModifier = powerModifier;
        this.radiationModifier = radiationModifier;
    }

    @Override
    public ItemStack getResultItem() {
        return outputItems[0];
    }

    @Override
    public boolean test(ItemStack input) {
        return this.inputItems[0].test(input);
    }

    /**
     * Gets the input ingredient.
     */
    public ItemStackIngredient getInput() {
        return inputItems[0];
    }

    public ItemStack getFirstInputStack() {
        return inputItems[0].getRepresentations().get(0);
    }

    @Contract(value = "_ -> new", pure = true)
    public ItemStack[] getOutput(ItemStack input) {
        return outputItems;
    }

    public List<ItemStack> getOutputDefinition() {
        return Arrays.asList(outputItems);
    }

    @Override
    public boolean isIncomplete() {
        return inputItems[0].hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        inputItems[0].write(buffer);
        for (ItemStack output : outputItems) {
            buffer.writeItem(output);
        }
    }

}