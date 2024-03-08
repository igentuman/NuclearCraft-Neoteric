package igentuman.nc.setup.registration;

import com.mojang.serialization.Codec;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.client.particle.FusionBeamParticleType;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import static igentuman.nc.NuclearCraft.rl;

public class NcParticleTypes {

    private NcParticleTypes() {
    }
/*
    public static final ParticleType<SimpleParticleType> RADIATION = registerParticleType("radiation");
    public static final ParticleType<FusionBeamParticleType> FUSION_BEAM = registerParticleType("fusion_beam", FusionBeamParticleType::new);

    private static <T extends ParticleType<?>> ParticleType<T> registerParticleType(String path)
    {
        ResourceLocation name = rl(path);
        return Registry.register(Registry.PARTICLE_TYPE, name, new ParticleType<T>()
        {
            @Override
            public Codec<T> codec() {
                return (T) -> T.CODEC;
            }

            @Override
            public String toString()
            {
                return name.toString();
            }
        });
    }*/
}