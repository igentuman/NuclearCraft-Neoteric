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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.FEATURE_REGISTER;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class WastelandPortalFeature extends Feature<NoneFeatureConfiguration> {
    public static final DeferredHolder<Feature<?>, WastelandPortalFeature> WASTELAND_PORTAL_FEATURE =
            NCRegistration.registerFeature(FEATURE_REGISTER, "wasteland_portal",
                    () -> new WastelandPortalFeature(NoneFeatureConfiguration.CODEC));

    public WastelandPortalFeature(Codec<NoneFeatureConfiguration> codec) {
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
        StructureTemplate template = templateManager.get(rl("wasteland/portal_10")).orElse(null);
        if (template == null) {
            debugLog("Failed to load structure template: portal_10");
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


        // Calculate average ground height
        template.placeInWorld((ServerLevelAccessor) world, pos.below(), pos.below(), placeSettings, world.getRandom(), BLOCK_UPDATE | KEEP_EXISTING);
        return true;
    }

}