package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;
import net.minecraft.world.item.Item;

/** Abstract per-tick catalyst behavior attached to a processor for one {@link CatalystType}. */
public abstract class Catalyst {

    public final CatalystType type;
    public final GlobalBlockEntity host;

    /** Strength = count of catalyst items in this type's catalyst slot. */
    public int power;

    /** Item currently backing this catalyst; lets the host detect slot-content changes. */
    public Item item;

    protected Catalyst(CatalystType type, GlobalBlockEntity host) {
        this.type = type;
        this.host = host;
    }

    /** Runs each server tick before the processor's operation logic. */
    public void preTick() {}

    /** Runs each server tick after the processor's operation logic. */
    public void postTick() {}
}
