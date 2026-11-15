package igentuman.nc.client.model;

import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import igentuman.nc.registration.ModEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static igentuman.nc.NuclearCraft.rl;

public final class SideConfigFaces {

    private static final ModelResourceLocation DEFAULT_MODEL = ModelResourceLocation.standalone(rl("side_config/default"));

    private SideConfigFaces() {}

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(DEFAULT_MODEL);
        Minecraft.getInstance().getResourceManager()
                .listResources("models/side_config", location -> location.getPath().endsWith(".json"))
                .keySet().forEach(location -> event.register(ModelResourceLocation.standalone(
                        location.withPath(path -> path.substring("models/".length(), path.length() - ".json".length())))));
    }

    public static TextureAtlasSprite getFace(ModEntry entry, RelativeDirection relativeDirection) {
        Minecraft minecraft = Minecraft.getInstance();
        var models = minecraft.getModelManager();
        Direction face = RelativeDirection.toAbsolute(relativeDirection, Direction.NORTH);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(entry.block().get());
        BakedModel override = models.getModel(ModelResourceLocation.standalone(blockId.withPrefix("side_config/")));
        if (override != models.getMissingModel()) {
            TextureAtlasSprite sprite = findFace(override, null, face);
            if (sprite != null) return sprite;
        }

        if (!entry.customModel()) {
            BlockState state = entry.block().get().defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            }
            TextureAtlasSprite sprite = findFace(minecraft.getBlockRenderer().getBlockModel(state), state, face);
            if (sprite != null) return sprite;
        }

        BakedModel fallback = models.getModel(DEFAULT_MODEL);
        TextureAtlasSprite sprite = findFace(fallback, null, face);
        return sprite != null ? sprite : models.getMissingModel().getParticleIcon(ModelData.EMPTY);
    }

    @Nullable
    private static TextureAtlasSprite findFace(BakedModel model, @Nullable BlockState state, Direction face) {
        RandomSource random = RandomSource.create(42);
        var quads = new ArrayList<>(model.getQuads(state, face, random, ModelData.EMPTY, null));
        random.setSeed(42);
        for (BakedQuad quad : model.getQuads(state, null, random, ModelData.EMPTY, null)) {
            if (quad.getDirection() == face) quads.add(quad);
        }
        return quads.size() == 1 ? quads.getFirst().getSprite() : null;
    }
}
