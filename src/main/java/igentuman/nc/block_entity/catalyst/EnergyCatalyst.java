package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/**
 * Reference ENERGY catalyst: lowers the recipe's effective energy-per-tick by its power.
 * The host resets {@code energyPerTick} to the recipe baseline before preTick each tick,
 * so the reduction is reapplied fresh and never compounds toward zero.
 */
public class EnergyCatalyst extends Catalyst {

    private static final int REDUCTION_PER_POWER = 5;

    public EnergyCatalyst(GlobalBlockEntity host) {
        super(CatalystType.ENERGY, host);
    }

    @Override
    public void preTick() {
        int reduced = host.recipeInfo.energyPerTick - power * REDUCTION_PER_POWER;
        host.recipeInfo.energyPerTick = Math.max(0, reduced);
    }
}
