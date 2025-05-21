package igentuman.nc.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class NCDensityFunction {
    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MODID);
    public static final ResourceKey<DensityFunction> WASTELAND_TERRAIN = ResourceKey.create(Registries.DENSITY_FUNCTION, rl("wasteland"));

    public static void bootstrap(BootstapContext<DensityFunction> context) {
        DensityFunction baseNoise = DensityFunctions.noise(
                context.lookup(Registries.NOISE).getOrThrow(Noises.SURFACE),
                0.5, 0.75
        );

        DensityFunction secondaryNoise = DensityFunctions.noise(
                context.lookup(Registries.NOISE).getOrThrow(Noises.SURFACE_SECONDARY),
                0.4, 0.7
        );

        // Add varied terrain height
        DensityFunction terrainHeight = DensityFunctions.add(
                DensityFunctions.constant(0.4),
                DensityFunctions.mul(
                        DensityFunctions.add(baseNoise, secondaryNoise),
                        DensityFunctions.constant(0.3)
                )
        );

        // Create final terrain shape with irregular features
        DensityFunction finalTerrain = DensityFunctions.add(
                terrainHeight,
                DensityFunctions.noise(
                        context.lookup(Registries.NOISE).getOrThrow(Noises.BADLANDS_SURFACE),
                        0.2, 0.5
                )
        );


        // Register the density function
        context.register(WASTELAND_TERRAIN, finalTerrain, Lifecycle.stable());
    }
}
