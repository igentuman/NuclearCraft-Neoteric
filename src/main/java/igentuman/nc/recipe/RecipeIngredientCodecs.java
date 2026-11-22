package igentuman.nc.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/** Keeps NuclearCraft's flat input JSON while delegating ingredient semantics to native codecs. */
public final class RecipeIngredientCodecs {
    private static final Codec<Ingredient> SINGLE_INGREDIENT = flat(Ingredient.CODEC.fieldOf("ingredient").codec(), "item", null);
    public static final Codec<Ingredient> INGREDIENT = Codec.either(SINGLE_INGREDIENT, SINGLE_INGREDIENT.listOf(1, Integer.MAX_VALUE))
            .xmap(either -> either.map(ingredient -> ingredient, ingredients -> new CompoundIngredient(ingredients).toVanilla()), Either::left);
    public static final Codec<SizedIngredient> ITEM = flat(SizedIngredient.NESTED_CODEC, "item", "count");
    public static final Codec<SizedFluidIngredient> FLUID = flat(SizedFluidIngredient.CODEC, "fluid", "amount");

    private RecipeIngredientCodecs() {}

    private static <S> Codec<S> flat(Codec<S> nested, String resourceField, @Nullable String sizeField) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getMap(input).flatMap(map -> {
                    T resource = map.get(resourceField);
                    T tag = map.get("tag");
                    T custom = map.get("neoforge:ingredient_type");
                    int forms = (resource == null ? 0 : 1) + (tag == null ? 0 : 1) + (custom == null ? 0 : 1);
                    if (forms != 1) {
                        return DataResult.error(() -> "Expected exactly one of " + resourceField + ", tag or neoforge:ingredient_type");
                    }
                    DataResult<T> ingredient;
                    if (resource != null || tag != null) {
                        ingredient = ops.getStringValue(tag != null ? tag : resource)
                                .map(id -> ops.createString(tag != null ? "#" + id : id));
                    } else {
                        ingredient = DataResult.success(ops.createMap(map.entries()
                                .filter(entry -> sizeField == null || !ops.createString(sizeField).equals(entry.getFirst()))));
                    }
                    return ingredient.flatMap(value -> {
                        Map<T, T> fields = new LinkedHashMap<>();
                        fields.put(ops.createString("ingredient"), value);
                        T size = sizeField == null ? null : map.get(sizeField);
                        if (size != null) fields.put(ops.createString(sizeField), size);
                        return nested.parse(ops, ops.createMap(fields)).map(decoded -> Pair.of(decoded, ops.empty()));
                    });
                });
            }

            @Override
            public <T> DataResult<T> encode(S input, DynamicOps<T> ops, T prefix) {
                return nested.encodeStart(ops, input).flatMap(encoded -> ops.getMap(encoded).flatMap(map -> {
                    T ingredient = map.get("ingredient");
                    Map<T, T> fields = new LinkedHashMap<>();
                    var simple = ops.getStringValue(ingredient).result();
                    if (simple.isPresent()) {
                        String id = simple.get();
                        fields.put(ops.createString(id.startsWith("#") ? "tag" : resourceField),
                                ops.createString(id.startsWith("#") ? id.substring(1) : id));
                    } else {
                        var custom = ops.getMap(ingredient).result();
                        if (custom.isPresent()) {
                            custom.get().entries().forEach(entry -> fields.put(entry.getFirst(), entry.getSecond()));
                        } else if (ops.getStream(ingredient).result().isPresent()) {
                            // Native holder sets may encode multiple direct values as a list.
                            fields.put(ops.createString("neoforge:ingredient_type"), ops.createString("neoforge:compound"));
                            fields.put(ops.createString("children"), ingredient);
                        } else {
                            return DataResult.error(() -> "Ingredient has no flat representation");
                        }
                    }
                    T size = sizeField == null ? null : map.get(sizeField);
                    if (size != null) fields.put(ops.createString(sizeField), size);
                    return ops.mergeToMap(prefix, fields);
                }));
            }
        };
    }
}
