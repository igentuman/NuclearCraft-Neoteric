package igentuman.nc.compat.mekanism;
import mekanism.api.Coord4D;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MekanismRadiation {
    //radiation in mRads
    //rads to Sv conversion 1/10
    public static final double URAD_PER_SV = 10_000_000d;
    // Sv per Rad. addRadiation(level, value, ...) takes value in Rad (multiplies by 1e6 internally).
    public static final double RAD_PER_SV = 10d;

    // Set true while mirroring Mek -> NC inside mixin, so NC.addRadiation skips back-emission to Mek.
    public static final ThreadLocal<Boolean> MIRRORING = ThreadLocal.withInitial(() -> false);

    public static void radiate(int x, int y,  int z, int radiation, Level level) {
        if(radiation == 0) return;
        RadiationManager.INSTANCE.radiate(new Coord4D(x, y, z, level.dimension()), ((double)radiation)/URAD_PER_SV);
    }

    public static void addEntityRadiation(Player entity, double sieverts) {
        RadiationManager.INSTANCE.radiate(entity, sieverts);
    }

    // Mirror NC player rad (uRad) to Mek player rad capability (Sv). NC = source of truth.
    public static void syncEntityRadiation(Player entity, long uRad) {
        IRadiationEntity cap = entity.getCapability(Capabilities.RADIATION_ENTITY).orElse(null);
        if (cap == null) return;
        cap.set(uRad / URAD_PER_SV);
    }
}
