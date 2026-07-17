package igentuman.nc.effect;
import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class RadiationDecay extends MobEffect {

    public RadiationDecay(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity1, LivingEntity livingEntity, int strength, double p_19466_)
    {
        applyEffectTick(livingEntity, strength);
    }

    public boolean isDurationEffectTick(int from, int to) {
        int k = 50 >> from;
        if (k > 0) {
            return to % k == 0;
        }
        return true;
    }

    public void applyEffectTick(LivingEntity ent, int id)
    {
        if(ent instanceof ServerPlayer serverPlayer) {
            PlayerRadiation radCap = serverPlayer.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
            if (radCap == null) {
                return;
            }
            radCap.setRadiation(Math.max(radCap.getRadiation() - 1000, 0));
        }
    }
}
