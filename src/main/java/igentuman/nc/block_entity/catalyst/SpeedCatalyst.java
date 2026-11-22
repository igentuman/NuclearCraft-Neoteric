package igentuman.nc.block_entity.catalyst;

import igentuman.nc.api.catalyst.Catalyst;
import igentuman.nc.api.catalyst.CatalystType;
import igentuman.nc.block_entity.GlobalBlockEntity;

public class SpeedCatalyst extends Catalyst {

    public SpeedCatalyst(GlobalBlockEntity host) {
        super(CatalystType.SPEED, host);
    }

    @Override
    public void preTick() {
        host.recipeInfo.multiplier = speedMultiplier();
        host.recipeInfo.parallelLimit = parallelLimit();
    }

    public int speedMultiplier() {
        return speedMultiplier(power);
    }

    public static int speedMultiplier(int power) {
        return power + 1;
    }

    public int parallelLimit() {
        return 1;
    }
}
