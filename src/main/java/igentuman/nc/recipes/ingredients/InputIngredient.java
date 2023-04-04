package igentuman.nc.recipes.ingredients;

import com.google.gson.JsonElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;


@MethodsReturnNonnullByDefault
public interface InputIngredient<TYPE> extends Predicate<TYPE> {


    boolean testType(@NotNull TYPE type);

    TYPE getMatchingInstance(TYPE type);

    long getNeededAmount(TYPE type);

    default boolean hasNoMatchingInstances() {
        return getRepresentations().isEmpty();
    }

    List<TYPE> getRepresentations();

    void write(FriendlyByteBuf buffer);

    JsonElement serialize();
}