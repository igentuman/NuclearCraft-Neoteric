package igentuman.nc.client.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;

public class BatteryBlockLoader implements IModelLoader<BatteryBlockLoader.BatteryModelGeometry> {

    public static final ResourceLocation BATTERY_LOADER = new ResourceLocation(MODID, "battery_loader");

    @Override
    public BatteryModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        String side = modelContents.get("textures").getAsJsonObject().get("down").getAsString();
        String up = modelContents.get("textures").getAsJsonObject().get("up").getAsString();

        Material sideDefault = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side));
        Material sideIn = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side+ "_in"));
        Material sideOut = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side+ "_out"));
        Material sideNone = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side+ "_non"));

        Material topDefault = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up));
        Material topIn = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up+ "_in"));
        Material topOut = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up+ "_out"));
        Material topNone = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up+ "_non"));

        return new BatteryModelGeometry(sideDefault, sideIn, sideOut, sideNone, topDefault, topIn, topOut, topNone);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    public static class BatteryModelGeometry implements IModelGeometry<BatteryModelGeometry> {
        public final Material sideDefault;
        public final Material sideIn;
        public final Material sideOut;
        public final Material sideNone;
        public final Material topDefault;
        public final Material topIn;
        public final Material topOut;
        public final Material topNone;

        public BatteryModelGeometry(Material side, Material sideIn, Material sideOut, Material sideNone,
                                    Material top, Material topIn, Material topOut, Material topNone) {
            this.sideDefault = side;
            this.topDefault = top;
            this.sideIn = sideIn;
            this.sideOut = sideOut;
            this.sideNone = sideNone;
            this.topIn = topIn;
            this.topOut = topOut;
            this.topNone = topNone;
        }

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new BatteryBlockBakedModel(modelTransform, spriteGetter, overrides, owner.getCameraTransforms(), this);
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return List.of(sideDefault, topDefault, sideIn, sideOut, sideNone, topIn, topOut, topNone);
        }
    }
}