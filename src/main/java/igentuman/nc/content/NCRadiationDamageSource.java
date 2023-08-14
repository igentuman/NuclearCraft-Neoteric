package igentuman.nc.content;

import net.minecraft.world.damagesource.DamageSource;

public class NCRadiationDamageSource {

    public static DamageSource RADIATION = new DamageSource("radiation").bypassArmor().bypassMagic().setMagic();
}
