package igentuman.nc.world.biome;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.registries.DeferredRegister;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class NCDensityFunction {
    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registry.DENSITY_FUNCTION_TYPE_REGISTRY, MODID);
    public static final ResourceKey<DensityFunction> WASTELAND_TERRAIN = ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, rl("wasteland"));
}
