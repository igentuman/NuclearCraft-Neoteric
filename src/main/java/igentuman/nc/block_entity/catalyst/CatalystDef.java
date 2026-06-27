package igentuman.nc.block_entity.catalyst;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Registration record for a catalyst: the item placed in the catalyst slot, its type,
 * and the factory that builds the {@link Catalyst} behavior bound to a host BE.
 */
public record CatalystDef(DeferredItem<Item> item, CatalystType type, CatalystFactory factory) {}
