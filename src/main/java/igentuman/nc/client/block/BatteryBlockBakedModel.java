package igentuman.nc.client.block;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.util.ClientTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static igentuman.nc.util.ClientTools.v;

public class BatteryBlockBakedModel implements IDynamicBakedModel {

    private final IModelTransform modelState;
    private final Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
    private final Map<String, List<BakedQuad>> quadCache = new HashMap<>();
    private final ItemOverrideList overrides;
    private final ItemCameraTransforms itemTransforms;
    public BatteryBlockLoader.BatteryModelGeometry batteryModelGeometry;

    public BatteryBlockBakedModel(IModelTransform modelState, Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
                                  ItemOverrideList overrides, ItemCameraTransforms itemTransforms,
                                  BatteryBlockLoader.BatteryModelGeometry batteryModelGeometry) {
        this.modelState = modelState;
        this.spriteGetter = spriteGetter;
        this.overrides = overrides;
        this.itemTransforms = itemTransforms;
        this.batteryModelGeometry = batteryModelGeometry;

        float l = 0;
        float r = 1;
        float p = 1;

        TransformationMatrix rotation = modelState.getRotation();

        TextureAtlasSprite textureSide = spriteGetter.apply(batteryModelGeometry.sideDefault);
        TextureAtlasSprite textureTop = spriteGetter.apply(batteryModelGeometry.topDefault);

        sideQuads = Arrays.asList(
                ClientTools.createQuad(v(r, p, r), v(r, p, l), v(l, p, l), v(l, p, r), rotation, textureTop),
                ClientTools.createQuad(v(l, l, l), v(r, l, l), v(r, l, r), v(l, l, r), rotation, textureSide),
                ClientTools.createQuad(v(r, p, r), v(r, l, r), v(r, l, l), v(r, p, l), rotation, textureSide),
                ClientTools.createQuad(v(l, p, l), v(l, l, l), v(l, l, r), v(l, p, r), rotation, textureSide),
                ClientTools.createQuad(v(r, p, l), v(r, l, l), v(l, l, l), v(l, p, l), rotation, textureSide),
                ClientTools.createQuad(v(l, p, r), v(l, l, r), v(r, l, r), v(r, p, r), rotation, textureSide)
        );
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    public List<BakedQuad> sideQuads;
    /**
     * @param state the blockstate for our block
     * @param side the six directions or null for quads that are not at a specific direction
     * @param rand random generator that you can use to add variations to your model (usually for textures)
     * @param extraData this represents the data that is given to use from our block entity
     * @return a list of quads
     */
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        RenderType layer = MinecraftForgeClient.getRenderLayer();

        if (side != null || (layer != null && !layer.equals(RenderType.solid()))) {
            return Collections.emptyList();
        }
        HashMap<Integer, ISizeToggable.SideMode> sideConfig = extraData.getData(BatteryBE.SIDE_CONFIG);
        if(sideConfig == null) {
            return sideQuads;
        }
        String cacheKey = keyFor(sideConfig.values());
        if(quadCache.containsKey(cacheKey)) {
            return quadCache.get(cacheKey);
        }

        float l = 0;
        float r = 1;
        float p = 1;

        TransformationMatrix rotation = modelState.getRotation();


        TextureAtlasSprite textureTop;
        switch (sideConfig.get(Direction.UP.ordinal())) {
            case DISABLED:
                textureTop = spriteGetter.apply(batteryModelGeometry.topNone);
                break;
            case IN:
                textureTop = spriteGetter.apply(batteryModelGeometry.topIn);
                break;
            case OUT:
                textureTop = spriteGetter.apply(batteryModelGeometry.topOut);
                break;
            default:
                textureTop = spriteGetter.apply(batteryModelGeometry.topDefault);
        }
        quadCache.put(cacheKey,
            Arrays.asList(
                    ClientTools.createQuad(v(r, p, r), v(r, p, l), v(l, p, l), v(l, p, r), rotation, textureTop),
                    ClientTools.createQuad(v(l, l, l), v(r, l, l), v(r, l, r), v(l, l, r), rotation, getSideTexture(sideConfig, Direction.DOWN)),
                    ClientTools.createQuad(v(r, p, r), v(r, l, r), v(r, l, l), v(r, p, l), rotation, getSideTexture(sideConfig, Direction.EAST)),
                    ClientTools.createQuad(v(l, p, l), v(l, l, l), v(l, l, r), v(l, p, r), rotation, getSideTexture(sideConfig, Direction.WEST)),
                    ClientTools.createQuad(v(r, p, l), v(r, l, l), v(l, l, l), v(l, p, l), rotation, getSideTexture(sideConfig, Direction.NORTH)),
                    ClientTools.createQuad(v(l, p, r), v(l, l, r), v(r, l, r), v(r, p, r), rotation, getSideTexture(sideConfig, Direction.SOUTH))
        ));

        return quadCache.get(cacheKey);
    }

    private String keyFor(Collection<ISizeToggable.SideMode> values) {
        String result = "";
        for(ISizeToggable.SideMode value : values) {
            result += value.ordinal();
        }
        return result;
    }

    private TextureAtlasSprite getSideTexture(HashMap<Integer, ISizeToggable.SideMode> sideConfig, Direction direction) {
        TextureAtlasSprite textureSide = spriteGetter.apply(batteryModelGeometry.sideDefault);
        switch (sideConfig.get(direction.ordinal())) {
            case DISABLED:
                textureSide = spriteGetter.apply(batteryModelGeometry.sideNone);
                break;
            case IN:
                textureSide = spriteGetter.apply(batteryModelGeometry.sideIn);
                break;
            case OUT:
                textureSide = spriteGetter.apply(batteryModelGeometry.sideOut);
        }
        return textureSide;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return spriteGetter.apply(batteryModelGeometry.sideDefault);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return itemTransforms;
    }
}