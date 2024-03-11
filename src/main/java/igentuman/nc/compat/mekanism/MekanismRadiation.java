package igentuman.nc.compat.mekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class MekanismRadiation {
    //radiation in mRads
    //rads to Sv conversion 1/10
    public static void radiate(int x, int y,  int z, int radiation, World level) {
        if(radiation == 0) return;
 //       RadiationManager.INSTANCE.radiate(new Coord4D(x, y, z, level.dimension()), ((double)radiation)/10000000);
    }

    public static void addEntityRadiation(PlayerEntity entity, double i) {
        //RadiationManager.INSTANCE.radiate(entity, i);
    }
}
