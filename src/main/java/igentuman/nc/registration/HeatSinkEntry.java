package igentuman.nc.registration;

import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.api.multiblock.part.HeatSinkDef;
import igentuman.nc.item.NCBlockItem;
import igentuman.nc.setup.ModEntries;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Consumer;

import static igentuman.nc.setup.Registers.BLOCKS;
import static igentuman.nc.setup.Registers.ITEMS;

/** Registration unit for one heat sink: registers its block and item from a {@link HeatSinkDef} and holds cooling params. */
public class HeatSinkEntry {

    public final String name;
    private HeatSinkDef def;
    private final DeferredBlock<HeatSinkBlock> block;
    private boolean enabled = true;

    private HeatSinkEntry(HeatSinkDef def) {
        this.name = def.name;
        this.def = def;
        String blockId = def.name + "_heat_sink";
        this.block = BLOCKS.registerBlock(blockId, props -> new HeatSinkBlock(props, this.def));
        ITEMS.registerItem(blockId, props -> new NCBlockItem(this.block.get(), props),
                props -> props.useBlockDescriptionPrefix());
    }

    public static HeatSinkEntry register(HeatSinkDef def) {
        HeatSinkEntry existing = ModEntries.HEAT_SINKS.get(def.name);
        if (existing != null) return existing;
        HeatSinkEntry entry = new HeatSinkEntry(def);
        ModEntries.HEAT_SINKS.put(def.name, entry);
        return entry;
    }

    public void override(Consumer<HeatSinkDef> mutator) {
        mutator.accept(def);
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DeferredBlock<HeatSinkBlock> block() {
        return block;
    }

    public HeatSinkDef def() {
        return def;
    }
}
