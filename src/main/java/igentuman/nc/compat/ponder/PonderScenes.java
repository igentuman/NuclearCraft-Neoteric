package igentuman.nc.compat.ponder;

import igentuman.nc.compat.ponder.scenes.*;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_ITEMS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCK_ITEMS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.getHSBlocks;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_ITEMS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_ITEMS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCK_ITEMS;
import static igentuman.nc.setup.registration.NCBlocks.NC_ELECTROMAGNETS;
import static igentuman.nc.setup.registration.NCBlocks.NC_RF_AMPLIFIERS;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSOR_BLOCKS_ITEMS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class PonderScenes {

    public static final ResourceLocation FISSION_REACTOR = rl("fission_reactor");
    public static final ResourceLocation FUSION_REACTOR = rl("fusion_reactor");
    public static final ResourceLocation TURBINE = rl("turbine");
    public static final ResourceLocation TARGET_CHAMBER = rl("target_chamber");
    public static final ResourceLocation LINEAR_ACCELERATOR = rl("linear_accelerator");
    public static final ResourceLocation RING_ACCELERATOR = rl("ring_accelerator");
    public static final ResourceLocation KUGELBLITZ_CHAMBER = rl("kugelblitz_chamber");

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);
        List<Item> fissionItems = new ArrayList<>(List.of(
                FISSION_BLOCK_ITEMS.get("fission_reactor_casing").get(),
                FISSION_BLOCK_ITEMS.get("fission_reactor_irradiation_chamber").get(),
                PROCESSOR_BLOCKS_ITEMS.get("irradiator").get(),
                FISSION_BLOCK_ITEMS.get("fission_reactor_glass").get(),
                FISSION_BLOCK_ITEMS.get("fission_reactor_controller").get(),
                FISSION_BLOCK_ITEMS.get("fission_reactor_port").get()
        ));
        HashSet<Block> moderators = getBlocksByTagKey(FissionReactorRegistration.MODERATORS_BLOCKS.location().toString());
        for (Block block : moderators) {
            fissionItems.add(block.asItem());
        }
        for (Block block : getHSBlocks()) {
            fissionItems.add(block.asItem());
        }
        HELPER.forComponents(
                fissionItems
        ).addStoryBoard(FISSION_REACTOR, FissionReactorPonderScenes::create);


        List<Item> fusionItems = new ArrayList<>(List.of(
                FUSION_ITEMS.get("fusion_core").get(),
                FUSION_ITEMS.get("fusion_reactor_connector").get(),
                FUSION_ITEMS.get("fusion_reactor_casing").get(),
                FUSION_ITEMS.get("fusion_reactor_casing_glass").get()
        ));
        for (RegistryObject<Block> block : NC_ELECTROMAGNETS.values()) {
            fusionItems.add(block.get().asItem());
        }
        for (RegistryObject<Block> block : NC_RF_AMPLIFIERS.values()) {
            fusionItems.add(block.get().asItem());
        }
        HELPER.forComponents(
                fusionItems
        ).addStoryBoard(FUSION_REACTOR, FusionReactorPonderScenes::create);


        List<Item> turbineItems = new ArrayList<>();
        TURBINE_BLOCK_ITEMS.values().forEach(entry -> turbineItems.add(entry.get()));

        HELPER.forComponents(
                turbineItems
        ).addStoryBoard(TURBINE, TurbinePonderScenes::create);


        List<Item> targetChamberItems = new ArrayList<>();
        PARTICLE_CHAMBER_BLOCKS.values().forEach(entry -> targetChamberItems.add(entry.get().asItem()));

        HELPER.forComponents(
                targetChamberItems
        ).addStoryBoard(TARGET_CHAMBER, TargetChamberPonderScenes::create);


        List<Item> linearAcceleratorItems = new ArrayList<>();
        ACCELERATOR_ITEMS.values().forEach(entry -> {
            if(!entry.get().asItem().toString().contains("ring")) {
                linearAcceleratorItems.add(entry.get());
            }
        });

        HELPER.forComponents(
                linearAcceleratorItems
        ).addStoryBoard(LINEAR_ACCELERATOR, LinearAcceleratorPonderScenes::create);

        List<Item> ringAcceleratorItems = new ArrayList<>();
        ACCELERATOR_ITEMS.values().forEach(entry -> {
            if(entry.get().asItem().toString().contains("ring")) {
                ringAcceleratorItems.add(entry.get());
            }
        });

        HELPER.forComponents(
                ringAcceleratorItems
        ).addStoryBoard(RING_ACCELERATOR, RingAcceleratorPonderScenes::create);

        List<Item> kugelblitzChamberItems = new ArrayList<>();
        KUGELBLITZ_ITEMS.values().forEach(entry -> kugelblitzChamberItems.add(entry.get()));

        HELPER.forComponents(
                kugelblitzChamberItems
        ).addStoryBoard(KUGELBLITZ_CHAMBER, KugelblitzChamberPonderScenes::create);
    }
}
