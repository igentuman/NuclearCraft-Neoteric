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
    }

    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            lootTables.put(NCBlocks.ORE_BLOCKS.get(ore).get(), createSimpleTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get()));
        }
    }
}