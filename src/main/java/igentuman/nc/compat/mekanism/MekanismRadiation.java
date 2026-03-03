package igentuman.nc.compat.mekanism;

import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MekanismRadiation {
    //radiation in mRads
    //rads to Sv conversion 1/10
    public static void radiate(int x, int y, int z, int radiation, Level level) {
        if(radiation == 0) return;
        RadiationManager.get().radiate(level, new BlockPos(x, y, z), ((double)radiation)/10000000);
    }

    public static void addEntityRadiation(Player entity, double i) {
        RadiationManager.get().radiate(entity, i);
    }
}
