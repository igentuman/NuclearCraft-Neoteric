package igentuman.nc.datagen;

import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
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
        ores();
        blocks();
        machines();
        tag(FissionBlocks.MODERATORS_BLOCKS).add(NCBlocks.NC_BLOCKS.get("graphite").get(), NCBlocks.NC_BLOCKS.get("beryllium").get());
        tag(FissionBlocks.CASING_BLOCKS).add(
                FissionReactor.MULTI_BLOCKS.get("fission_reactor_casing").get(),
                FissionReactor.MULTI_BLOCKS.get("fission_reactor_controller").get(),
                FissionReactor.MULTI_BLOCKS.get("fission_reactor_glass").get(),
                FissionReactor.MULTI_BLOCKS.get("fission_reactor_port").get()
                );
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(Tags.Blocks.ORES).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(NCBlocks.ORE_TAGS.get(ore.replaceAll("_deepslate|_end|_nether","")))
                    .add(NCBlocks.ORE_BLOCKS.get(ore).get());
        }
    }

    private void blocks() {
        for(String block: NCBlocks.NC_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(Tags.Blocks.STORAGE_BLOCKS).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(NCBlocks.BLOCK_TAGS.get(block)).add(NCBlocks.NC_BLOCKS.get(block).get());
        }

    }

    private void machines() {
        for(String block: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCEnergyBlocks.ENERGY_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCEnergyBlocks.ENERGY_BLOCKS.get(block).get());
        }
        for(String block: NCProcessors.PROCESSORS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCProcessors.PROCESSORS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCProcessors.PROCESSORS.get(block).get());
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Block Tags";
    }
}
