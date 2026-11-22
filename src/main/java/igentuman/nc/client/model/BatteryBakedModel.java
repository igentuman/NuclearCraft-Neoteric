package igentuman.nc.client.model;

import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.SideMode;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.client.model.ClientQuadTools.createQuad;
import static igentuman.nc.client.model.ClientQuadTools.v;

public class BatteryBakedModel implements DynamicBlockStateModel {

    public record Sprites(Material.Baked sideDefault, Material.Baked sideIn, Material.Baked sideOut, Material.Baked sideNone,
                          Material.Baked topDefault, Material.Baked topIn, Material.Baked topOut, Material.Baked topNone) {
    }

    private record GeometryKey(BatteryBakedModel model, int config) {
    }

    private static final int DEFAULT_KEY = key(defaultConfig());

    private final Sprites sprites;
    private final Map<Integer, BlockStateModelPart> partCache = new ConcurrentHashMap<>();
    private final BlockStateModelPart defaultPart;

    public BatteryBakedModel(Sprites sprites) {
        this.sprites = sprites;
        this.defaultPart = buildPart(defaultConfig());
        partCache.put(DEFAULT_KEY, defaultPart);
    }

    private static SideMode[] defaultConfig() {
        SideMode[] def = new SideMode[6];
        for (int i = 0; i < 6; i++) def[i] = SideMode.DEFAULT;
        return def;
    }

    private static int key(SideMode[] config) {
        int key = 0;
        for (SideMode mode : config) key = key * 4 + mode.ordinal();
        return key;
    }

    private BlockStateModelPart buildPart(SideMode[] config) {
        float l = 0, r = 1;
        Material.Baked top = switch (config[Direction.UP.ordinal()]) {
            case DISABLED -> sprites.topNone();
            case IN -> sprites.topIn();
            case OUT -> sprites.topOut();
            default -> sprites.topDefault();
        };
        // Faces were emitted unculled before; keep that so neighbours never hide a configured face.
        QuadCollection quads = new QuadCollection.Builder()
                .addUnculledFace(createQuad(v(r, r, r), v(r, r, l), v(l, r, l), v(l, r, r), top))
                .addUnculledFace(createQuad(v(l, l, l), v(r, l, l), v(r, l, r), v(l, l, r), sideSprite(config, Direction.DOWN)))
                .addUnculledFace(createQuad(v(r, r, r), v(r, l, r), v(r, l, l), v(r, r, l), sideSprite(config, Direction.EAST)))
                .addUnculledFace(createQuad(v(l, r, l), v(l, l, l), v(l, l, r), v(l, r, r), sideSprite(config, Direction.WEST)))
                .addUnculledFace(createQuad(v(r, r, l), v(r, l, l), v(l, l, l), v(l, r, l), sideSprite(config, Direction.NORTH)))
                .addUnculledFace(createQuad(v(l, r, r), v(l, l, r), v(r, l, r), v(r, r, r), sideSprite(config, Direction.SOUTH)))
                .build();
        return new SimpleModelWrapper(quads, true, sprites.sideDefault());
    }

    private Material.Baked sideSprite(SideMode[] config, Direction direction) {
        return switch (config[direction.ordinal()]) {
            case DISABLED -> sprites.sideNone();
            case IN -> sprites.sideIn();
            case OUT -> sprites.sideOut();
            default -> sprites.sideDefault();
        };
    }

    private static SideMode[] config(BlockAndTintGetter level, BlockPos pos) {
        SideMode[] config = level.getModelData(pos).get(AbstractStorageBE.SIDE_CONFIG);
        return config == null || config.length != 6 ? null : config;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> parts) {
        SideMode[] config = config(level, pos);
        if (config == null) {
            parts.add(defaultPart);
            return;
        }
        parts.add(partCache.computeIfAbsent(key(config), k -> buildPart(config)));
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        SideMode[] config = config(level, pos);
        return new GeometryKey(this, config == null ? DEFAULT_KEY : key(config));
    }

    @Override
    public Material.Baked particleMaterial() {
        return sprites.sideDefault();
    }

    @Override
    public int materialFlags() {
        return defaultPart.materialFlags();
    }
}
