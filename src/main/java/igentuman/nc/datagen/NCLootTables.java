package igentuman.nc.datagen;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.data.DataGenerator;

public class NCLootTables extends BaseLootTableProvider {

    public NCLootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        ores();
        blocks();
        machines();
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            lootTables.put(NCBlocks.ORE_BLOCKS.get(ore).get(), createSimpleTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get()));
        }
    }
    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            lootTables.put(NCBlocks.NC_BLOCKS.get(name).get(), createSimpleTable("block", NCBlocks.NC_BLOCKS.get(name).get()));
        }
        for(String name: FissionReactor.MULTI_BLOCKS.keySet()) {
            lootTables.put(FissionReactor.MULTI_BLOCKS.get(name).get(), createSimpleTable("block", FissionReactor.MULTI_BLOCKS.get(name).get()));
        }
    }

    private void machines() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            lootTables.put(NCProcessors.PROCESSORS.get(name).get(), createSimpleTable("block", NCProcessors.PROCESSORS.get(name).get()));
        }
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            lootTables.put(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(), createSimpleTable("block", NCEnergyBlocks.ENERGY_BLOCKS.get(name).get()));
        }
    }
}