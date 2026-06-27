package igentuman.nc.config;

/**
 * Thin alias retained for backwards compatibility with fork code.
 * Per-entry gating now lives in {@link Entries}; this delegates to it.
 */
public class Processors {

    public static boolean isEnabled(String name) {
        return Entries.isEnabled(name);
    }
}
