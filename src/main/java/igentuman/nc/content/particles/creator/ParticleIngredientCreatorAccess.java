package igentuman.nc.content.particles.creator;

import igentuman.nc.NuclearCraft;

import java.util.function.Consumer;

/**
 * Provides access to helpers for creating particle ingredients.
 */
public class ParticleIngredientCreatorAccess {

    private ParticleIngredientCreatorAccess() {
    }

    private static IParticleIngredientCreator PARTICLE_INGREDIENT_CREATOR;

    /**
     * Gets the particle ingredient creator.
     */
    public static IParticleIngredientCreator particle() {
        if (PARTICLE_INGREDIENT_CREATOR == null) {
            lookupInstance(IParticleIngredientCreator.class, "igentuman.nc.content.particles.creator.ParticleIngredientCreator",
                  helper -> PARTICLE_INGREDIENT_CREATOR = helper);
        }
        return PARTICLE_INGREDIENT_CREATOR;
    }

    private static <TYPE extends IIngredientCreator<?, ?, ?>> void lookupInstance(Class<TYPE> type, String className, Consumer<TYPE> setter) {
        try {
            Class<?> clazz = Class.forName(className);
            setter.accept(type.cast(clazz.getField("INSTANCE").get(null)));
        } catch (ReflectiveOperationException ex) {
            NuclearCraft.LOGGER.error("Error retrieving {}, Nuclearcraft may be absent, damaged, or outdated.", className);
        }
    }
}