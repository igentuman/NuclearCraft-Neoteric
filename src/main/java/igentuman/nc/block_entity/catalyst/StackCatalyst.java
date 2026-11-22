package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

public class StackCatalyst extends SpeedCatalyst {

    public StackCatalyst(GlobalBlockEntity host) {
        super(host);
    }

    @Override
    public int parallelLimit() {
        return parallelLimit(power);
    }

    public static int parallelLimit(int power) {
        return Math.min(32, 1 + power / 4);
    }
}
