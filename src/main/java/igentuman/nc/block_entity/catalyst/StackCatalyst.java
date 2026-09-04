package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/** Mid-tier speed upgrade that batches one operation per four installed items, up to 32. */
public class StackCatalyst extends SpeedCatalyst {

    public StackCatalyst(GlobalBlockEntity host) {
        super(host);
    }

    @Override
    public int parallelLimit() {
        return parallelLimit(power);
    }

    public static int parallelLimit(int power) {
        return Math.min(32, (power + 3) / 4);
    }
}
