package igentuman.nc.recipes.ingredient;

import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.world.item.Items.BARRIER;

public abstract class ItemStackIngredient implements InputIngredient<@NotNull ItemStack> {

    protected int amount;

    public ItemStackIngredient copy() {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        this.write(buffer);
        return IngredientCreatorAccess.item().read(buffer);
    }

    public void setAmount(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = i;
    }

    public boolean isValid() {
        List<@NotNull ItemStack> representations = getRepresentations();
        return !representations.isEmpty() && !representations.get(0).isEmpty() && !representations.get(0).is(BARRIER);
    }
}