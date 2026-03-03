package igentuman.nc.world.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import igentuman.api.platform.NCRegistration;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.FEATURE_REGISTER;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class WastelandBossLairFeature extends Feature<NoneFeatureConfiguration> {
    public static final DeferredHolder<Feature<?>, WastelandBossLairFeature> WASTELAND_BOSS_LAIR_FEATURE =
            NCRegistration.registerFeature(FEATURE_REGISTER, "wasteland_boss_lair",
                    () -> new WastelandBossLairFeature(NoneFeatureConfiguration.CODEC));

    public WastelandBossLairFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void init() {
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        LevelAccessor world = context.level();
        BlockPos pos = context.origin();

        if (!world.getBiome(pos).is(WASTELAND)) {
            return false;
        }
        
        StructureTemplateManager templateManager = ((WorldGenRegion) world).getLevel().getStructureManager();

        // Get the structure template
        StructureTemplate template = templateManager.get(rl("wasteland_boss_lair")).orElse(null);
        if (template == null) {
            debugLog("Failed to load structure template: boss_lair");
            return false;
        }
        
        net.minecraft.world.level.block.Rotation[] rotations = net.minecraft.world.level.block.Rotation.values();
        net.minecraft.world.level.block.Rotation randomRotation = rotations[context.random().nextInt(rotations.length)];
        
        net.minecraft.world.level.block.Mirror randomMirror = context.random().nextBoolean()
                ? net.minecraft.world.level.block.Mirror.NONE 
                : (context.random().nextBoolean() ? net.minecraft.world.level.block.Mirror.LEFT_RIGHT : net.minecraft.world.level.block.Mirror.FRONT_BACK);
        
        StructurePlaceSettings placeSettings = new StructurePlaceSettings()
                .setIgnoreEntities(false)
                .setMirror(randomMirror)
                .setRotation(randomRotation);
        final int BLOCK_UPDATE = 1;
        final int KEEP_EXISTING = 16;

        // Place the structure in the world
        template.placeInWorld((ServerLevelAccessor) world, pos.below(), pos.below(), placeSettings, world.getRandom(), BLOCK_UPDATE | KEEP_EXISTING);
        return true;
    }
}