package igentuman.nc.block_entity;

public interface OutputLevelSource {

    double outputLevel(int mode);

    static double fraction(double value, double max) {
        return max <= 0 ? 0 : Math.clamp(value / max, 0, 1);
    }

    static int comparatorSignal(double level) {
        return Math.clamp((int) (level * 15 + 1e-9), 0, 15);
    }
}
