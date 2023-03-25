package igentuman.nc.datagen;

import igentuman.nc.setup.registration.NCBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;

public class NCBlockTags extends BlockTagsProvider {

    public NCBlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, MODID, helper);
    }

    @Override
    protected void addTags() {
        addOres();
        addBlocks();

    }

    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(Tags.Blocks.ORES).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(NCBlocks.ORE_TAGS.get(ore.replaceAll("_deepslate|_end|_nether","")))
                    .add(NCBlocks.ORE_BLOCKS.get(ore).get());
        }
    }

    private void addBlocks() {
        for(String block: NCBlocks.NC_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(Tags.Blocks.STORAGE_BLOCKS).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(NCBlocks.BLOCK_TAGS.get(block)).add(NCBlocks.NC_BLOCKS.get(block).get());
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Block Tags";
    }
}
