package igentuman.nc.util;

/** Describes a single GUI slot by its position, visual type, and whether it is an output slot. */
public class SlotDef {
    public int x;
    public int y;
    public SlotType type;
    public boolean output;

    public enum SlotType {
        DEFAULT, BIG, VERTICAL_BAR, ROUND
    }

    public SlotDef(int x, int y, SlotType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public SlotDef(int x, int y) {
        this(x, y, false);
    }

    public SlotDef(int x, int y, boolean output) {
        this.x = x;
        this.y = y;
        this.type = SlotType.DEFAULT;
        this.output = output;
    }
}
