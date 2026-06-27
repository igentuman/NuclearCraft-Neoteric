package igentuman.nc.util.caps;

public class EnergyCapDefinition {
    private final int inputRate;
    private final int outputRate;
    private final int capacity;

    private EnergyCapDefinition(int inputRate, int outputRate, int capacity) {
        this.inputRate = inputRate;
        this.outputRate = outputRate;
        this.capacity = capacity;
    }

    public static EnergyCapDefinition create(int...params) {
        return new EnergyCapDefinition(params[0], params[1], params[2]);
    }

    public static EnergyCapDefinition processor(int capacity) {
        return new EnergyCapDefinition(capacity/2, 0, capacity);
    }

    public static EnergyCapDefinition battery(int capacity) {
        return new EnergyCapDefinition(capacity/2, capacity/2, capacity);
    }

    public static EnergyCapDefinition generator(int capacity) {
        return new EnergyCapDefinition(0, capacity/2, capacity);
    }

    public int getInputRate() {
        return inputRate;
    }

    public int getOutputRate() {
        return outputRate;
    }

    public int getCapacity() {
        return capacity;
    }
}
