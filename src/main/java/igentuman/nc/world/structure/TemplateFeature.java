package igentuman.nc.world.structure;

import com.mojang.serialization.Codec;
import igentuman.nc.setup.level.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class TemplateFeature extends Feature<NoneFeatureConfiguration> {

    protected static final int PLACE_FLAGS = 1 | 16;

    protected TemplateFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    protected static boolean inWasteland(LevelAccessor world, BlockPos pos) {
        return world.getBiome(pos).is(ModBiomes.WASTELAND_TAG);
    }

    protected static StructureTemplateManager templates(LevelAccessor world) {
        return ((WorldGenRegion) world).getLevel().getStructureManager();
    }

    protected static StructurePlaceSettings randomSettings(RandomSource random) {
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        Mirror mirror = random.nextBoolean() ? Mirror.NONE
                : (random.nextBoolean() ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK);
        return new StructurePlaceSettings().setIgnoreEntities(false).setMirror(mirror).setRotation(rotation);
    }

    protected static void placeTemplate(StructureTemplate template, ServerLevelAccessor world, BlockPos pos, StructurePlaceSettings settings, RandomSource random) {
        template.placeInWorld(world, pos, pos, settings, random, PLACE_FLAGS);
    }
}
