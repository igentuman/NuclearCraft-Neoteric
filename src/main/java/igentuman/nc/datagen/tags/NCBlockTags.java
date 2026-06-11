package igentuman.nc.datagen.tags;

import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.*;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.*;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_CORE_PROXY;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.*;
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
        tag(MODERATORS_BLOCKS).add(NCBlocks.NC_MATERIAL_BLOCKS.get("graphite").get(), NCBlocks.NC_MATERIAL_BLOCKS.get("beryllium").get());
        tag(TARGET_CHAMBER_CASING_BLOCKS).add(
                TARGET_CHAMBER_BLOCKS.get("target_chamber_casing").get(),
                TARGET_CHAMBER_BLOCKS.get("target_chamber_casing_glass").get(),
                TARGET_CHAMBER_BLOCKS.get("target_chamber_controller").get(),
                TARGET_CHAMBER_BLOCKS.get("target_chamber_port").get(),
                TARGET_CHAMBER_BLOCKS.get("target_chamber_beam_port").get()
        );
        tag(TARGET_CHAMBER_INNER_BLOCKS).add(
                ACCELERATOR_BLOCKS.get("particle_beam").get(),
                TARGET_CHAMBER_BLOCKS.get("target_chamber_camera").get()
        );
        tag(KugelblitzRegistration.CASING_BLOCKS).add(
                KUGELBLITZ_BLOCKS.get("neutronium_frame").get(),
                KUGELBLITZ_BLOCKS.get("chamber_port").get(),
                KUGELBLITZ_BLOCKS.get("chamber_terminal").get(),
                KUGELBLITZ_BLOCKS.get("quantum_flux_regulator").get(),
                KUGELBLITZ_BLOCKS.get("quantum_transformer").get(),
                KUGELBLITZ_BLOCKS.get("photon_concentrator").get(),
                KUGELBLITZ_BLOCKS.get("event_horizon_stabilizer").get());
        tag(AcceleratorRegistration.ACCELERATOR_CASING_BLOCKS).add(
                ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get(),
                ACCELERATOR_BLOCKS.get("accelerator_casing").get(),
                ACCELERATOR_BLOCKS.get("accelerator_casing_glass").get(),
                ACCELERATOR_BLOCKS.get("ring_accelerator_controller").get(),
                ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get(),
                ACCELERATOR_BLOCKS.get("accelerator_port").get(),
                ACCELERATOR_BLOCKS.get("accelerator_beam_port").get(),
                ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get()
        );
        tag(AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS).add(
                ACCELERATOR_BLOCKS.get("particle_beam").get(),
                ACCELERATOR_BLOCKS.get("electromagnet_yoke").get()
        );
        for(RegistryObject<Block> magnet: NC_ELECTROMAGNETS.values()) {
            tag(AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS).add(
                    magnet.get()
            );
        }
        for(RegistryObject<Block> block: NC_RF_AMPLIFIERS.values()) {
            tag(AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS).add(
                    block.get()
            );
        }

        for(Block cooler: AcceleratorRegistration.getCoolerBlocks()) {
            tag(AcceleratorRegistration.ACCELERATOR_INNER_BLOCKS).add(
                    cooler
            );
        }
        for(RegistryObject<Block> magnet: NC_ELECTROMAGNETS.values()) {
            tag(ELECTROMAGNETS).add(
                    magnet.get()
            );
        }
        for(RegistryObject<Block> amplifier: NC_RF_AMPLIFIERS.values()) {
            tag(AMPLIFIERS).add(
                    amplifier.get()
            );
        }
        tag(FusionReactorRegistration.CASING_BLOCKS).add(
                FUSION_BLOCKS.get("fusion_reactor_casing").get(),
                FUSION_BLOCKS.get("fusion_reactor_casing_glass").get());
        tag(CASING_BLOCKS).add(
                FISSION_BLOCKS.get("msr_controller").get(),
                FISSION_BLOCKS.get("msr_port").get(),
                FISSION_BLOCKS.get("fission_reactor_casing").get(),
                FISSION_BLOCKS.get("fission_reactor_controller").get(),
                FISSION_BLOCKS.get("fission_reactor_glass").get(),
                FISSION_BLOCKS.get("fission_reactor_port").get(),
                NCProcessors.PROCESSORS.get("irradiator").get()
                );
        tag(HEAT_SINK_BLOCKS).add(FissionReactorRegistration.getHSBlocks());
        tag(INNER_REACTOR_BLOCKS)
                .add(FissionReactorRegistration.getHSBlocks())
                .add(
                        NCBlocks.NC_MATERIAL_BLOCKS.get("graphite").get(),
                        NCBlocks.NC_MATERIAL_BLOCKS.get("beryllium").get(),
                        FISSION_BLOCKS.get("fission_reactor_irradiation_chamber").get(),
                        FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get(),
                        FISSION_BLOCKS.get("msr_fuel_cell").get(),
                        FISSION_BLOCKS.get("heat_exchanger").get()
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
            if(ore.contains("platinum")) {
                tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ORE_BLOCKS.get(ore).get());
            } else {
                if(ore.matches("tin|lead")) {
                    tag(BlockTags.NEEDS_STONE_TOOL).add(ORE_BLOCKS.get(ore).get());
                } else {
                    tag(BlockTags.NEEDS_IRON_TOOL).add(ORE_BLOCKS.get(ore).get());
                }
            }
            tag(Tags.Blocks.ORES).add(NCBlocks.ORE_BLOCKS.get(ore).get());
            tag(ORE_TAGS.get(ore.replaceAll("_deepslate|_end|_nether","")))
                    .add(NCBlocks.ORE_BLOCKS.get(ore).get());
        }
    }

    private void blocks() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MULTIBLOCK_BUILDER_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(REDSTONE_DIMMER_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(CHARGING_STATION_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(PU_239_BOMB.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(REDSTONE_DIMMER_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(PU_239_BOMB.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(CHARGING_STATION_BLOCK.get());

        for(String block: NCBlocks.NC_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NCBlocks.NC_BLOCKS.get(block).get());
            if(block.contains("platinum")) {
                tag(BlockTags.NEEDS_DIAMOND_TOOL).add(NC_BLOCKS.get(block).get());
            } else {
                tag(BlockTags.NEEDS_IRON_TOOL).add(NC_BLOCKS.get(block).get());
            }
            tag(Tags.Blocks.STORAGE_BLOCKS).add(NCBlocks.NC_BLOCKS.get(block).get());
            if(BLOCK_TAGS.get(block) != null) {
                tag(BLOCK_TAGS.get(block)).add(NCBlocks.NC_BLOCKS.get(block).get());
            }
        }
        for(String block: NC_MATERIAL_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NC_MATERIAL_BLOCKS.get(block).get());
            if(block.contains("platinum")) {
                tag(BlockTags.NEEDS_DIAMOND_TOOL).add(NC_MATERIAL_BLOCKS.get(block).get());
            } else {
                tag(BlockTags.NEEDS_IRON_TOOL).add(NC_MATERIAL_BLOCKS.get(block).get());
            }
            tag(Tags.Blocks.STORAGE_BLOCKS).add(NC_MATERIAL_BLOCKS.get(block).get());
            if(BLOCK_TAGS.get(block) != null) {
                tag(BLOCK_TAGS.get(block)).add(NC_MATERIAL_BLOCKS.get(block).get());
            }
        }
        tag(DECAY_GEN_BLOCK).add(
                NC_MATERIAL_BLOCKS.get("uranium").get(),
                NC_MATERIAL_BLOCKS.get("uranium238").get(),
                NC_MATERIAL_BLOCKS.get("plutonium238").get(),
                NC_MATERIAL_BLOCKS.get("americium241").get()
                );

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
        for(String block: TURBINE_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TURBINE_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(TURBINE_BLOCKS.get(block).get());
        }
        for(String block: KUGELBLITZ_BLOCKS.keySet()) {
            if(block.equals("black_hole")) continue;
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(KUGELBLITZ_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_DIAMOND_TOOL).add(KUGELBLITZ_BLOCKS.get(block).get());
        }
        for(String block: ACCELERATOR_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ACCELERATOR_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(ACCELERATOR_BLOCKS.get(block).get());
        }
        for(String block: TARGET_CHAMBER_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TARGET_CHAMBER_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(TARGET_CHAMBER_BLOCKS.get(block).get());
        }
        for(String block: FUSION_BLOCKS.keySet()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FUSION_BLOCKS.get(block).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(FUSION_BLOCKS.get(block).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FUSION_CORE_PROXY.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(WASTELAND_EARTH.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(PORTAL_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(EXPL_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(EXPL_PROXY_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(FUSION_CORE_PROXY.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(WASTELAND_EARTH.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(PORTAL_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(EXPL_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(EXPL_PROXY_BLOCK.get());
    }

    @Override
    public @NotNull String getName() {
        return "NuclearCraft Block Tags";
    }

}
