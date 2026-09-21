package igentuman.nc.util.caps;

import java.util.LinkedHashSet;

/** Fluent descriptor of a block entity's input, output, and global fluid tanks and their capacities. */
public class FluidCapDefinition {

    public final LinkedHashSet<Tank> inputTanks = new LinkedHashSet<>();
    public final LinkedHashSet<Tank> outputTanks = new LinkedHashSet<>();
    public final LinkedHashSet<Tank> globalTanks = new LinkedHashSet<>();

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
