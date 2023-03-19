package igentuman.nc.datagen;

import igentuman.nc.setup.NCBlocks;
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

    }

    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCK_ITEMS.keySet()) {
            tag(Tags.Items.ORES)
                    .add(NCBlocks.ORE_BLOCK_ITEMS.get(ore).get());
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Item Tags";
    }
}