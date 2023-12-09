package igentuman.nc.content;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class NCRadiationDamageSource {

    public static DamageSource RADIATION = new DamageSource(Holder.direct(new DamageType("radiation", DamageScaling.ALWAYS, 1f)));
}
