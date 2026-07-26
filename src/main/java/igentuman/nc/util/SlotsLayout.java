package igentuman.nc.util;

import java.util.ArrayList;
import java.util.List;

/** Builds and provides preset arrangements of input/output slots for processor GUIs. */
public class SlotsLayout {
    public final List<SlotDef> slots = new ArrayList<>();

    private static final int STRIDE = 20;
    private static final int ROW_Y = 30;
    private static final int GRID_TOP_Y = 20;

    public final static SlotsLayout ONE_TO_ONE   = create().addInputRow(30, 1).addOutputRow(115, 1);
    public final static SlotsLayout ONE_TO_TWO   = create().addInputRow(30, 1).addOutputRow(115, 2);
    public final static SlotsLayout ONE_TO_THREE = create().addInputRow(30, 1).addOutputRow(115, 3);
    public final static SlotsLayout ONE_TO_FOUR  = create().addInputRow(30, 1).addOutputGrid(115, 4);
    public final static SlotsLayout ONE_TO_SIX   = create().addInputRow(30, 1).addOutputGrid(115, 6);
    public final static SlotsLayout ONE_TO_EIGHT = create().addInputRow(30, 1).addOutputGrid(99, 8);
    public final static SlotsLayout TWO_TO_ONE   = create().addInputRow(30, 2).addOutputRow(115, 1);
    public final static SlotsLayout TWO_TO_TWO   = create().addInputRow(30, 2).addOutputRow(115, 2);
    public final static SlotsLayout SIX_TO_ONE   = create().addInputGrid(30, 6).addOutputRow(135, 1);

    private SlotsLayout() {}

    public static SlotsLayout create() {
        return new SlotsLayout();
    }

    /**
     * Resolves a curated preset for the processor's total input/output slot counts.
     * Slot order within the preset is canonical: inputs (left) then outputs (right),
     * matching the container order input items, input fluids, output items, output fluids.
     */
    public static SlotsLayout forProcessor(int inputItems, int inputFluids, int outputItems, int outputFluids) {
        int in = inputItems + inputFluids;
        int out = outputItems + outputFluids;
        return switch (in * 100 + out) {
            case 101 -> ONE_TO_ONE;
            case 102 -> ONE_TO_TWO;
            case 103 -> ONE_TO_THREE;
            case 104 -> ONE_TO_FOUR;
            case 106 -> ONE_TO_SIX;
            case 108 -> ONE_TO_EIGHT;
            case 201 -> TWO_TO_ONE;
            case 202 -> TWO_TO_TWO;
            case 601 -> SIX_TO_ONE;
            default -> throw new IllegalArgumentException(
                    "No slot layout preset for " + in + " inputs -> " + out + " outputs");
        };
    }

    public SlotsLayout addInput(int x, int y) {
        slots.add(new SlotDef(x, y, false));
        return this;
    }

    public SlotsLayout addOutput(int x, int y) {
        slots.add(new SlotDef(x, y, true));
        return this;
    }

    private SlotsLayout addInputRow(int startX, int count) {
        for (int i = 0; i < count; i++) addInput(startX + i * STRIDE, ROW_Y);
        return this;
    }

    private SlotsLayout addOutputRow(int startX, int count) {
        for (int i = 0; i < count; i++) addOutput(startX + i * STRIDE, ROW_Y);
        return this;
    }

    private SlotsLayout addInputGrid(int startX, int count) {
        for (int i = 0; i < count; i++) addInput(startX + (i / 2) * STRIDE, GRID_TOP_Y + (i % 2) * STRIDE);
        return this;
    }

    private SlotsLayout addOutputGrid(int startX, int count) {
        for (int i = 0; i < count; i++) addOutput(startX + (i / 2) * STRIDE, GRID_TOP_Y + (i % 2) * STRIDE);
        return this;
    }

    public SlotsLayout withCatalysts(int count) {
        SlotsLayout copy = SlotsLayout.create();
        copy.slots.addAll(this.slots);
        int startX = 92 - (count - 1) * STRIDE;
        for (int i = 0; i < count; i++) {
            copy.slots.add(new SlotDef(startX + i * STRIDE, 74, false));
        }
        return copy;
    }
}
