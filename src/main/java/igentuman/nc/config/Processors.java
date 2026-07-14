package igentuman.nc.config;

/** Convenience alias for per-entry enable checks; delegates to {@link Entries}. */
public class Processors {

    public static boolean isEnabled(String name) {
        return Entries.isEnabled(name);
    }
}
