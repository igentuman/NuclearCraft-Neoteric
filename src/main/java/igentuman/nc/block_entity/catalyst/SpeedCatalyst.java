package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/**
 * Reference SPEED catalyst: raises the recipe multiplier by its power.
 * The host resets {@code multiplier} to 1 before preTick each tick, so this never compounds.
 */
public class SpeedCatalyst extends Catalyst {

    public SpeedCatalyst(GlobalBlockEntity host) {
        super(CatalystType.SPEED, host);
    }

    @Override
    public void preTick() {
        host.recipeInfo.multiplier += power;
    }
}
