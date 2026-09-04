package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/** End-game speed upgrade providing standard speed scaling and one parallel operation per item. */
public class QuantumCatalyst extends SpeedCatalyst {

    public QuantumCatalyst(GlobalBlockEntity host) {
        super(host);
    }

    @Override
    public int speedMultiplier() {
        return speedMultiplier(power);
    }

    @Override
    public int parallelLimit() {
        return parallelLimit(power);
    }

    public static int speedMultiplier(int power) {
        return SpeedCatalyst.speedMultiplier(power);
    }

    public static int parallelLimit(int power) {
        return Math.max(1, power);
    }
}
