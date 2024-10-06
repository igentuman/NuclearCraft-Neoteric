package igentuman.nc.setup.registration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.registries.RegisterEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class WorldGeneration {
   public static final ResourceKey<Biome> WASTELAND_BIOME = makeKey("wasteland");

    private static ResourceKey<Biome> makeKey(String name) {
        return ResourceKey.create(Registries.BIOME, rl(name));
    }
    public static void registerExtraStuff(RegisterEvent evt) {
/*        if (evt.getRegistryKey().equals(Registries.BIOME_SOURCE)) {
            Registry.register(BuiltInRegistries.BIOME_SOURCE, "nuclearcraft_wasteland", WastelandBiomeProvider.CODEC);
        } else if (evt.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
            Registry.register(BuiltInRegistries.CHUNK_GENERATOR, "nuclearcraft_wasteland", NuclearcraftChunkGenerator.CODEC);
        }*/
    }
    public static class StructureLoader {
        private static final String STRUCTURE_PATH = ":structures/fission_reactor   ";

        public static StructureTemplate loadStructure(ServerLevel level, ResourceLocation structureLocation) {
            StructureTemplateManager manager = level.getStructureManager();
            return manager.get(structureLocation).orElse(null);
        }
    }

    public static class StructurePlacer {
        public static void placeStructure(ServerLevel level, BlockPos pos, String name) {
            // Load the structure
            ResourceLocation structureLocation = new ResourceLocation(MODID, name);
            StructureTemplate template = StructureLoader.loadStructure(level, structureLocation);

            if (template == null) {
                // Handle structure not found
                System.out.println("Structure not found: " + structureLocation);
                return;
            }

            // Define the placement settings
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false);

            // Place the structure in the world
            template.placeInWorld(level, pos, pos, settings, level.random, 2);
        }
    }
}
