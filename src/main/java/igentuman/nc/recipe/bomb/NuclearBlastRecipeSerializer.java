package igentuman.nc.recipe.bomb;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.recipe.ItemOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class NuclearBlastRecipeSerializer implements RecipeSerializer<NuclearBlastRecipe> {

    private static final MapCodec<NuclearBlastRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(NuclearBlastRecipe::input),
            ItemOutput.CODEC.fieldOf("output").forGetter(NuclearBlastRecipe::output),
            Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(NuclearBlastRecipe::chance)
    ).apply(inst, NuclearBlastRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, NuclearBlastRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, NuclearBlastRecipe::input,
                    ItemOutput.STREAM_CODEC, NuclearBlastRecipe::output,
                    ByteBufCodecs.DOUBLE, NuclearBlastRecipe::chance,
                    NuclearBlastRecipe::new);

    @Override
    public MapCodec<NuclearBlastRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, NuclearBlastRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
