package igentuman.nc.util.caps;

import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Exposes native item handlers as one, indexing their slots sequentially across delegates. */
public class CombinedItemHandler extends CombinedResourceHandler<ItemResource> {
    @SafeVarargs
    public CombinedItemHandler(ResourceHandler<ItemResource>... delegates) {
        super(delegates);
    }
}
