package igentuman.nc.datagen.tags;

import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.CASING_ITEMS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.MODERATORS_ITEMS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCBlocks.NC_RF_AMPLIFIERS;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.Tags.*;

public class NCItemTags extends ItemTagsProvider {

    public NCItemTags(DataGenerator generator, BlockTagsProvider blockTags, GatherDataEvent event) {
        super(generator.getPackOutput(), event.getLookupProvider(), blockTags.contentsGetter(),  MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ores();
        blocks();
        ingots();
        chunks();
        dusts();
        plates();
        nuggets();
        gems();
        parts();
        fuel();
        isotopes();
        disks();
        for(RegistryObject<Item> magnet: NC_ELECTROMAGNETS_ITEMS.values()) {
            tag(ELECTROMAGNETS_ITEMS).add(magnet.get());
            tag(AcceleratorRegistration.ACCELERATOR_INNER_ITEMS).add(
                    magnet.get()
            );
        }
        for(RegistryObject<Item> amplifier: NC_RF_AMPLIFIERS_ITEMS.values()) {
            tag(AMPLIFIERS_ITEMS).add(amplifier.get());
            tag(AcceleratorRegistration.ACCELERATOR_INNER_ITEMS).add(
                    amplifier.get()
            );
        }
        tag(FusionReactorRegistration.CASING_ITEMS).add(
                FusionReactorRegistration.FUSION_ITEMS.get("fusion_reactor_casing").get(),
                FusionReactorRegistration.FUSION_ITEMS.get("fusion_reactor_casing_glass").get());
        tag(MODERATORS_ITEMS).add(NC_BLOCKS_ITEMS.get("graphite").get(), NC_BLOCKS_ITEMS.get("beryllium").get());
        tag(CASING_ITEMS).add(
                FissionReactorRegistration.FISSION_BLOCK_ITEMS.get("fission_reactor_casing").get(),
                FissionReactorRegistration.FISSION_BLOCK_ITEMS.get("fission_reactor_controller").get(),
                FissionReactorRegistration.FISSION_BLOCK_ITEMS.get("fission_reactor_glass").get(),
                FissionReactorRegistration.FISSION_BLOCK_ITEMS.get("fission_reactor_port").get()
        );
    }

    private void disks() {
        for(String name: NC_RECORDS.keySet()) {
            tag(ItemTags.MUSIC_DISCS).add(NC_RECORDS.get(name).get());
        }
    }

    private void isotopes() {
        for(String name: FissionFuel.NC_ISOTOPES.keySet()) {
            tag(NC_ISOTOPE_TAG.get(name.replaceAll("_ox|_ni|_za", ""))).add(FissionFuel.NC_ISOTOPES.get(name).get());
        }
    }

    private void fuel() {
        for(List<String> name: FissionFuel.NC_FUEL.keySet()) {
            tag(REACTOR_FUEL_TAG.get(name.get(1)+name.get(2))).add(FissionFuel.NC_FUEL.get(name).get());
        }

        for(List<String> name: FissionFuel.NC_DEPLETED_FUEL.keySet()) {
            tag(REACTOR_DEPLETED_FUEL_TAG.get(name.get(1)+name.get(2))).add(FissionFuel.NC_DEPLETED_FUEL.get(name).get());
        }
    }

    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            tag(PARTS_TAG).add(NCItems.NC_PARTS.get(name).get());
        }
    }

    private void gems() {
        for(String name: NC_GEMS.keySet()) {
            tag(Tags.Items.GEMS).add(NC_GEMS.get(name).get());
            tag(GEMS_TAG.get(name)).add(NC_GEMS.get(name).get());
        }
    }

    private void ingots() {
        for(String name: NC_INGOTS.keySet()) {
            tag(Tags.Items.INGOTS).add(NC_INGOTS.get(name).get());
            tag(INGOTS_TAG.get(name)).add(NC_INGOTS.get(name).get());
        }
    }

    private void nuggets() {
        for(String name: NC_NUGGETS.keySet()) {
            tag(Tags.Items.NUGGETS).add(NC_NUGGETS.get(name).get());
            tag(NUGGETS_TAG.get(name)).add(NC_NUGGETS.get(name).get());
        }
    }

    private void plates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            tag(PLATE_TAG).add(NCItems.NC_PLATES.get(name).get());
            tag(PLATES_TAG.get(name)).add(NCItems.NC_PLATES.get(name).get());
        }
    }

    private void dusts() {
        for(String name: NC_DUSTS.keySet()) {
            tag(Tags.Items.DUSTS).add(NC_DUSTS.get(name).get());
            tag(DUSTS_TAG.get(name)).add(NC_DUSTS.get(name).get());
        }
        tag(DUSTS_TAG.get("salt")).add(NC_ITEMS.get("salt").get());
        tag(DUSTS_TAG.get("sodium_chloride")).add(NC_ITEMS.get("salt").get());
    }

    private void chunks() {
        for(String name: NC_CHUNKS.keySet()) {
            tag(Tags.Items.RAW_MATERIALS).add(NC_CHUNKS.get(name).get());
            tag(CHUNKS_TAG.get(name)).add(NC_CHUNKS.get(name).get());
        }
    }

    private void ores() {
        for(String ore: ORE_BLOCK_ITEMS.keySet()) {
            tag(Tags.Items.ORES).add(ORE_BLOCK_ITEMS.get(ore).get());
            tag(ORE_ITEM_TAGS.get(ore.replaceAll("_deepslate|_end|_nether", ""))).add(ORE_BLOCK_ITEMS.get(ore).get());
        }
    }

    private void blocks() {
        for(String name: NC_BLOCKS_ITEMS.keySet()) {
            tag(Tags.Items.STORAGE_BLOCKS).add(NC_BLOCKS_ITEMS.get(name).get());
            if(BLOCK_ITEM_TAGS.get(name) != null) {
                tag(BLOCK_ITEM_TAGS.get(name)).add(NC_BLOCKS_ITEMS.get(name).get());
            }
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Item Tags";
    }
}