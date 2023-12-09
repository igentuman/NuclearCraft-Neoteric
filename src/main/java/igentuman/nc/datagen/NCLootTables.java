package igentuman.nc.datagen;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCBlocks.NC_RF_AMPLIFIERS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCK;

public class NCLootTables extends BaseLootTableProvider {

    public NCLootTables() {
        super(null);
    }

    private void ores(BiConsumer<ResourceLocation, LootTable.Builder> builder) {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            if(NCItems.NC_CHUNKS.containsKey(ore.replaceAll("_deepslate|_end|_nether",""))) {
                builder.accept(NCBlocks.ORE_BLOCKS.get(ore).getId(), createSilkTouchTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get(), NCItems.NC_CHUNKS.get(ore.replaceAll("_deepslate|_end|_nether","")).get(), 1, 1));
            } else {
                builder.accept(NCBlocks.ORE_BLOCKS.get(ore).getId(), createSimpleTable("ore", NCBlocks.ORE_BLOCKS.get(ore).get()));
            }
        }
    }
    private void blocks(BiConsumer<ResourceLocation, LootTable.Builder> builder) {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            builder.accept(NCBlocks.NC_BLOCKS.get(name).getId(), createSimpleTable("block", NCBlocks.NC_BLOCKS.get(name).get()));
        }
        for(String name: FISSION_BLOCKS.keySet()) {
            builder.accept(FISSION_BLOCKS.get(name).getId(), createSimpleTable("block", FISSION_BLOCKS.get(name).get()));
        }
        for(String name: FUSION_BLOCKS.keySet()) {
            builder.accept(FUSION_BLOCKS.get(name).getId(), createSimpleTable("block", FUSION_BLOCKS.get(name).get()));
        }
    }

    private void machines(BiConsumer<ResourceLocation, LootTable.Builder> builder) {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            builder.accept(NCProcessors.PROCESSORS.get(name).getId(), createSimpleTable("block", NCProcessors.PROCESSORS.get(name).get()));
        }
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            builder.accept(NCEnergyBlocks.ENERGY_BLOCKS.get(name).getId(), createSimpleTable("block", NCEnergyBlocks.ENERGY_BLOCKS.get(name).get()));
        }
        for(String name: STORAGE_BLOCK.keySet()) {
            builder.accept(STORAGE_BLOCK.get(name).getId(), createSimpleTable("block", STORAGE_BLOCK.get(name).get()));
        }
        for(String name: NC_ELECTROMAGNETS.keySet()) {
            builder.accept(NC_ELECTROMAGNETS.get(name).getId(), createSimpleTable("block", NC_ELECTROMAGNETS.get(name).get()));
        }
        for(String name: NC_RF_AMPLIFIERS.keySet()) {
            builder.accept(NC_RF_AMPLIFIERS.get(name).getId(), createSimpleTable("block", NC_RF_AMPLIFIERS.get(name).get()));
        }
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        ores(biConsumer);
        blocks(biConsumer);
        machines(biConsumer);
    }
}