package igentuman.nc.content;


import net.minecraft.util.DamageSource;

public class NCRadiationDamageSource {

    public static DamageSource RADIATION = new DamageSource("radiation").bypassArmor().bypassMagic().setMagic();
}
