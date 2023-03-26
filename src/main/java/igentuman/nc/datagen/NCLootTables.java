package igentuman.nc.datagen;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.data.DataGenerator;

public class NCLootTables extends BaseLootTableProvider {

    public NCLootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addOres();
        addBlocks();
        addProcessors();
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

    private void addProcessors() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            lootTables.put(NCProcessors.PROCESSORS.get(name).get(), createSimpleTable("block", NCProcessors.PROCESSORS.get(name).get()));
        }
    }
}