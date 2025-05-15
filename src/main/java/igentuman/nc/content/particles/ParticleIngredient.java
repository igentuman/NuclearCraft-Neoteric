package igentuman.nc.content.particles;

import igentuman.nc.content.particles.creator.ParticleIngredientCreator;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for particle ingredients.
 */
public abstract class ParticleIngredient implements IParticleIngredient {

    protected int amount;

    /**
     * Creates a copy of this ingredient.
     *
     * @return A copy of this ingredient.
     */
    public ParticleIngredient copy() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        this.write(buffer);
        return ParticleIngredientCreator.INSTANCE.read(buffer);
    }

    /**
     * Sets the amount of this ingredient.
     *
     * @param amount The new amount.
     * @throws IllegalArgumentException if the amount is negative.
     */
    public void setAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }
}