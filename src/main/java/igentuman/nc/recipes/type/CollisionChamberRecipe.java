package igentuman.nc.recipes.type;

import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;

/**
 * 2 input particles -> up to 4 output particles. Energy + cross section only.
 */
@NothingNullByDefault
public abstract class CollisionChamberRecipe extends ParticleOnlyRecipe {

    public static final String CODE_ID = "collision_chamber";
    public static final int MAX_INPUTS = 2;
    public static final int MAX_OUTPUTS = 4;

    public CollisionChamberRecipe(ResourceLocation id,
                                  ParticleStack[] inputParticles,
                                  ParticleStack[] outputParticles,
                                  long minEnergy,
                                  long maxEnergy,
                                  long energyReleased,
                                  double crossSection) {
        super(id, inputParticles, outputParticles, minEnergy, maxEnergy, energyReleased, crossSection);
    }

    @Override
    public String getCodeId() {
        return CODE_ID;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_controller").get());
    }

    @Override
    public @NotNull String getGroup() {
        return CODE_ID;
    }
}
