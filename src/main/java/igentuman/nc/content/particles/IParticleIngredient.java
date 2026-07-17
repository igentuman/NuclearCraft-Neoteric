package igentuman.nc.content.particles;

import com.google.gson.JsonElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Interface describing the base methods for particle ingredients in recipes.
 * source https://github.com/Lach01298/QMD
 */
@MethodsReturnNonnullByDefault
public interface IParticleIngredient extends Predicate<ParticleStack>
{
    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param particleStack Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@NotNull ParticleStack particleStack);

    /**
     * Gets a copy of the internal instance that matches the given argument.
     *
     * @param particleStack Input argument.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    ParticleStack getMatchingInstance(ParticleStack particleStack);

    /**
     * Gets the amount of the given argument that is needed, or zero if the given argument doesn't match.
     *
     * @param particleStack Input argument.
     *
     * @return Amount of the given argument that is needed.
     */
    long getNeededAmount(ParticleStack particleStack);

    /**
     * Checks if this ingredient has any matching instances.
     *
     * @return {@code true} for no matching instances, {@code false} if there are any matching instances.
     */
    default boolean hasNoMatchingInstances() {
        return getRepresentations().isEmpty();
    }

    /**
     * Primarily for JEI, a list of valid instances of the type
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     *
     * @apiNote Do not modify any of the values returned by the representations
     */
    List<ParticleStack> getRepresentations();

    /**
     * Writes this ingredient to a PacketBuffer.
     *
     * @param buffer The buffer to write to.
     */
    void write(FriendlyByteBuf buffer);

    /**
     * Serializes this ingredient to a JsonElement
     *
     * @return JsonElement representation of this ingredient.
     */
    JsonElement serialize();

    /**
     * Gets the name of this ingredient.
     *
     * @return The name of this ingredient.
     */
    String getName();

    /**
     * Gets the amount of this ingredient.
     *
     * @return The amount of this ingredient.
     */
    int getAmount();
}
