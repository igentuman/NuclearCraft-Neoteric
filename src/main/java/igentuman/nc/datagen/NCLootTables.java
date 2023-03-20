package igentuman.nc.datagen;

import igentuman.nc.setup.NCBlocks;
import net.minecraft.data.DataGenerator;

public class NCLootTables extends BaseLootTableProvider {

    public NCLootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addOres();
        addBlocks();
    }

    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            lootTables.put(NCBlocks.ORE_BLOCKS.get(ore).get(), createSimpleTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get()));
        }
    }
    private void addBlocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            lootTables.put(NCBlocks.NC_BLOCKS.get(name).get(), createSimpleTable("block", NCBlocks.NC_BLOCKS.get(name).get()));
        }
    }
}