package igentuman.nc.compat.kubejs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import static igentuman.nc.NuclearCraft.rl;

public final class NcRecipeComponents {

    private static final int DEFAULT_FLUID_AMOUNT = 1000;

    public static final RecipeComponentType<ItemOutput> ITEM_OUTPUT = RecipeComponentType.unit(rl("item_output"),
            type -> new CodecComponent<>(type, ItemOutput.CODEC, ItemOutput.class, NcRecipeComponents::itemOutput));

    public static final RecipeComponentType<FluidOutput> FLUID_OUTPUT = RecipeComponentType.unit(rl("fluid_output"),
            type -> new CodecComponent<>(type, FluidOutput.CODEC, FluidOutput.class, NcRecipeComponents::fluidOutput));

    public static final RecipeComponentType<ParticleStack> PARTICLE_STACK = RecipeComponentType.unit(rl("particle_stack"),
            type -> new CodecComponent<>(type, ParticleStack.CODEC, ParticleStack.class, (cx, from) -> null));

    public static final RecipeComponentType<ParticleIngredient> PARTICLE_INGREDIENT = RecipeComponentType.unit(rl("particle_ingredient"),
            type -> new CodecComponent<>(type, ParticleIngredient.CODEC, ParticleIngredient.class, (cx, from) -> null));

    private NcRecipeComponents() {}

    public static void register(RecipeComponentTypeRegistry registry) {
        registry.register(ITEM_OUTPUT);
        registry.register(FLUID_OUTPUT);
        registry.register(PARTICLE_STACK);
        registry.register(PARTICLE_INGREDIENT);
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

    private record CountedTag(ResourceLocation id, int count) {
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
            return value.startsWith("#") ? new CountedTag(ResourceLocation.parse(value.substring(1)), count) : null;
        }
    }

    private record CodecComponent<T>(RecipeComponentType<?> type, Codec<T> codec, Class<T> valueClass,
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
                    new KubeRuntimeException("Invalid " + type.id() + " '" + json + "': " + message));
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
}
