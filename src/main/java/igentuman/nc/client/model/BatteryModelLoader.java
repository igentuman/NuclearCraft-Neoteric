package igentuman.nc.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

import static igentuman.nc.NuclearCraft.rl;

public class BatteryModelLoader implements IGeometryLoader<BatteryModelLoader.BatteryGeometry> {

    public static final ResourceLocation ID = rl("battery_model");

    private static Material material(String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(path));
    }

    @Override
    public BatteryGeometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        JsonObject textures = json.getAsJsonObject("textures");
        String side = textures.get("down").getAsString();
        String top = textures.get("up").getAsString();
        return new BatteryGeometry(
                material(side), material(side + "_in"), material(side + "_out"), material(side + "_non"),
                material(top), material(top + "_in"), material(top + "_out"), material(top + "_non"));
    }

    public static class BatteryGeometry implements IUnbakedGeometry<BatteryGeometry> {
        public final Material sideDefault, sideIn, sideOut, sideNone;
        public final Material topDefault, topIn, topOut, topNone;

        public BatteryGeometry(Material sideDefault, Material sideIn, Material sideOut, Material sideNone,
                               Material topDefault, Material topIn, Material topOut, Material topNone) {
            this.sideDefault = sideDefault;
            this.sideIn = sideIn;
            this.sideOut = sideOut;
            this.sideNone = sideNone;
            this.topDefault = topDefault;
            this.topIn = topIn;
            this.topOut = topOut;
            this.topNone = topNone;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                               Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                               ItemOverrides overrides) {
            return new BatteryBakedModel(spriteGetter, overrides, context.getTransforms(), this);
        }
    }
}
