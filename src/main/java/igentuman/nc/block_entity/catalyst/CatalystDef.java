package igentuman.nc.block_entity.catalyst;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/** Registration record binding a catalyst item to its {@link CatalystType} and behavior {@link CatalystFactory}. */
public record CatalystDef(DeferredItem<Item> item, CatalystType type, CatalystFactory factory) {}
