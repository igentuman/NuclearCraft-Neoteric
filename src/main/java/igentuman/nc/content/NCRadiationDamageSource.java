package igentuman.nc.content;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

public class NCRadiationDamageSource {
    public static final DamageSource RADIATION = new DamageSource("radiation").bypassArmor().bypassMagic();

    public static void init() {

    }

    public static DamageSource RADIATION(ServerPlayer player) {
        return RADIATION;
    }

    public static DamageSource RADIATION(Level level) {
        return RADIATION;
    }
}
