package igentuman.nc.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

public class NCDamageSources {
    public static final DamageSource ACID = new DamageSource("acid").bypassArmor();

    public static final DamageSource TURBINE = new DamageSource("turbine");

    public static DamageSource TURBINE(Level level) {
        return TURBINE;
    }
}
