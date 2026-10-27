package igentuman.nc.screen.element;

import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;

public final class SlotMover {

    private static final Field SLOT_Y = resolveSlotY();

    private SlotMover() {
    }

    private static Field resolveSlotY() {
        try {
            Field f = Slot.class.getField("y");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static boolean available() {
        return SLOT_Y != null;
    }

    public static void setY(Slot slot, int y) {
        if (SLOT_Y == null || slot.y == y) return;
        try {
            SLOT_Y.setInt(slot, y);
        } catch (IllegalAccessException ignored) {
        }
    }
}
