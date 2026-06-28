package igentuman.nc.util;

import java.util.ArrayList;
import java.util.List;

public class SlotsLayout {
    public final List<SlotDef> slots = new ArrayList<>();

    public final static SlotsLayout ONE_TO_ONE = SlotsLayout.create().addDefault(30, 30).addBig(115, 30);
    public final static SlotsLayout ONE_TO_TWO = SlotsLayout.create().addDefault(30, 30).addDefault(115, 30).addDefault(135, 30);
    public final static SlotsLayout TWO_TO_ONE = SlotsLayout.create().addDefault(30, 30).addDefault(50, 30).addDefault(115, 30);
    public final static SlotsLayout TWO_TO_TWO = SlotsLayout.create().addDefault(30, 30).addDefault(50, 30).addDefault(115, 30).addDefault(135, 30);
    public final static SlotsLayout ONE_TO_THREE = SlotsLayout.create().addDefault(30, 30).addDefault(50, 30).addDefault(115, 30).addDefault(135, 30).addDefault(155, 30);
    public final static SlotsLayout THREE_TO_ONE = SlotsLayout.create().addDefault(10, 30).addDefault(30, 30).addDefault(50, 30).addDefault(115, 30);
    public final static SlotsLayout THREE_TO_TWO = SlotsLayout.create().addDefault(10, 30).addDefault(30, 30).addDefault(50, 30).addDefault(115, 30).addDefault(135, 30);

    private SlotsLayout() {}

    public static SlotsLayout create() {
        return new SlotsLayout();
    }

    /**
     * Builds a layout in the canonical slot order the container/screen expect:
     * input items, input fluids, output items, output fluids. Inputs sit on the left,
     * outputs on the right.
     */
    public static SlotsLayout forProcessor(int inputItems, int inputFluids, int outputItems, int outputFluids) {
        SlotsLayout layout = create();
        int lx = 30;
        for (int i = 0; i < inputItems; i++)  { layout.addDefault(lx, 30); lx += 20; }
        for (int i = 0; i < inputFluids; i++) { layout.addDefault(lx, 30); lx += 20; }
        int rx = 115;
        for (int i = 0; i < outputItems; i++)  { layout.addDefault(rx, 30); rx += 20; }
        for (int i = 0; i < outputFluids; i++) { layout.addDefault(rx, 30); rx += 20; }
        return layout;
    }

    public SlotsLayout addDefault(int x, int y) {
        slots.add(new SlotDef(x, y));
        return this;
    }

    public SlotsLayout addBig(int x, int y) {
        slots.add(new SlotDef(x, y, SlotDef.SlotType.BIG));
        return this;
    }

    public SlotsLayout addBar(int x, int y) {
        slots.add(new SlotDef(x, y, SlotDef.SlotType.VERTICAL_BAR));
        return this;
    }

    public SlotsLayout addRound(int x, int y) {
        slots.add(new SlotDef(x, y, SlotDef.SlotType.ROUND));
        return this;
    }

}
