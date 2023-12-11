package igentuman.nc.util;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class NCDamageSources {
    public static final DamageSource ACID = new DamageSource(Holder.direct(new DamageType("acid", DamageScaling.ALWAYS, 1f)));

}
