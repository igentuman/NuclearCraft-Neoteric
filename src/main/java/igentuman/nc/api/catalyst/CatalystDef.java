package igentuman.nc.api.catalyst;

import net.minecraft.world.item.Item;

import java.util.Objects;
import java.util.function.Supplier;

/** Registration record binding a catalyst item to its {@link CatalystType} and behavior {@link CatalystFactory}. */
public record CatalystDef(Supplier<? extends Item> item, CatalystType type, CatalystFactory factory) {
    public CatalystDef {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(factory, "factory");
    }
}
