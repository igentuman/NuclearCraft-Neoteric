package igentuman.nc.setup.processors.config;

public class ProcessorSlots {

    public final static int[] defaultInputSlotPos = new int[] {20, 50};
    public final static int[] defaultOutputSlotPos = new int[] {80, 50};
    public final static int defaultMargin = 25;

    private int input_items;
    private int input_fluids;
    private int output_items;
    private int output_fluids;

    //order for slots: input fluids, input items, output fluids, output items;
    private int[] slotPositions;

    public int getInputItems() {
        return input_items;
    }

    public ProcessorSlots setInputItems(int input_items) {
        this.input_items = input_items;
        return this;
    }

    public int getInputFluids() {
        return input_fluids;
    }

    public ProcessorSlots setInputFluids(int input_fluids) {
        this.input_fluids = input_fluids;
        return this;
    }

    public int getOutputItems() {
        return output_items;
    }

    public ProcessorSlots setOutputItems(int output_items) {
        this.output_items = output_items;
        return this;
    }

    public int getOutputFluids() {
        return output_fluids;
    }

    public ProcessorSlots setOutputFluids(int output_fluids) {
        this.output_fluids = output_fluids;
        return this;
    }

    public int[] getSlotPositions() {
        return slotPositions;
    }

    //order for slots: input fluids, input items, output fluids, output items;
    public ProcessorSlots setSlotPositions(int[] slotPositions) {
        this.slotPositions = slotPositions;
        return this;
    }
}
