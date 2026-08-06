package igentuman.nc.compat.ponder;

import net.createmod.ponder.foundation.PonderIndex;

public class PonderUtil {

    public static void initPlugin() {
        PonderIndex.addPlugin(new NCPonderPlugin());
    }
}
