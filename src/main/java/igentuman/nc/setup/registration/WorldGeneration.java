package igentuman.nc.setup.registration;

import com.mojang.serialization.Codec;
import igentuman.nc.world.BiomeFilterNether;
import igentuman.nc.world.OrePlacementModifier;
import igentuman.nc.world.structure.WastelandBossLairFeature;
import igentuman.nc.world.structure.WastelandPortalFeature;
import igentuman.nc.world.structure.WastelandStructureFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.PLACEMENT_MODIFIERS;

public class WorldGeneration {
    public static final TagKey<Biome> WASTELAND = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), rl("wasteland"));

    public static final ResourceKey<Biome> WASTELAND_BIOME = makeKey("wasteland");

    public static final RegistryObject<PlacementModifierType<?>> NC_ORE_MODIFIER =
            PLACEMENT_MODIFIERS.register("nc_ore_modifier", () -> placement(OrePlacementModifier.CODEC));

    public static final RegistryObject<PlacementModifierType<?>> VEGETATION_MODIFIER =
            PLACEMENT_MODIFIERS.register("nc_vegetation_modifier", () -> placement(BiomeFilterNether.CODEC));

    private static ResourceKey<Biome> makeKey(String name) {
        return ResourceKey.create(Registries.BIOME, rl(name));
    }

    public static <P extends PlacementModifier> PlacementModifierType<P> placement(Codec<P> codec) {
        return () -> codec;
    }

    public static void init() {
        WastelandStructureFeature.init();
        WastelandPortalFeature.init();
        WastelandBossLairFeature.init();
    }

    public static class StructureLoader {

        public static StructureTemplate loadStructure(ServerLevel level, ResourceLocation structureLocation) {
            StructureTemplateManager manager = level.getStructureManager();
            return manager.get(structureLocation).orElse(null);
        }
    }

    public static class StructurePlacer {
        public static void placeStructure(ServerLevel level, BlockPos pos, String name) {
            ResourceLocation structureLocation = rl(name);
            StructureTemplate template = StructureLoader.loadStructure(level, structureLocation);

            if (template == null) {
                System.out.println("Structure not found: " + structureLocation);
                return;
            }

            // Define the placement settings
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false);
            template.placeInWorld(level, pos, pos, settings, level.random, 2);
        }
    }
}
