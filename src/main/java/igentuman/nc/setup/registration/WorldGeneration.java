package igentuman.nc.setup.registration;

import com.mojang.serialization.Codec;
import igentuman.nc.world.BiomeFilterNether;
import igentuman.nc.world.OrePlacementModifier;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class WorldGeneration {
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(Registries.BIOME, MODID);
    public static final TagKey<Biome> WASTELAND = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), rl("wasteland"));

    public static final ResourceKey<Biome> WASTELAND_BIOME = makeKey("wasteland");

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);

    public static final RegistryObject<PlacementModifierType<?>> NC_ORE_MODIFIER =
            PLACEMENT_MODIFIERS.register("nc_ore_modifier", () -> placement(OrePlacementModifier.CODEC));

    public static final RegistryObject<PlacementModifierType<?>> VEGETATION_MODIFIER =
            PLACEMENT_MODIFIERS.register("nc_vegetation_modifier", () -> placement(BiomeFilterNether.CODEC));

    private static ResourceKey<Biome> makeKey(String name) {
        return ResourceKey.create(Registries.BIOME, rl(name));
    }

    public static void register(IEventBus eventBus) {
        PLACEMENT_MODIFIERS.register(eventBus);
        BIOMES.register(eventBus);
    }

    public static <P extends PlacementModifier> PlacementModifierType<P> placement(Codec<P> codec) {
        return () -> codec;
    }

    public static void init() {
        WastelandStructureFeature.init();
    }

    public static class StructureLoader {

        public static StructureTemplate loadStructure(ServerLevel level, ResourceLocation structureLocation) {
            StructureTemplateManager manager = level.getStructureManager();
            return manager.get(structureLocation).orElse(null);
        }
    }

    public static class StructurePlacer {
        public static void placeStructure(ServerLevel level, BlockPos pos, String name) {
            // Load the structure
            ResourceLocation structureLocation = rl(name);
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
