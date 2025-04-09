package igentuman.nc.recipes.ingredient;

import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackIngredient implements InputIngredient<@NotNull ItemStack> {

    protected int amount;

    public ItemStackIngredient copy() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        this.write(buffer);
        return IngredientCreatorAccess.item().read(buffer);
    }

    public void setAmount(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = i;
    }
}