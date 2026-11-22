package igentuman.nc.recipe.kugelblitz;

import igentuman.nc.recipe.RecipeIngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.api.recipe.ItemOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** JSON and network codecs for {@link KugelblitzRecipe}. */
public final class KugelblitzRecipeSerializer {

    private static final MapCodec<KugelblitzRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.ITEM.fieldOf("input").forGetter(KugelblitzRecipe::input),
            ItemOutput.CODEC.fieldOf("output").forGetter(KugelblitzRecipe::output),
            Codec.DOUBLE.optionalFieldOf("time_modifier", 1.0).forGetter(KugelblitzRecipe::timeModifier),
            Codec.DOUBLE.optionalFieldOf("power_modifier", 1.0).forGetter(KugelblitzRecipe::powerModifier)
    ).apply(inst, KugelblitzRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, KugelblitzRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, KugelblitzRecipe::input,
                    ItemOutput.STREAM_CODEC, KugelblitzRecipe::output,
                    ByteBufCodecs.DOUBLE, KugelblitzRecipe::timeModifier,
                    ByteBufCodecs.DOUBLE, KugelblitzRecipe::powerModifier,
                    KugelblitzRecipe::new);

    private KugelblitzRecipeSerializer() {}

    public static RecipeSerializer<KugelblitzRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
