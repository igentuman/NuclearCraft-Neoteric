package igentuman.nc.setup.registration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.RegisterEvent;

import static igentuman.nc.NuclearCraft.rl;

public class WorldGeneration {
   // public static final ResourceKey<Biome> WASTELAND_BIOME = makeKey("wasteland");

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
}
