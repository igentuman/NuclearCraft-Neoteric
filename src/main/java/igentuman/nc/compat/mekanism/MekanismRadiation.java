package igentuman.nc.compat.mekanism;
import mekanism.api.Coord4D;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MekanismRadiation {
    //radiation in mRads
    //rads to Sv conversion 1/10
    public static void radiate(int x, int y,  int z, int radiation, Level level) {
        if(radiation == 0) return;
        RadiationManager.INSTANCE.radiate(new Coord4D(x, y, z, level.dimension()), ((double)radiation)/10000000);
    }

    public static void addEntityRadiation(Player entity, double i) {
        RadiationManager.INSTANCE.radiate(entity, i);
    }
}
