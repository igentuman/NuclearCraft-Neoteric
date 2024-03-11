package igentuman.nc.client.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.SeparatePerspectiveModel;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;

public class BatteryBlockLoader implements IModelLoader<BatteryBlockLoader.BatteryModelGeometry> {

    public static final ResourceLocation BATTERY_LOADER = new ResourceLocation(MODID, "battery_loader");

    @Override
    public void onResourceManagerReload(IResourceManager iResourceManager) {

    }

    @Override
    public BatteryModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        String side = modelContents.get("textures").getAsJsonObject().get("down").getAsString();
        String up = modelContents.get("textures").getAsJsonObject().get("up").getAsString();

        RenderMaterial sideDefault = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side));
        RenderMaterial sideIn = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side+ "_in"));
        RenderMaterial sideOut = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side+ "_out"));
        RenderMaterial sideNone = ForgeHooksClient.getBlockMaterial(new ResourceLocation(side+ "_non"));

        RenderMaterial topDefault = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up));
        RenderMaterial topIn = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up+ "_in"));
        RenderMaterial topOut = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up+ "_out"));
        RenderMaterial topNone = ForgeHooksClient.getBlockMaterial(new ResourceLocation(up+ "_non"));

        return new BatteryModelGeometry(sideDefault, sideIn, sideOut, sideNone, topDefault, topIn, topOut, topNone);
    }

    public static class BatteryModelGeometry implements IModelGeometry<BatteryModelGeometry> {
        public final RenderMaterial sideDefault;
        public final RenderMaterial sideIn;
        public final RenderMaterial sideOut;
        public final RenderMaterial sideNone;
        public final RenderMaterial topDefault;
        public final RenderMaterial topIn;
        public final RenderMaterial topOut;
        public final RenderMaterial topNone;

        public BatteryModelGeometry(RenderMaterial side, RenderMaterial sideIn, RenderMaterial sideOut, RenderMaterial sideNone,
                                    RenderMaterial top, RenderMaterial topIn, RenderMaterial topOut, RenderMaterial topNone) {
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
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new BatteryBlockBakedModel(modelTransform, spriteGetter, overrides, owner.getCameraTransforms(), this);

        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration iModelConfiguration, Function<ResourceLocation, IUnbakedModel> function, Set<Pair<String, String>> set) {
            return Arrays.asList(sideDefault, topDefault, sideIn, sideOut, sideNone, topIn, topOut, topNone);
        }
    }
}