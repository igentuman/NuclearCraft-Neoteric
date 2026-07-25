package igentuman.nc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public class OreVeinRecipeSerializer implements RecipeSerializer<OreVeinRecipe> {

    public static final record OreEntryJson(SizedIngredient item, int weight) {}

    private final MapCodec<OreVeinRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, OreVeinRecipe> streamCodec;

    private static final Codec<OreEntryJson> ORE_ENTRY_CODEC = RecordCodecBuilder.create(e -> e.group(
            SizedIngredient.FLAT_CODEC.fieldOf("item").forGetter(OreEntryJson::item),
            Codec.INT.fieldOf("weight").forGetter(OreEntryJson::weight)
    ).apply(e, OreEntryJson::new));

    public OreVeinRecipeSerializer() {
        this.codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(OreVeinRecipe::getId),
                ORE_ENTRY_CODEC.listOf().fieldOf("ores").forGetter(r -> r.getOres().stream().map(oe -> new OreEntryJson(oe.ingredient(), oe.weight())).toList()),
                Codec.DOUBLE.optionalFieldOf("rarity", 1.0).forGetter(OreVeinRecipe::getRarityModifier)
        ).apply(inst, (id, oreJsons, rarity) -> {
            List<OreVeinRecipe.OreEntry> ores = new ArrayList<>();
            for (OreEntryJson oj : oreJsons) {
                ores.add(new OreVeinRecipe.OreEntry(oj.item(), oj.weight()));
            }
            return new OreVeinRecipe(id, ores, rarity);
        }));

        this.streamCodec = new StreamCodec<>() {
            private final StreamCodec<RegistryFriendlyByteBuf, OreEntryJson> ENTRY_STREAM =
                    StreamCodec.composite(
                            SizedIngredient.STREAM_CODEC, OreEntryJson::item,
                            ByteBufCodecs.INT, OreEntryJson::weight,
                            OreEntryJson::new);

            private final StreamCodec<RegistryFriendlyByteBuf, List<OreEntryJson>> ENTRY_LIST =
                    ENTRY_STREAM.apply(ByteBufCodecs.list());

            @Override
            public OreVeinRecipe decode(RegistryFriendlyByteBuf buf) {
                ResourceLocation id = buf.readResourceLocation();
                List<OreEntryJson> oreJsons = ENTRY_LIST.decode(buf);
                double rarity = buf.readDouble();
                List<OreVeinRecipe.OreEntry> ores = new ArrayList<>();
                for (OreEntryJson oj : oreJsons) {
                    ores.add(new OreVeinRecipe.OreEntry(oj.item(), oj.weight()));
                }
                return new OreVeinRecipe(id, ores, rarity);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, OreVeinRecipe recipe) {
                buf.writeResourceLocation(recipe.getId());
                ENTRY_LIST.encode(buf, recipe.getOres().stream().map(oe -> new OreEntryJson(oe.ingredient(), oe.weight())).toList());
                buf.writeDouble(recipe.getRarityModifier());
            }
        };
    }

    @Override
    public MapCodec<OreVeinRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, OreVeinRecipe> streamCodec() {
        return streamCodec;
    }
}
