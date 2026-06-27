package igentuman.nc.util.caps;

public class ItemCapDefinition {

    public int inputSlots = 0;
    public int outputSlots = 0;
    public int globalSlots = 0;
    public int catalystSlots = 0;
    public int hiddenSlots = 0;

    private ItemCapDefinition() {
    }

    public static ItemCapDefinition create() {
        return new ItemCapDefinition();
    }

    public ItemCapDefinition inputs(int qty) {
        inputSlots = qty;
        return this;
    }

    public ItemCapDefinition outputs(int qty) {
        outputSlots = qty;
        return this;
    }

    public ItemCapDefinition global(int qty) {
        globalSlots = qty;
        return this;
    }

    public ItemCapDefinition catalyst(int qty) {
        catalystSlots = qty;
        return this;
    }

    public ItemCapDefinition hidden(int qty) {
        hiddenSlots = qty;
        return this;
    }
}
