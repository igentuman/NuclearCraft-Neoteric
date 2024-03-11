package igentuman.nc.effect;
import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

import javax.annotation.Nullable;

public class RadiationResistance extends Effect {
    protected RadiationResistance(EffectType p_i50391_1_, int p_i50391_2_) {
        super(p_i50391_1_, p_i50391_2_);
    }
/*    public RadiationResistance(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }*/

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity1, LivingEntity livingEntity, int strength, double p_19466_)
    {
        applyEffectTick(livingEntity, strength);
    }

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 50 >> p_19456_;
        if (k > 0) {
            return p_19455_ % k == 0;
        }
        return true;
    }

    public void applyEffectTick(LivingEntity ent, int id)
    {
        PlayerRadiation radCap = ent.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
        if(radCap == null) {
            return;
        }
        radCap.setRadiation(radCap.getRadiation() - id/1000);
    }
}
