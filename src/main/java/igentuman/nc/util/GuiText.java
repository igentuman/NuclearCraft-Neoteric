package igentuman.nc.util;

/** Text colour helpers for 26.1 GUI extraction. */
public final class GuiText {

    private GuiText() {
    }

    /**
     * Applies the pre-26.1 {@code Font} rule: a colour with no alpha bits is drawn opaque.
     * 26.1 {@code GuiGraphicsExtractor#text} skips text whose alpha is zero, so colours computed
     * at runtime as plain RGB must pass through here.
     */
    public static int legacy(int color) {
        return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
    }
}
