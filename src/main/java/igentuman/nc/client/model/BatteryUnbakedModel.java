package igentuman.nc.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import static igentuman.nc.NuclearCraft.rl;

public record BatteryUnbakedModel(Identifier side, Identifier top) implements CustomUnbakedBlockStateModel {

    public static final Identifier ID = rl("battery");

    public static final MapCodec<BatteryUnbakedModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("side").forGetter(BatteryUnbakedModel::side),
            Identifier.CODEC.fieldOf("top").forGetter(BatteryUnbakedModel::top)
    ).apply(i, BatteryUnbakedModel::new));

    @Override
    public MapCodec<BatteryUnbakedModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        // Built from sprites only; no model dependencies.
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        MaterialBaker materials = baker.materials();
        ModelDebugName name = () -> ID + "[" + side + "]";
        return new BatteryBakedModel(new BatteryBakedModel.Sprites(
                bake(materials, name, side, ""), bake(materials, name, side, "_in"),
                bake(materials, name, side, "_out"), bake(materials, name, side, "_non"),
                bake(materials, name, top, ""), bake(materials, name, top, "_in"),
                bake(materials, name, top, "_out"), bake(materials, name, top, "_non")));
    }

    private static Material.Baked bake(MaterialBaker materials, ModelDebugName name, Identifier base, String suffix) {
        return materials.get(new Material(base.withSuffix(suffix)), name);
    }
}
