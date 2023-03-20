package igentuman.nc.datagen;

import igentuman.nc.setup.NCBlocks;
import igentuman.nc.setup.NCItems;
import igentuman.nc.setup.materials.Ingots;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;

public class NCItemTags extends ItemTagsProvider {

    public NCItemTags(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(generator, blockTags, MODID, helper);
    }

    @Override
    protected void addTags() {
        addOres();
        addBlocks();
        addIngots();
        addChunks();
        addDusts();
        addPlates();
        addNuggets();

    }

    private void addIngots() {
        for(String name: NCItems.NC_INGOTS.keySet()) {
            tag(Tags.Items.INGOTS).add(NCItems.NC_INGOTS.get(name).get());
            tag(NCItems.INGOTS_TAG.get(name)).add(NCItems.NC_INGOTS.get(name).get());
        }
    }

    private void addNuggets() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            tag(Tags.Items.NUGGETS).add(NCItems.NC_CHUNKS.get(name).get());
            tag(NCItems.CHUNKS_TAG.get(name)).add(NCItems.NC_CHUNKS.get(name).get());
        }
    }

    private void addPlates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            tag(NCItems.PLATE_TAG).add(NCItems.NC_PLATES.get(name).get());
            tag(NCItems.PLATES_TAG.get(name)).add(NCItems.NC_PLATES.get(name).get());
        }
    }

    private void addDusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            tag(Tags.Items.DUSTS).add(NCItems.NC_DUSTS.get(name).get());
            tag(NCItems.DUSTS_TAG.get(name)).add(NCItems.NC_DUSTS.get(name).get());
        }
    }

    private void addChunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            tag(Tags.Items.RAW_MATERIALS).add(NCItems.NC_CHUNKS.get(name).get());
            tag(NCItems.CHUNKS_TAG.get(name)).add(NCItems.NC_CHUNKS.get(name).get());
        }
    }

    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCK_ITEMS.keySet()) {
            tag(Tags.Items.ORES).add(NCBlocks.ORE_BLOCK_ITEMS.get(ore).get());
            tag(NCBlocks.ORE_ITEM_TAGS.get(ore.replaceAll("_deepslate|_end|_nether", ""))).add(NCBlocks.ORE_BLOCK_ITEMS.get(ore).get());
        }
    }

    private void addBlocks() {
        for(String name: NCBlocks.NC_BLOCKS_ITEMS.keySet()) {
            tag(Tags.Items.STORAGE_BLOCKS).add(NCBlocks.NC_BLOCKS_ITEMS.get(name).get());
            tag(NCBlocks.BLOCK_ITEM_TAGS.get(name)).add(NCBlocks.NC_BLOCKS_ITEMS.get(name).get());
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Item Tags";
    }
}