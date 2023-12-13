package igentuman.nc.datagen;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.data.DataGenerator;

import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCBlocks.NC_RF_AMPLIFIERS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCK;

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
            if(NCItems.NC_CHUNKS.containsKey(ore.replaceAll("_deepslate|_end|_nether",""))) {
                lootTables.put(NCBlocks.ORE_BLOCKS.get(ore).get(), createSilkTouchTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get(), NCItems.NC_CHUNKS.get(ore.replaceAll("_deepslate|_end|_nether","")).get(), 1, 1));
            } else {
                lootTables.put(NCBlocks.ORE_BLOCKS.get(ore).get(), createSimpleTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get()));
            }
        }
    }
    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            lootTables.put(NCBlocks.NC_BLOCKS.get(name).get(), createSimpleTable("block", NCBlocks.NC_BLOCKS.get(name).get()));
        }
        for(String name: FISSION_BLOCKS.keySet()) {
            lootTables.put(FISSION_BLOCKS.get(name).get(), createSimpleTable("block", FISSION_BLOCKS.get(name).get()));
        }
        for(String name: FUSION_BLOCKS.keySet()) {
            lootTables.put(FUSION_BLOCKS.get(name).get(), createSimpleTable("block", FUSION_BLOCKS.get(name).get()));
        }
        for(String name: TURBINE_BLOCKS.keySet()) {
            lootTables.put(TURBINE_BLOCKS.get(name).get(), createSimpleTable("block", TURBINE_BLOCKS.get(name).get()));
        }
    }

    private void machines() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            lootTables.put(NCProcessors.PROCESSORS.get(name).get(), createSimpleTable("block", NCProcessors.PROCESSORS.get(name).get()));
        }
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            lootTables.put(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(), createSimpleTable("block", NCEnergyBlocks.ENERGY_BLOCKS.get(name).get()));
        }
        for(String name: STORAGE_BLOCK.keySet()) {
            lootTables.put(STORAGE_BLOCK.get(name).get(), createSimpleTable("block", STORAGE_BLOCK.get(name).get()));
        }
        for(String name: NC_ELECTROMAGNETS.keySet()) {
            lootTables.put(NC_ELECTROMAGNETS.get(name).get(), createSimpleTable("block", NC_ELECTROMAGNETS.get(name).get()));
        }
        for(String name: NC_RF_AMPLIFIERS.keySet()) {
            lootTables.put(NC_RF_AMPLIFIERS.get(name).get(), createSimpleTable("block", NC_RF_AMPLIFIERS.get(name).get()));
        }
    }
}