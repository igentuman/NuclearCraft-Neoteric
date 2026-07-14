package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/** Energy catalyst that lowers the host processor's effective energy-per-tick by its power. */
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
