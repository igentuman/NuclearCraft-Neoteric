package igentuman.nc.datagen;

import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCBlocks.NC_RF_AMPLIFIERS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCKS;
import static igentuman.nc.setup.registration.Tags.BLOCK_TAGS;
import static igentuman.nc.setup.registration.Tags.ORE_TAGS;

public class NCBlockTags extends BlockTagsProvider {

    public NCBlockTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ores();
        blocks();
        machines();
        tag(FissionBlocks.MODERATORS_BLOCKS).add(NCBlocks.NC_BLOCKS.get("graphite").get(), NCBlocks.NC_BLOCKS.get("beryllium").get());
        tag(FissionBlocks.CASING_BLOCKS).add(
                FISSION_BLOCKS.get("fission_reactor_casing").get(),
                FISSION_BLOCKS.get("fission_reactor_controller").get(),
                FISSION_BLOCKS.get("fission_reactor_glass").get(),
                FISSION_BLOCKS.get("fission_reactor_port").get(),
                NCProcessors.PROCESSORS.get("irradiator").get()
                );
        tag(FissionBlocks.HEAT_SINK_BLOCKS).add(FissionReactor.getHSBlocks());
        tag(FissionBlocks.INNER_REACTOR_BLOCKS)
                .add(FissionReactor.getHSBlocks())
                .add(
                        NCBlocks.NC_BLOCKS.get("graphite").get(),
                        NCBlocks.NC_BLOCKS.get("beryllium").get(),
                        FISSION_BLOCKS.get("fission_reactor_irradiation_chamber").get(),
                        FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get()
                );
        tag(TurbineRegistration.CASING_BLOCKS).add(
                TURBINE_BLOCKS.get("turbine_casing").get(),
                TURBINE_BLOCKS.get("turbine_glass").get(),
                TURBINE_BLOCKS.get("turbine_bearing").get(),
                TURBINE_BLOCKS.get("turbine_controller").get(),
                TURBINE_BLOCKS.get("turbine_port").get()
        );
        tag(TurbineRegistration.CASING_BLOCKS).add(
                TurbineRegistration.getCoilBlocks()
        );
        tag(TurbineRegistration.INNER_TURBINE_BLOCKS).add(
                TURBINE_BLOCKS.get("turbine_rotor_shaft").get()
        );
        tag(TurbineRegistration.INNER_TURBINE_BLOCKS).add(
                TurbineRegistration.getBladeBlocks()
        );
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(Tags.Blocks.ORES).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(ORE_TAGS.get(ore.replaceAll("_deepslate|_end|_nether","")))
                    .add(NCBlocks.ORE_BLOCKS.get(ore).get());
        }
    }

    private void blocks() {
        for(String block: NCBlocks.NC_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCBlocks.NC_BLOCKS.get(block).get());
            tag(Tags.Blocks.STORAGE_BLOCKS).add(NCBlocks.NC_BLOCKS.get(block).get());
            if(BLOCK_TAGS.get(block) != null) {
                tag(BLOCK_TAGS.get(block)).add(NCBlocks.NC_BLOCKS.get(block).get());
            }
        }

    }

    private void machines() {
        for(String block: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCEnergyBlocks.ENERGY_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCEnergyBlocks.ENERGY_BLOCKS.get(block).get());
        }
        for(String block: STORAGE_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(STORAGE_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(STORAGE_BLOCKS.get(block).get());
        }
        for(String block: NCProcessors.PROCESSORS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCProcessors.PROCESSORS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NCProcessors.PROCESSORS.get(block).get());
        }
        for(String block: NC_ELECTROMAGNETS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NC_ELECTROMAGNETS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NC_ELECTROMAGNETS.get(block).get());
        }
        for(String block: NC_RF_AMPLIFIERS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NC_RF_AMPLIFIERS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(NC_RF_AMPLIFIERS.get(block).get());
        }
        for(String block: FISSION_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FISSION_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(FISSION_BLOCKS.get(block).get());
        }
        for(String block: FUSION_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FUSION_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(FUSION_BLOCKS.get(block).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FUSION_CORE_PROXY.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(FUSION_CORE_PROXY.get());
    }

    @Override
    public @NotNull String getName() {
        return "NuclearCraft Block Tags";
    }

}
