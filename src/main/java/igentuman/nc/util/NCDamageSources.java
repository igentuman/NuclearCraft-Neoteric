package igentuman.nc.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import static igentuman.nc.NuclearCraft.rl;

public class NCDamageSources {
    public static final DamageSource ACID = new DamageSource(Holder.direct(new DamageType("acid", DamageScaling.ALWAYS, 1f)));

    public static ResourceKey<DamageType> TURBINE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, rl("turbine"));

    public static DamageSource TURBINE(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TURBINE_TYPE));
    }
}
