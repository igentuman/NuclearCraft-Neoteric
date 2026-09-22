package igentuman.nc.multiblock.accelerator;

public final class AcceleratorThermal {

    private AcceleratorThermal() {
    }

    public static long temperatureK(long storedHeat, long heatCapacity) {
        if (heatCapacity <= 0) return 0;
        return Math.max(0, storedHeat) / heatCapacity;
    }

    public static boolean isOverheated(long temperatureK, long maximumTemperatureK) {
        return maximumTemperatureK > 0 && temperatureK >= maximumTemperatureK;
    }

    public static long applyPassiveCooling(long storedHeat, long coolingPerTick) {
        if (coolingPerTick <= 0) return Math.max(0, storedHeat);
        try {
            return Math.max(0, Math.subtractExact(storedHeat, coolingPerTick));
        } catch (ArithmeticException overflow) {
            return 0;
        }
    }

    public static long coolantOperations(long storedHeat, long heatPerMb, int inputAmountPerOp,
                                          long availableInputMb, long availableOutputCapacityMb,
                                          int outputAmountPerOp) {
        if (storedHeat <= 0 || heatPerMb <= 0 || inputAmountPerOp <= 0 || outputAmountPerOp <= 0) return 0;
        long heatPerOp;
        try {
            heatPerOp = Math.multiplyExact(heatPerMb, (long) inputAmountPerOp);
        } catch (ArithmeticException overflow) {
            return 0;
        }
        if (heatPerOp <= 0) return 0;
        long heatOps = (storedHeat + heatPerOp - 1) / heatPerOp;
        long inputOps = availableInputMb / inputAmountPerOp;
        long outputOps = availableOutputCapacityMb / outputAmountPerOp;
        return Math.max(0, Math.min(heatOps, Math.min(inputOps, outputOps)));
    }

    public static long heatAfterCoolant(long storedHeat, long heatPerMb, int inputAmountPerOp, long ops) {
        if (ops <= 0) return Math.max(0, storedHeat);
        try {
            long removed = Math.multiplyExact(Math.multiplyExact(heatPerMb, (long) inputAmountPerOp), ops);
            return Math.max(0, Math.subtractExact(storedHeat, removed));
        } catch (ArithmeticException overflow) {
            return 0;
        }
    }
}
