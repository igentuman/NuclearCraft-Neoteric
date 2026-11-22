package igentuman.nc.client.model;

import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import igentuman.nc.registration.ModEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.NuclearCraft.rl;

public final class SideConfigFaces {

    private static final Identifier DEFAULT_MODEL = rl("side_config/default");
    private static final Map<Identifier, StandaloneModelKey<QuadCollection>> KEYS = new ConcurrentHashMap<>();
    private static volatile java.util.Set<Identifier> registered = java.util.Set.of();

    private SideConfigFaces() {}

    private static StandaloneModelKey<QuadCollection> key(Identifier modelId) {
        return KEYS.computeIfAbsent(modelId, id -> new StandaloneModelKey<>(id::toString));
    }

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        java.util.Set<Identifier> ids = new java.util.HashSet<>();
        ids.add(DEFAULT_MODEL);
        Minecraft.getInstance().getResourceManager()
                .listResources("models/side_config", location -> location.getPath().endsWith(".json"))
                .keySet().forEach(location -> ids.add(
                        location.withPath(path -> path.substring("models/".length(), path.length() - ".json".length()))));
        for (Identifier id : ids) {
            event.register(key(id), SimpleUnbakedStandaloneModel.quadCollection(id));
        }
        registered = java.util.Set.copyOf(ids);
    }

    public static TextureAtlasSprite getFace(ModEntry entry, RelativeDirection relativeDirection) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager models = minecraft.getModelManager();
        Direction face = RelativeDirection.toAbsolute(relativeDirection, Direction.NORTH);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(entry.block().get());
        QuadCollection override = standalone(models, blockId.withPrefix("side_config/"));
        if (override != null) {
            TextureAtlasSprite sprite = findFace(override.getQuads(face), override.getQuads(null), face);
            if (sprite != null) return sprite;
        }

        if (!entry.customModel()) {
            BlockState state = entry.block().get().defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            }
            List<BlockStateModelPart> parts = new ArrayList<>();
            models.getBlockStateModelSet().get(state)
                    .collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RandomSource.create(42), parts);
            List<BakedQuad> culled = new ArrayList<>();
            List<BakedQuad> unculled = new ArrayList<>();
            for (BlockStateModelPart part : parts) {
                culled.addAll(part.getQuads(face));
                unculled.addAll(part.getQuads(null));
            }
            TextureAtlasSprite sprite = findFace(culled, unculled, face);
            if (sprite != null) return sprite;
        }

        QuadCollection fallback = standalone(models, DEFAULT_MODEL);
        if (fallback != null) {
            TextureAtlasSprite sprite = findFace(fallback.getQuads(face), fallback.getQuads(null), face);
            if (sprite != null) return sprite;
        }
        return models.getBlockStateModelSet().missingModel().particleMaterial().sprite();
    }

    @Nullable
    private static QuadCollection standalone(ModelManager models, Identifier modelId) {
        if (!registered.contains(modelId)) return null;
        return models.getStandaloneModel(key(modelId));
    }

    @Nullable
    private static TextureAtlasSprite findFace(List<BakedQuad> culled, List<BakedQuad> unculled, Direction face) {
        List<BakedQuad> quads = new ArrayList<>(culled);
        for (BakedQuad quad : unculled) {
            if (quad.direction() == face) quads.add(quad);
        }
        return quads.size() == 1 ? quads.getFirst().materialInfo().sprite() : null;
    }
}
