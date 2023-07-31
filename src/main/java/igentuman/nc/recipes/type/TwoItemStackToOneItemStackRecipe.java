package igentuman.nc.recipes.type;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

@NothingNullByDefault
public abstract class TwoItemStackToOneItemStackRecipe extends NcRecipe implements BiPredicate<@NotNull ItemStack, @NotNull ItemStack> {

    private final ItemStackIngredient input1;
    private final ItemStackIngredient input2;
    private final ItemStack output;

    /**
     * @param id         Recipe name.
     * @param input1  Main input.
     * @param input2 Secondary/extra input.
     * @param output     Output.
     */
    public TwoItemStackToOneItemStackRecipe(ResourceLocation id, ItemStackIngredient input1, ItemStackIngredient input2, ItemStack output) {
        super(id);
        this.input1 = Objects.requireNonNull(input1, "Main input cannot be null.");
        this.input2 = Objects.requireNonNull(input2, "Secondary/Extra input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ItemStack input, ItemStack extra) {
        return input1.test(input) && input2.test(extra);
    }

    /**
     * Gets the main input ingredient.
     */
    public ItemStackIngredient getMainInput() {
        return input1;
    }

    /**
     * Gets the secondary input ingredient.
     */
    public ItemStackIngredient getExtraInput() {
        return input2;
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param input Specific input.
     * @param extra Specific secondary/extra input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack extra) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean isIncomplete() {
        return input1.hasNoMatchingInstances() || input2.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input1.write(buffer);
        input2.write(buffer);
        buffer.writeItem(output);
    }
}