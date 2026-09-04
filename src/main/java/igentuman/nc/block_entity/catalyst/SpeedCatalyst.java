package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/** Speed catalyst that raises the host processor's recipe multiplier by its power each tick. */
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
