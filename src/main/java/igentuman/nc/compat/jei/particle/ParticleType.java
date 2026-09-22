package igentuman.nc.compat.jei.particle;

import igentuman.nc.api.particle.ParticleStack;
import mezz.jei.api.ingredients.IIngredientType;

public final class ParticleType {

    public static final IIngredientType<ParticleStack> PARTICLE = () -> ParticleStack.class;

    private ParticleType() {
    }
}
