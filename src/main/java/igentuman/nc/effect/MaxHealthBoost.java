package igentuman.nc.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// Adds flat max-health via an attribute modifier (+4 HP per amplifier level), mirroring vanilla HealthBoost.
public class MaxHealthBoost extends MobEffect {

    private static final String MODIFIER_UUID = "5d6f1c84-3a2e-4b71-9f0c-2e8a7b134d62";

    public MaxHealthBoost(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MAX_HEALTH, MODIFIER_UUID, 4.0D, AttributeModifier.Operation.ADDITION);
    }
}
