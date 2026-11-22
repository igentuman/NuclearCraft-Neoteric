package igentuman.nc.block_entity.catalyst;

import igentuman.nc.api.catalyst.Catalyst;
import igentuman.nc.api.catalyst.CatalystType;
import igentuman.nc.block_entity.GlobalBlockEntity;

public class EnergyCatalyst extends Catalyst {

    public static final int CAPACITY_PERCENT_PER_POWER = 50;

    public EnergyCatalyst(GlobalBlockEntity host) {
        super(CatalystType.ENERGY, host);
    }
}
