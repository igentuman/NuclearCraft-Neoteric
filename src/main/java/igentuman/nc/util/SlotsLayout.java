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
