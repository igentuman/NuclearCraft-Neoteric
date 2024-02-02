package igentuman.nc.datagen;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BLOCKS;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCKS;

public class NCLootTables extends BaseLootTableProvider {

    private void ores() {
        for(String ore: ORE_BLOCKS.keySet()) {
            if(NCItems.NC_CHUNKS.containsKey(ore.replaceAll("_deepslate|_end|_nether",""))) {
                add(ORE_BLOCKS.get(ore).get(), block -> createSilkTouchTable("ore", ORE_BLOCKS.get(ore).get(), NCItems.NC_CHUNKS.get(ore.replaceAll("_deepslate|_end|_nether","")).get(), 1, 1));
            } else {
                add(ORE_BLOCKS.get(ore).get(), block -> createSimpleTable("ore", ORE_BLOCKS.get(ore).get()));
            }
        }
    }
    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            add(NCBlocks.NC_BLOCKS.get(name).get(), block -> createSimpleTable("block", NCBlocks.NC_BLOCKS.get(name).get()));
        }
        for(String name: FISSION_BLOCKS.keySet()) {
            add(FISSION_BLOCKS.get(name).get(), block -> createSimpleTable("block", FISSION_BLOCKS.get(name).get()));
        }
        for(String name: FUSION_BLOCKS.keySet()) {
            add(FUSION_BLOCKS.get(name).get(), block -> createSimpleTable("block", FUSION_BLOCKS.get(name).get()));
        }
        for(String name: TURBINE_BLOCKS.keySet()) {
            lootTables.put(TURBINE_BLOCKS.get(name).get(), createSimpleTable("block", TURBINE_BLOCKS.get(name).get()));
        }
    }

    private void machines() {
        for(String name: PROCESSORS.keySet()) {
            add(PROCESSORS.get(name).get(), block -> createSimpleTable("block", PROCESSORS.get(name).get()));
        }
        for(String name: ENERGY_BLOCKS.keySet()) {
            add(ENERGY_BLOCKS.get(name).get(), block -> createSimpleTable("block", ENERGY_BLOCKS.get(name).get()));
        }
        for(String name: STORAGE_BLOCKS.keySet()) {
            add(STORAGE_BLOCKS.get(name).get(), block -> createSimpleTable("block", STORAGE_BLOCKS.get(name).get()));
        }
        for(String name: NC_ELECTROMAGNETS.keySet()) {
            add(NC_ELECTROMAGNETS.get(name).get(), block -> createSimpleTable("block", NC_ELECTROMAGNETS.get(name).get()));
        }
        for(String name: NC_RF_AMPLIFIERS.keySet()) {
            add(NC_RF_AMPLIFIERS.get(name).get(), block -> createSimpleTable("block", NC_RF_AMPLIFIERS.get(name).get()));
        }
    }

    @Override
    public void generate() {
        ores();
        blocks();
        machines();
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        List<Block> all = new ArrayList<>();
        all.addAll(ORE_BLOCKS.values().stream().map(RegistryObject::get).toList());
        all.addAll(NC_BLOCKS.values().stream().map(RegistryObject::get).toList());
        all.addAll(FISSION_BLOCKS.values().stream().map(RegistryObject::get).toList());
        all.addAll(FUSION_BLOCKS.values().stream().map(RegistryObject::get).toList());
        all.addAll(PROCESSORS.values().stream().map(RegistryObject::get).toList());
        all.addAll(ENERGY_BLOCKS.values().stream().map(RegistryObject::get).toList());
        all.addAll(STORAGE_BLOCKS.values().stream().map(RegistryObject::get).toList());
        all.addAll(NC_ELECTROMAGNETS.values().stream().map(RegistryObject::get).toList());
        all.addAll(NC_RF_AMPLIFIERS.values().stream().map(RegistryObject::get).toList());
        return all;
    }
}