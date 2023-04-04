package igentuman.nc.recipes.ingredients.creator;

import com.google.gson.JsonElement;
import igentuman.nc.recipes.ingredients.InputIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;


public interface IIngredientCreator<TYPE, STACK, INGREDIENT extends InputIngredient<@NotNull STACK>> {

    INGREDIENT from(STACK instance);

    INGREDIENT from(TYPE instance, int amount);

    INGREDIENT from(TagKey<TYPE> tag, int amount);

    INGREDIENT read(FriendlyByteBuf buffer);

    INGREDIENT deserialize(@Nullable JsonElement json);

    INGREDIENT createMulti(INGREDIENT... ingredients);

    INGREDIENT from(Stream<INGREDIENT> ingredients);
}