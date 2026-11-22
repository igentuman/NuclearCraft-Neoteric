package igentuman.nc.handler.crafter;

import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.util.caps.RoutingItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Live native view of the crafter's linked item storage, sharing the caller's transaction. */
public class AggregatedItemHandler extends RoutingItemHandler {
    public AggregatedItemHandler(ResourceHandler<ItemResource> containers) {
        this(containers, () -> true);
    }

    public AggregatedItemHandler(ResourceHandler<ItemResource> containers, BooleanSupplier available) {
        super(() -> available.getAsBoolean() ? sources(containers).stream()
                .map(handler -> new Endpoint(handler, true, true)).toList() : List.of());
    }

    static List<ResourceHandler<ItemResource>> sources(ResourceHandler<ItemResource> containers) {
        List<ResourceHandler<ItemResource>> out = new ArrayList<>();
        var seen = new HashSet<UUID>();
        for (int i = 0; i < containers.size(); i++) {
            ItemStack stack = containers.getResource(i).toStack(containers.getAmountAsInt(i));
            if (stack.isEmpty() || !(stack.getItem() instanceof ContainerBlockItem)) continue;
            // UUID aliases share one inventory; count and visit that storage only once.
            UUID uuid = ContainerBlockItem.readUuid(stack);
            if (uuid != null && !seen.add(uuid)) continue;
            var access = ItemAccess.forHandlerIndexStrict(containers, i);
            var handler = access.getCapability(Capabilities.Item.ITEM);
            if (handler != null) out.add(handler);
        }
        return out;
    }
}
