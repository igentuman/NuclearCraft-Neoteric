package igentuman.nc.compat.ponder;

import igentuman.nc.compat.ponder.scenes.*;
import igentuman.nc.multiblock.fission.FissionTags;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.FusionReactor;
import igentuman.nc.setup.entries.HeatExchanger;
import igentuman.nc.setup.entries.Kugelblitz;
import igentuman.nc.setup.entries.Turbine;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class PonderScenes {

    public static final ResourceLocation FISSION_REACTOR = rl("fission_reactor");
    public static final ResourceLocation FUSION_REACTOR = rl("fusion_reactor");
    public static final ResourceLocation TURBINE = rl("turbine");
    public static final ResourceLocation TARGET_CHAMBER = rl("target_chamber");
    public static final ResourceLocation DECAY_CHAMBER = rl("decay_chamber");
    public static final ResourceLocation COLLISION_CHAMBER = rl("collision_chamber");
    public static final ResourceLocation LINEAR_ACCELERATOR = rl("linear_accelerator");
    public static final ResourceLocation RING_ACCELERATOR = rl("ring_accelerator");
    public static final ResourceLocation BEAM_DIVERTER = rl("beam_diverter");
    public static final ResourceLocation KUGELBLITZ_CHAMBER = rl("kugelblitz_chamber");
    public static final ResourceLocation HEAT_EXCHANGER = rl("heat_exchanger");
    public static final ResourceLocation MOLTEN_SALT_REACTOR = rl("molten_salt_reactor");

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);
        List<Item> fissionItems = new ArrayList<>(List.of(
                ModEntries.get("fission_reactor_casing").item().get(),
                ModEntries.get("fission_reactor_irradiation_chamber").item().get(),
                ModEntries.get("fission_reactor_pile-driver_irradiation_chamber").item().get(),
                ModEntries.get("irradiator").item().get(),
                ModEntries.get("fission_reactor_glass").item().get(),
                ModEntries.get("fission_reactor_controller").item().get(),
                ModEntries.get("fission_reactor_port").item().get()
        ));
        HashSet<Block> moderators = getBlocksByTagKey(FissionTags.MODERATORS.location().toString());
        for (Block block : moderators) {
            fissionItems.add(block.asItem());
        }
        for (HeatSinkEntry hs : ModEntries.HEAT_SINKS.values()) {
            if (hs.isEnabled()) fissionItems.add(hs.block().get().asItem());
        }
        HELPER.forComponents(
                fissionItems
        ).addStoryBoard(FISSION_REACTOR, FissionReactorPonderScenes::create);


        List<Item> fusionItems = new ArrayList<>(List.of(
                ModEntries.get("fusion_reactor_core").item().get(),
                ModEntries.get("fusion_reactor_connector").item().get(),
                ModEntries.get("fusion_reactor_casing").item().get(),
                ModEntries.get("fusion_reactor_glass").item().get()
        ));
        for (String t : FusionReactor.MAGNET_TIERS) {
            ModEntry entry = ModEntries.get(t + "_electromagnet");
            if (entry != null && entry.hasItem()) fusionItems.add(entry.item().get());
        }
        for (String t : FusionReactor.MAGNET_TIERS) {
            ModEntry entry = ModEntries.get(t + "_rf_amplifier");
            if (entry != null && entry.hasItem()) fusionItems.add(entry.item().get());
        }
        HELPER.forComponents(
                fusionItems
        ).addStoryBoard(FUSION_REACTOR, FusionReactorPonderScenes::create);


        List<Item> turbineItems = new ArrayList<>();
        for (String name : Turbine.TAB_BLOCKS) {
            ModEntry entry = ModEntries.get(name);
            if (entry != null && entry.hasItem()) turbineItems.add(entry.item().get());
        }

        HELPER.forComponents(
                turbineItems
        ).addStoryBoard(TURBINE, TurbinePonderScenes::create);


        List<Item> heatExchangerItems = new ArrayList<>();
        for (String name : HeatExchanger.TAB_BLOCKS) {
            ModEntry entry = ModEntries.get(name);
            if (entry != null && entry.hasItem()) heatExchangerItems.add(entry.item().get());
        }

        HELPER.forComponents(
                heatExchangerItems
        ).addStoryBoard(HEAT_EXCHANGER, HeatExchangerPonderScenes::create);


        List<Item> msrItems = new ArrayList<>(List.of(
                ModEntries.get("msr_controller").item().get(),
                ModEntries.get("msr_fuel_cell").item().get(),
                ModEntries.get("msr_port").item().get()
        ));

        HELPER.forComponents(
                msrItems
        ).addStoryBoard(MOLTEN_SALT_REACTOR, MoltenSaltReactorPonderScenes::create);


        List<Item> kugelblitzChamberItems = new ArrayList<>();
        for (String name : Kugelblitz.TAB_BLOCKS) {
            ModEntry entry = ModEntries.get(name);
            if (entry != null && entry.hasItem()) kugelblitzChamberItems.add(entry.item().get());
        }

        HELPER.forComponents(
                kugelblitzChamberItems
        ).addStoryBoard(KUGELBLITZ_CHAMBER, KugelblitzChamberPonderScenes::create);
    }
}
