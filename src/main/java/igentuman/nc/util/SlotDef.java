package igentuman.nc.util;

public class SlotDef {
    public int x;
    public int y;
    public SlotType type;

    public enum SlotType {
        DEFAULT, BIG, VERTICAL_BAR, ROUND
    }

    public SlotDef(int x, int y, SlotType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public SlotDef(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = SlotType.DEFAULT;
    }
}
