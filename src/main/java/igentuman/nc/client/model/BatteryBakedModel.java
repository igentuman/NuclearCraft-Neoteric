package igentuman.nc.client.model;

import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.SideMode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static igentuman.nc.client.model.ClientQuadTools.createQuad;
import static igentuman.nc.client.model.ClientQuadTools.v;

public class BatteryBakedModel implements IDynamicBakedModel {

    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ItemOverrides overrides;
    private final ItemTransforms transforms;
    private final BatteryModelLoader.BatteryGeometry geometry;
    private final Map<String, List<BakedQuad>> quadCache = new HashMap<>();
    private List<BakedQuad> defaultQuads;

    public BatteryBakedModel(Function<Material, TextureAtlasSprite> spriteGetter, ItemOverrides overrides,
                             ItemTransforms transforms, BatteryModelLoader.BatteryGeometry geometry) {
        this.spriteGetter = spriteGetter;
        this.overrides = overrides;
        this.transforms = transforms;
        this.geometry = geometry;
    }

    private TextureAtlasSprite sprite(Material material) {
        return spriteGetter.apply(material);
    }

    private List<BakedQuad> buildQuads(SideMode[] config) {
        float l = 0, r = 1;
        TextureAtlasSprite top = switch (config[Direction.UP.ordinal()]) {
            case DISABLED -> sprite(geometry.topNone);
            case IN -> sprite(geometry.topIn);
            case OUT -> sprite(geometry.topOut);
            default -> sprite(geometry.topDefault);
        };
        return List.of(
                createQuad(v(r, r, r), v(r, r, l), v(l, r, l), v(l, r, r), top),
                createQuad(v(l, l, l), v(r, l, l), v(r, l, r), v(l, l, r), sideSprite(config, Direction.DOWN)),
                createQuad(v(r, r, r), v(r, l, r), v(r, l, l), v(r, r, l), sideSprite(config, Direction.EAST)),
                createQuad(v(l, r, l), v(l, l, l), v(l, l, r), v(l, r, r), sideSprite(config, Direction.WEST)),
                createQuad(v(r, r, l), v(r, l, l), v(l, l, l), v(l, r, l), sideSprite(config, Direction.NORTH)),
                createQuad(v(l, r, r), v(l, l, r), v(r, l, r), v(r, r, r), sideSprite(config, Direction.SOUTH))
        );
    }

    private TextureAtlasSprite sideSprite(SideMode[] config, Direction direction) {
        return switch (config[direction.ordinal()]) {
            case DISABLED -> sprite(geometry.sideNone);
            case IN -> sprite(geometry.sideIn);
            case OUT -> sprite(geometry.sideOut);
            default -> sprite(geometry.sideDefault);
        };
    }

    private List<BakedQuad> defaultQuads() {
        if (defaultQuads == null) {
            SideMode[] def = new SideMode[6];
            for (int i = 0; i < 6; i++) def[i] = SideMode.DEFAULT;
            defaultQuads = buildQuads(def);
        }
        return defaultQuads;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                    @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (side != null || (renderType != null && !renderType.equals(RenderType.solid()))) {
            return Collections.emptyList();
        }
        SideMode[] config = extraData.get(AbstractStorageBE.SIDE_CONFIG);
        if (config == null) {
            return defaultQuads();
        }
        StringBuilder key = new StringBuilder(6);
        for (SideMode mode : config) key.append(mode.ordinal());
        return quadCache.computeIfAbsent(key.toString(), k -> buildQuads(config));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return sprite(geometry.sideDefault);
    }

    @NotNull
    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @NotNull
    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }
}
