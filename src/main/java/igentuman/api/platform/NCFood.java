package igentuman.api.platform;

import net.minecraft.world.food.FoodProperties;

/**
 * Platform wrapper for FoodProperties.Builder APIs.
 * In 1.21.1: saturationMod() → saturationModifier(), alwaysEat() → alwaysEdible().
 */
public final class NCFood {
    private NCFood() {}

    public static FoodProperties simple(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build();
    }

    public static FoodProperties alwaysEdible(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .alwaysEdible()
                .build();
    }
}
