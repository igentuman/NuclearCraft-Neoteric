package igentuman.nc.compat.kubejs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import igentuman.nc.recipe.RecipeIngredientCodecs;
import net.minecraft.resources.ResourceKey;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import static igentuman.nc.NuclearCraft.rl;

public final class NcRecipeComponents {

    private static final int DEFAULT_FLUID_AMOUNT = 1000;

    public static final ResourceKey<RecipeComponentType<?>> ITEM_OUTPUT_TYPE = RecipeComponentType.key(rl("item_output"));
    public static final ResourceKey<RecipeComponentType<?>> FLUID_OUTPUT_TYPE = RecipeComponentType.key(rl("fluid_output"));
    public static final ResourceKey<RecipeComponentType<?>> PARTICLE_STACK_TYPE = RecipeComponentType.key(rl("particle_stack"));
    public static final ResourceKey<RecipeComponentType<?>> PARTICLE_INGREDIENT_TYPE = RecipeComponentType.key(rl("particle_ingredient"));
    public static final ResourceKey<RecipeComponentType<?>> FLAT_SIZED_INGREDIENT_TYPE = RecipeComponentType.key(rl("flat_sized_ingredient"));
    public static final ResourceKey<RecipeComponentType<?>> FLAT_SIZED_FLUID_INGREDIENT_TYPE = RecipeComponentType.key(rl("flat_sized_fluid_ingredient"));

    public static final RecipeComponent<ItemOutput> ITEM_OUTPUT =
            new CodecComponent<>(ITEM_OUTPUT_TYPE, ItemOutput.CODEC, ItemOutput.class, NcRecipeComponents::itemOutput);

    public static final RecipeComponent<FluidOutput> FLUID_OUTPUT =
            new CodecComponent<>(FLUID_OUTPUT_TYPE, FluidOutput.CODEC, FluidOutput.class, NcRecipeComponents::fluidOutput);

    public static final RecipeComponent<ParticleStack> PARTICLE_STACK =
            new CodecComponent<>(PARTICLE_STACK_TYPE, ParticleStack.CODEC, ParticleStack.class, (cx, from) -> null);

    public static final RecipeComponent<ParticleIngredient> PARTICLE_INGREDIENT =
            new CodecComponent<>(PARTICLE_INGREDIENT_TYPE, ParticleIngredient.CODEC, ParticleIngredient.class, (cx, from) -> null);

    // The native codecs enforce positive sizes. A valid tag can resolve to no entries
    // in the current pack, and recipes using that tag still need to be editable by scripts.
    /** Sized item input using NuclearCraft's flat {@code {"item"|"tag", "count"}} JSON. */
    public static final SizedIngredientComponent FLAT_SIZED_INGREDIENT =
            new SizedIngredientComponent(FLAT_SIZED_INGREDIENT_TYPE, RecipeIngredientCodecs.ITEM, true);

    /** Sized fluid input using NuclearCraft's flat {@code {"fluid"|"tag", "amount"}} JSON. */
    public static final SizedFluidIngredientComponent FLAT_SIZED_FLUID_INGREDIENT =
            new SizedFluidIngredientComponent(FLAT_SIZED_FLUID_INGREDIENT_TYPE, RecipeIngredientCodecs.FLUID, true);

    private NcRecipeComponents() {}

    public static void register(RecipeComponentTypeRegistry registry) {
        registry.unit(ITEM_OUTPUT);
        registry.unit(FLUID_OUTPUT);
        registry.unit(PARTICLE_STACK);
        registry.unit(PARTICLE_INGREDIENT);
        registry.unit(FLAT_SIZED_INGREDIENT);
        registry.unit(FLAT_SIZED_FLUID_INGREDIENT);
    }

    @Nullable
    private static ItemOutput itemOutput(RecipeScriptContext cx, Object from) {
        CountedTag tag = CountedTag.parse(from, 1);
        if (tag != null) return ItemOutput.of(TagKey.create(Registries.ITEM, tag.id()), tag.count());
        if (cx.cx().isMapLike(from)) return null;
        ItemStack stack = (ItemStack) cx.cx().jsToJava(from, TypeInfo.of(ItemStack.class));
        return stack == null || stack.isEmpty() ? null : ItemOutput.of(stack.getItem(), stack.getCount());
    }

    @Nullable
    private static FluidOutput fluidOutput(RecipeScriptContext cx, Object from) {
        CountedTag tag = CountedTag.parse(from, DEFAULT_FLUID_AMOUNT);
        if (tag != null) return FluidOutput.of(TagKey.create(Registries.FLUID, tag.id()), tag.count());
        if (cx.cx().isMapLike(from)) return null;
        FluidStack stack = (FluidStack) cx.cx().jsToJava(from, TypeInfo.of(FluidStack.class));
        return stack == null || stack.isEmpty() ? null : FluidOutput.of(stack.getFluid(), stack.getAmount());
    }

    private record CountedTag(Identifier id, int count) {
        @Nullable
        static CountedTag parse(Object from, int defaultCount) {
            if (!(from instanceof CharSequence text)) return null;
            String value = text.toString().trim();
            int count = defaultCount;
            int separator = value.indexOf("x #");
            if (separator > 0) {
                count = Integer.parseInt(value.substring(0, separator).trim());
                value = value.substring(separator + 2);
            }
            return value.startsWith("#") ? new CountedTag(Identifier.parse(value.substring(1)), count) : null;
        }
    }

    private record CodecComponent<T>(ResourceKey<RecipeComponentType<?>> type, Codec<T> codec, Class<T> valueClass,
                                     BiFunction<RecipeScriptContext, Object, T> shorthand)
            implements RecipeComponent<T> {

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(valueClass);
        }

        @Override
        public T wrap(RecipeScriptContext cx, Object from) {
            if (valueClass.isInstance(from)) return valueClass.cast(from);
            T value = shorthand.apply(cx, from);
            if (value != null) return value;
            JsonElement json = JsonUtils.of(cx.cx(), from);
            return codec.parse(cx.ops().json(), json).getOrThrow(message ->
                    new KubeRuntimeException("Invalid " + type.identifier() + " '" + json + "': " + message));
        }

        @Override
        public String toString() {
            return type.identifier().toString();
        }
    }
}
