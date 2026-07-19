package igentuman.nc.recipe.kugelblitz;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.recipe.ItemOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class KugelblitzRecipeSerializer implements RecipeSerializer<KugelblitzRecipe> {

    private static final MapCodec<KugelblitzRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(KugelblitzRecipe::input),
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

    @Override
    public MapCodec<KugelblitzRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, KugelblitzRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
