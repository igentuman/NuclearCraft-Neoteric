package igentuman.nc.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

/** Fluent registry of mod damage types, emitted to the datapack registry and resolved into damage sources. */
public class NCDamageSources {

    private static final Map<ResourceKey<DamageType>, DamageType> DEFINITIONS = new LinkedHashMap<>();

    public static final ResourceKey<DamageType> ACID = builder("acid")
            .scaling(DamageScaling.ALWAYS)
            .exhaustion(0.1F)
            .register();

    public static final ResourceKey<DamageType> Q36 = builder("q36")
            .scaling(DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER)
            .register();

    public static final ResourceKey<DamageType> RADIATION = builder("radiation")
            .scaling(DamageScaling.NEVER)
            .exhaustion(0.0F)
            .effects(DamageEffects.HURT)
            .register();

    public static Builder builder(String name) {
        return new Builder(name);
    }

    /** Datagen hook: registers every defined damage type into the datapack registry. */
    public static void bootstrap(BootstrapContext<DamageType> context) {
        for (Map.Entry<ResourceKey<DamageType>, DamageType> e : DEFINITIONS.entrySet()) {
            context.register(e.getKey(), e.getValue());
        }
    }

    /** Resolves a registered damage type into a usable {@link DamageSource} for the given level. */
    public static DamageSource of(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key)
        );
    }

    public static class Builder {
        private final String name;
        private String messageId;
        private DamageScaling scaling = DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
        private float exhaustion = 0.1F;
        private DamageEffects effects = DamageEffects.HURT;

        private Builder(String name) {
            this.name = name;
            this.messageId = name;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder scaling(DamageScaling scaling) {
            this.scaling = scaling;
            return this;
        }

        public Builder exhaustion(float exhaustion) {
            this.exhaustion = exhaustion;
            return this;
        }

        public Builder effects(DamageEffects effects) {
            this.effects = effects;
            return this;
        }

        public ResourceKey<DamageType> register() {
            ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, rl(name));
            DEFINITIONS.put(key, new DamageType(messageId, scaling, exhaustion, effects));
            return key;
        }
    }
}
