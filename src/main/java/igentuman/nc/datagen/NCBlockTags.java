package igentuman.nc.datagen;

import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCK;

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
                FissionReactor.MULTI_BLOCKS.get("fission_reactor_port").get(),
                NCProcessors.PROCESSORS.get("irradiator").get()
                );
        tag(FissionBlocks.HEAT_SINK_BLOCKS).add(FissionReactor.getHSBlocks());
        tag(FissionBlocks.INNER_REACTOR_BLOCKS)
                .add(FissionReactor.getHSBlocks())
                .add(
                        NCBlocks.NC_BLOCKS.get("graphite").get(),
                        NCBlocks.NC_BLOCKS.get("beryllium").get(),
                        FissionReactor.MULTI_BLOCKS.get("fission_reactor_irradiation_chamber").get(),
                        FissionReactor.MULTI_BLOCKS.get("fission_reactor_solid_fuel_cell").get()
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
            if(NCBlocks.BLOCK_TAGS.get(block) != null) {
                tag(NCBlocks.BLOCK_TAGS.get(block)).add(NCBlocks.NC_BLOCKS.get(block).get());
            }
        }

    }

    private void machines() {
        for(String block: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCEnergyBlocks.ENERGY_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCEnergyBlocks.ENERGY_BLOCKS.get(block).get());
        }
        for(String block: STORAGE_BLOCK.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(STORAGE_BLOCK.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(STORAGE_BLOCK.get(block).get());
        }
        for(String block: NCProcessors.PROCESSORS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCProcessors.PROCESSORS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCProcessors.PROCESSORS.get(block).get());
        }
        for(String block: FUSION_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FUSION_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(FUSION_BLOCKS.get(block).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FUSION_CORE_PROXY.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(FUSION_CORE_PROXY.get());
        for(String block: NC_ELECTROMAGNETS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NC_ELECTROMAGNETS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NC_ELECTROMAGNETS.get(block).get());
        }
    }

    @Override
    public String getName() {
        return "NuclearCraft Block Tags";
    }
}
