package igentuman.nc.datagen;

import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class NCItemTags extends ItemTagsProvider {

    public NCItemTags(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(generator, blockTags, MODID, helper);
    }

    @Override
    protected void addTags() {
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
        tag(FissionBlocks.MODERATORS_ITEMS).add(NCBlocks.NC_BLOCKS_ITEMS.get("graphite").get(), NCBlocks.NC_BLOCKS_ITEMS.get("beryllium").get());
        tag(FissionBlocks.CASING_ITEMS).add(
                FissionReactor.MULTIBLOCK_ITEMS.get("fission_reactor_casing").get(),
                FissionReactor.MULTIBLOCK_ITEMS.get("fission_reactor_controller").get(),
                FissionReactor.MULTIBLOCK_ITEMS.get("fission_reactor_glass").get(),
                FissionReactor.MULTIBLOCK_ITEMS.get("fission_reactor_port").get()
        );
    }

    private void isotopes() {
        for(String name: Fuel.NC_ISOTOPES.keySet()) {
            tag(Fuel.ISOTOPE_TAG).add(Fuel.NC_ISOTOPES.get(name).get());
            //tag(NCItems.PLATES_TAG.get(name)).add(NCItems.NC_PLATES.get(name).get());
        }
    }

    private void fuel() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            tag(Fuel.NC_FUEL_TAG).add(Fuel.NC_FUEL.get(name).get());
            //tag(NCItems.PLATES_TAG.get(name)).add(NCItems.NC_PLATES.get(name).get());
        }

        for(List<String> name: Fuel.NC_DEPLETED_FUEL.keySet()) {
            tag(Fuel.NC_DEPLETED_FUEL_TAG).add(Fuel.NC_DEPLETED_FUEL.get(name).get());
        }
    }

    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            tag(NCItems.PARTS_TAG).add(NCItems.NC_PARTS.get(name).get());
        }
    }

    private void gems() {
        for(String name: NCItems.NC_GEMS.keySet()) {
            tag(Tags.Items.GEMS).add(NCItems.NC_GEMS.get(name).get());
            tag(NCItems.GEMS_TAG.get(name)).add(NCItems.NC_GEMS.get(name).get());
        }
    }

    private void ingots() {
        for(String name: NCItems.NC_INGOTS.keySet()) {
            tag(Tags.Items.INGOTS).add(NCItems.NC_INGOTS.get(name).get());
            tag(NCItems.INGOTS_TAG.get(name)).add(NCItems.NC_INGOTS.get(name).get());
        }
    }

    private void nuggets() {
        for(String name: NCItems.NC_NUGGETS.keySet()) {
            tag(Tags.Items.NUGGETS).add(NCItems.NC_NUGGETS.get(name).get());
            tag(NCItems.NUGGETS_TAG.get(name)).add(NCItems.NC_NUGGETS.get(name).get());
        }
    }

    private void plates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            tag(NCItems.PLATE_TAG).add(NCItems.NC_PLATES.get(name).get());
            tag(NCItems.PLATES_TAG.get(name)).add(NCItems.NC_PLATES.get(name).get());
        }
    }

    private void dusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            tag(Tags.Items.DUSTS).add(NCItems.NC_DUSTS.get(name).get());
            tag(NCItems.DUSTS_TAG.get(name)).add(NCItems.NC_DUSTS.get(name).get());
        }
    }

    private void chunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            tag(Tags.Items.RAW_MATERIALS).add(NCItems.NC_CHUNKS.get(name).get());
            tag(NCItems.CHUNKS_TAG.get(name)).add(NCItems.NC_CHUNKS.get(name).get());
        }
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCK_ITEMS.keySet()) {
            tag(Tags.Items.ORES).add(NCBlocks.ORE_BLOCK_ITEMS.get(ore).get());
            tag(NCBlocks.ORE_ITEM_TAGS.get(ore.replaceAll("_deepslate|_end|_nether", ""))).add(NCBlocks.ORE_BLOCK_ITEMS.get(ore).get());
        }
    }

    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS_ITEMS.keySet()) {
            tag(Tags.Items.STORAGE_BLOCKS).add(NCBlocks.NC_BLOCKS_ITEMS.get(name).get());
            if(NCBlocks.BLOCK_ITEM_TAGS.get(name) != null) {
                tag(NCBlocks.BLOCK_ITEM_TAGS.get(name)).add(NCBlocks.NC_BLOCKS_ITEMS.get(name).get());
            }
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Item Tags";
    }
}