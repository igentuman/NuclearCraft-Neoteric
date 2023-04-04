package igentuman.nc.recipes;

import igentuman.nc.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class FissionRecipe extends NcRecipe implements Predicate<@NotNull ItemStack> {

    private final ItemStackIngredient input;
    private final ItemStack output;
    
    public FissionRecipe(ResourceLocation id, ItemStackIngredient input, ItemStackIngredient extraInput, ItemStack output) {
        super(id);
        this.input = Objects.requireNonNull(input, "Main input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ItemStack inputStack) {
        return input.test(inputStack);
    }
    
    public ItemStackIngredient getInput() {
        return input;
    }
    
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack extra) {
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
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        buffer.writeItem(output);
    }
}