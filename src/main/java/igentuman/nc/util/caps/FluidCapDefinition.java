package igentuman.nc.util.caps;

import java.util.HashSet;

/** Fluent descriptor of a block entity's input, output, and global fluid tanks and their capacities. */
public class FluidCapDefinition {

    public final HashSet<Tank> inputTanks = new HashSet<>();
    public final HashSet<Tank> outputTanks = new HashSet<>();
    public final HashSet<Tank> globalTanks = new HashSet<>();

    private FluidCapDefinition() {
    }

    public static FluidCapDefinition create() {
        return new FluidCapDefinition();
    }

    public static FluidCapDefinition basic() {
        return create().addInput(100000);
    }

    public FluidCapDefinition addInput(int capacity) {
        inputTanks.add(new Tank(capacity));
        return this;
    }

    public FluidCapDefinition addOutput(int capacity) {
        outputTanks.add(new Tank(capacity));
        return this;
    }

    public FluidCapDefinition addGlobal(int capacity) {
        globalTanks.add(new Tank(capacity));
        return this;
    }

    public static class Tank {
        public int capacity;

        public Tank(int capacity) {
            this.capacity = capacity;
        }
    }
}
