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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.setup.registration.Registries.FEATURE_REGISTER;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class WastelandStructureFeature extends Feature<NoneFeatureConfiguration> {
    public static final List<ResourceLocation> structures = new ArrayList<>();
    public static final DeferredHolder<Feature<?>, WastelandStructureFeature> WASTELAND_RUINS_FEATURE =
            NCRegistration.registerFeature(FEATURE_REGISTER, "wasteland_ruins",
                    () -> new WastelandStructureFeature(NoneFeatureConfiguration.CODEC));

    public WastelandStructureFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void init() {
    }
    
    /**
     * Finds all wasteland structures available in the datapack
     * @param templateManager The structure template manager
     * @return A list of resource locations for wasteland structures
     */
    private List<ResourceLocation> findWastelandStructures(StructureTemplateManager templateManager) {
        if(!structures.isEmpty()) {
            return structures;
        }
        
        String wastelandPath = "nuclearcraft:wasteland/";
        
        Stream<ResourceLocation> allTemplates = templateManager.listTemplates();
        
        // Filter for wasteland structures
        for (ResourceLocation location : allTemplates.toList()) {
            String path = location.getPath();
            if (path.startsWith("wasteland/")) {
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                int rarityValue = 0;
                if (fileName.matches(".*\\d+$")) {
                    String rarityStr = fileName.replaceAll(".*?([0-9]+)$", "$1");
                    rarityValue = Integer.parseInt(rarityStr);
                }
                int rarityIndex = 11 - Math.abs(rarityValue);
                for(int i = 0; i < rarityIndex; i++) {
                    structures.add(ResourceLocation.tryParse(location.toString()));
                }
                structures.add(location);
            }
        }
        
        // If no structures found, try with the mod ID prefix
        if (structures.isEmpty()) {
            for (ResourceLocation location : allTemplates.toList()) {
                String fullPath = location.toString();
                if (fullPath.contains(wastelandPath)) {
                    String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
                    int rarityValue = 0;
                    if (fileName.matches(".*\\d+$")) {
                        String rarityStr = fileName.replaceAll(".*?([0-9]+)$", "$1");
                        rarityValue = Integer.parseInt(rarityStr);
                    }
                    int rarityIndex = 11 - Math.abs(rarityValue);
                    for(int i = 0; i < rarityIndex; i++) {
                        structures.add(ResourceLocation.tryParse(location.toString()));
                    }
                }
            }
        }
        
        // Log the found structures
        if (!structures.isEmpty()) {
            debugLog("Wasteland structures pool size: " + structures.size());
        }
        
        return structures;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        LevelAccessor world = context.level();
        BlockPos pos = context.origin();

        if (!world.getBiome(pos).is(WASTELAND)) {
            return false;
        }
        
        StructureTemplateManager templateManager = ((WorldGenRegion) world).getLevel().getStructureManager();
        
        List<ResourceLocation> availableStructures = findWastelandStructures(templateManager);
        
        if (availableStructures.isEmpty()) {
            debugLog("No wasteland structures found in datapack");
            return false;
        }
        
        // Select a random structure
        int id = context.random().nextInt(availableStructures.size());
        ResourceLocation structureId = availableStructures.get(id);
        
        // Get the structure template
        StructureTemplate template = templateManager.get(structureId).orElse(null);
        if (template == null) {
            debugLog("Failed to load structure template: " + structureId);
            return false;
        }
        
        String featureName = structureId.getPath();
        if (featureName.contains("/")) {
            featureName = featureName.substring(featureName.lastIndexOf('/') + 1);
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
        int vSize = template.getSize().getY();
        final int BLOCK_UPDATE = 1;
        final int KEEP_EXISTING = 16;
        
        final int STRUCTURE_SIZE = 7;
        
        int totalHeight = 0;
        int sampleCount = 0;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        
        int sampleStep = 2;
        for (int x = 0; x < STRUCTURE_SIZE; x += sampleStep) {
            for (int z = 0; z < STRUCTURE_SIZE; z += sampleStep) {
                BlockPos checkPos = pos.offset(x, 0, z);
                int groundY = findGroundLevel(world, checkPos);
                if (groundY > 0) {
                    totalHeight += groundY;
                    sampleCount++;
                    minHeight = Math.min(minHeight, groundY);
                    maxHeight = Math.max(maxHeight, groundY);
                }
            }
        }
        
        if (sampleCount == 0) return false;
        
        // Calculate average ground height
        int avgHeight = totalHeight / sampleCount;

        if(featureName.contains("bunker")) {
            avgHeight -= (vSize-2);
        }
        avgHeight -= 2;
        if (maxHeight - minHeight > 5) {
            return false;
        }
        
        BlockPos placementPos = new BlockPos(pos.getX(), avgHeight, pos.getZ());
        
        template.placeInWorld((ServerLevelAccessor) world, placementPos, placementPos, placeSettings, world.getRandom(), BLOCK_UPDATE | KEEP_EXISTING);
        return true;
    }
    
    /**
     * Finds the ground level at the given position
     * @param world The world
     * @param pos The position to check
     * @return The Y coordinate of the ground, or -1 if not found
     */
    private int findGroundLevel(LevelAccessor world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), world.getMaxBuildHeight(), pos.getZ());

        while (mutablePos.getY() > world.getMinBuildHeight() &&
               (world.isEmptyBlock(mutablePos))) {
            mutablePos.move(0, -1, 0);
        }
        
        if (mutablePos.getY() <= world.getMinBuildHeight()) {
            return -1;
        }
        
        return mutablePos.getY();
    }
}