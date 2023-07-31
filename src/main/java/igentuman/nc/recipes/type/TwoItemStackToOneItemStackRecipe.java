package igentuman.nc.recipes.type;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiPredicate;

@NothingNullByDefault
public abstract class TwoItemStackToOneItemStackRecipe extends NcRecipe implements BiPredicate<@NotNull ItemStack, @NotNull ItemStack> {

    public TwoItemStackToOneItemStackRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, double timeModifier, double powerModifier, double radiationModifier) {
        super(id);
        this.inputItems = input;
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.length == 0) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.outputItems = output;
        this.timeModifier = timeModifier;
        this.powerModifier = powerModifier;
        this.radiationModifier = radiationModifier;
    }

    @Override
    public boolean test(ItemStack input, ItemStack extra) {
        return inputItems[0].test(input) && inputItems[1].test(extra);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack[] getOutput(@NotNull ItemStack input1, @NotNull ItemStack input2) {
        return outputItems;
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isIncomplete() {
        return inputItems[0].hasNoMatchingInstances() || inputItems[1].hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        for(ItemStackIngredient input: inputItems) {
            input.write(buffer);
        }
        buffer.writeInt(outputItems.length);
        for (ItemStack output : outputItems) {
            buffer.writeItem(output);
        }
    }
}