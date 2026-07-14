package igentuman.nc.compat;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.util.SlotsLayout;
import igentuman.nc.util.caps.FluidCapDefinition;
import igentuman.nc.util.caps.ItemCapDefinition;

import java.util.ArrayList;
import java.util.List;

/** Shared slot and progress-bar geometry for the JEI and EMI processor recipe categories. */
public class ProcessorCategoryLayout {

    public static final int SLOT = 18;
    public static final int STRIDE = 20;
    public static final int FALLBACK_GAP = 30;
    public static final int PAD = 2;
    public static final int BAR_W = 36;

    /** Slot sprite atlas offsets mirror {@code screen.element.SlotWidget}: item u0 / fluid u18, input v0 / output v36. */
    public enum SlotType {
        ITEM_IN(0, 0, false), FLUID_IN(18, 0, false), ITEM_OUT(0, 36, true), FLUID_OUT(18, 36, true);
        public final int u;
        public final int v;
        public final boolean output;
        SlotType(int u, int v, boolean output) { this.u = u; this.v = v; this.output = output; }
    }

    public static final class Slot {
        public final int spriteX;
        public final int spriteY;
        public final int ingX;
        public final int ingY;
        public final SlotType type;
        Slot(int spriteX, int spriteY, SlotType type) {
            this.spriteX = spriteX;
            this.spriteY = spriteY;
            this.ingX = spriteX + 1;
            this.ingY = spriteY + 1;
            this.type = type;
        }
    }

    public final int itemInputCount;
    public final int fluidInputCount;
    public final int itemOutputCount;
    public final int fluidOutputCount;
    public final int progressBar;
    public final int barH;
    public final List<Slot> slots = new ArrayList<>();
    public final int width;
    public final int height;
    public final boolean hasArrow;
    public final int arrowX;
    public final int arrowY;

    public ProcessorCategoryLayout(ModEntry entry) {
        ItemCapDefinition itemCap = entry.itemCap();
        FluidCapDefinition fluidCap = entry.fluidCap();
        itemInputCount   = itemCap  != null ? itemCap.inputSlots         : 0;
        fluidInputCount  = fluidCap != null ? fluidCap.inputTanks.size()  : 0;
        itemOutputCount  = itemCap  != null ? itemCap.outputSlots         : 0;
        fluidOutputCount = fluidCap != null ? fluidCap.outputTanks.size() : 0;
        progressBar = entry.progressBar();
        barH = progressBar > 14 ? 36 : 15;

        // Canonical order: item inputs, fluid inputs, item outputs, fluid outputs.
        List<SlotType> types = new ArrayList<>();
        for (int i = 0; i < itemInputCount; i++)   types.add(SlotType.ITEM_IN);
        for (int i = 0; i < fluidInputCount; i++)  types.add(SlotType.FLUID_IN);
        for (int i = 0; i < itemOutputCount; i++)  types.add(SlotType.ITEM_OUT);
        for (int i = 0; i < fluidOutputCount; i++) types.add(SlotType.FLUID_OUT);
        int n = types.size();

        // Raw ingredient positions (pre-shift). Use the in-game layout when available.
        int[] rawX = new int[n];
        int[] rawY = new int[n];
        SlotsLayout layout = entry.slotsLayout();
        int totalInputs = itemInputCount + fluidInputCount;
        if (layout != null && layout.slots.size() == n) {
            for (int i = 0; i < n; i++) {
                rawX[i] = layout.slots.get(i).x;
                rawY[i] = layout.slots.get(i).y;
            }
        } else {
            int x = 30;
            for (int i = 0; i < n; i++) {
                if (i == totalInputs && totalInputs > 0) x += FALLBACK_GAP;
                rawX[i] = x;
                rawY[i] = 30;
                x += STRIDE;
            }
        }

        // Sprite boxes live at (rawX-1, rawY-1); compute raw bounds incl. the arrow, then shift to PAD.
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        int inRight = Integer.MIN_VALUE, outLeft = Integer.MAX_VALUE, top = Integer.MAX_VALUE, bot = Integer.MIN_VALUE;
        boolean anyIn = false, anyOut = false;
        for (int i = 0; i < n; i++) {
            int sx = rawX[i] - 1, sy = rawY[i] - 1;
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx + SLOT);
            maxY = Math.max(maxY, sy + SLOT);
            top = Math.min(top, sy);
            bot = Math.max(bot, sy + SLOT);
            if (types.get(i).output) { anyOut = true; outLeft = Math.min(outLeft, sx); }
            else { anyIn = true; inRight = Math.max(inRight, sx + SLOT); }
        }

        boolean arrow = anyIn && anyOut;
        int aRawX = 0, aRawY = 0;
        if (arrow) {
            aRawX = (inRight + outLeft) / 2 - BAR_W / 2;
            aRawY = (top + bot) / 2 - barH / 2;
            minX = Math.min(minX, aRawX);
            minY = Math.min(minY, aRawY);
            maxX = Math.max(maxX, aRawX + BAR_W);
            maxY = Math.max(maxY, aRawY + barH);
        }
        if (n == 0) { minX = 0; minY = 0; maxX = SLOT; maxY = SLOT; }

        int xShift = PAD - minX;
        int yShift = PAD - minY;
        for (int i = 0; i < n; i++) {
            slots.add(new Slot(rawX[i] - 1 + xShift, rawY[i] - 1 + yShift, types.get(i)));
        }
        hasArrow = arrow;
        arrowX = aRawX + xShift;
        arrowY = aRawY + yShift;
        width = (maxX - minX) + 2 * PAD;
        height = (maxY - minY) + 2 * PAD;
    }
}
