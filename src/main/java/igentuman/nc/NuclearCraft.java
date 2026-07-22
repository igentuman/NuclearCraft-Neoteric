package igentuman.nc;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.config.*;
import igentuman.nc.compat.cc.CCCompatHandler;
import igentuman.nc.handler.event.ServerEvents;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.network.*;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.NcParticles;
import igentuman.nc.setup.Registers;
import igentuman.nc.setup.entries.Crafter;
import igentuman.nc.setup.entries.Energy;
import igentuman.nc.setup.entries.Storage;
import igentuman.nc.util.MultiblocksProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static igentuman.nc.config.Common.DEBUG_LOGGING;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
/** Mod entrypoint: bootstraps registries, entries, worldgen, capabilities, network payloads and creative tabs. */
@Mod(NuclearCraft.MODID)
public class NuclearCraft {
    public static final String MODID = "nuclearcraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static int TICK_COUNTER = 0;

    // Creates a creative tab with the id "nc:example_tab" for the example item, that is placed after the combat tab
/*    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nc")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(EXAMPLE_MACHINE_BLOCK_ITEM.get());
            }).build());*/

    public NuclearCraft(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerTicketControllers);
        Registers.init(modEventBus);
        NcParticles.init();
        NCSounds.init();
        ModEntries.init();
        NeoForge.EVENT_BUS.register(new ServerEvents());
        WorldGen.init(
                ModEntries.ENTRIES.values().stream()
                        .map(ModEntry::materialEntry)
                        .filter(mat -> mat != null && mat.hasWorldgenConfig())
                        .toList()
        );
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(Networking::registerPayloads);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::registerClientReloadListeners);
        }
        if (ModList.get().isLoaded("computercraft")) {
            CCCompatHandler.register(modEventBus);
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, Common.SPEC, MODID + "/common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, WorldGen.SPEC, MODID + "/worldgen.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Materials.SPEC, MODID + "/materials.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Entries.SPEC, MODID + "/entries.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Multiblocks.SPEC, MODID + "/multiblocks.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, igentuman.nc.config.Bomb.SPEC, MODID + "/bomb.toml");
    }



    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlockEntity() && entry.itemCap() != null) {
                event.registerBlockEntity(
                        Capabilities.ItemHandler.BLOCK,
                        entry.blockEntity().get(),
                        (be, side) -> {
                            if (be instanceof GlobalBlockEntity gbe) {
                                return gbe.getItemHandler(side);
                            }
                            return null;
                        }
                );
            }
            if (entry.hasBlockEntity() && entry.fluidCap() != null) {
                event.registerBlockEntity(
                        Capabilities.FluidHandler.BLOCK,
                        entry.blockEntity().get(),
                        (be, side) -> {
                            if (be instanceof GlobalBlockEntity gbe) {
                                return gbe.getFluidHandler(side);
                            }
                            return null;
                        }
                );
            }
            if (entry.hasBlockEntity() && entry.energyCap() != null) {
                event.registerBlockEntity(
                        Capabilities.EnergyStorage.BLOCK,
                        entry.blockEntity().get(),
                        (be, side) -> {
                            if (be instanceof GlobalBlockEntity gbe) {
                                return gbe.getEnergyHandler(side);
                            }
                            return null;
                        }
                );
            }
        }

        Storage.registerCapabilities(event);
        Crafter.registerCapabilities(event);
        Energy.registerCapabilities(event);

        // Multiblock ports proxy capabilities from their controller. The port's own ModEntry
        // has no cap definitions, so register caps here unconditionally for every port BE type.
        for (MultiblockEntry mb : MultiblockRegistry.ENTRIES.values()) {
            for (ModEntry port : mb.portEntries()) {
                if (!port.hasBlockEntity()) continue;
                event.registerBlockEntity(
                        Capabilities.ItemHandler.BLOCK,
                        port.blockEntity().get(),
                        (be, side) -> be instanceof MultiblockPortBE part ? part.getItemHandler(side) : null
                );
                event.registerBlockEntity(
                        Capabilities.FluidHandler.BLOCK,
                        port.blockEntity().get(),
                        (be, side) -> be instanceof MultiblockPortBE part ? part.getFluidHandler(side) : null
                );
                event.registerBlockEntity(
                        Capabilities.EnergyStorage.BLOCK,
                        port.blockEntity().get(),
                        (be, side) -> be instanceof MultiblockPortBE part ? part.getEnergyHandler(side) : null
                );
            }
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(igentuman.nc.setup.entries.Bomb.TICKET_CONTROLLER);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == Common.SPEC) Common.refreshTagPriority();
        if (event.getConfig().getSpec() == Multiblocks.SPEC) Multiblocks.refresh();
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == Common.SPEC) Common.refreshTagPriority();
        if (event.getConfig().getSpec() == Multiblocks.SPEC) Multiblocks.refresh();
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(MultiblocksProvider.getInstance());
    }

    private void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(MultiblocksProvider.getInstance());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        for (ModEntry entry: ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() != null) {
                var mat = entry.materialEntry();
                String matName = mat.name;
                if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                    if (mat.hasOre() && Materials.isTypeEnabled(matName, "ore")) event.accept(mat.oreItem());
                    if (mat.hasRawOre() && Materials.isTypeEnabled(matName, "raw_ore")) event.accept(mat.rawOre());
                }
                if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                    if (mat.hasBlock() && Materials.isTypeEnabled(matName, "block")) event.accept(mat.storageItem());
                }
                if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                    if (mat.hasIngot() && Materials.isTypeEnabled(matName, "ingot")) event.accept(mat.ingot());
                    if (mat.hasGem() && Materials.isTypeEnabled(matName, "gem")) event.accept(mat.gem());
                    if (mat.hasDust() && Materials.isTypeEnabled(matName, "dust")) event.accept(mat.dust());
                    if (mat.hasPlate() && Materials.isTypeEnabled(matName, "plate")) event.accept(mat.plate());
                    if (mat.hasNugget() && Materials.isTypeEnabled(matName, "nugget")) event.accept(mat.nugget());
                }
                if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                    if (mat.hasFluid() && Materials.isTypeEnabled(matName, "fluid")) event.accept(mat.bucket());
                }
                continue;
            }
            if (!entry.isEnabled()) continue;
            if(entry.hasBlockEntity()) {
                if (entry.hasBlock()) {
                    var block = entry.block().get();
                    if (block instanceof MultiblockControllerBlock || block instanceof MultiblockPartBlock) {
                        continue;
                    }
                }
                if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                    event.accept(entry.item());
                }
                continue;
            }
            if(entry.hasBlock()) {
                if (entry.block().get() instanceof MultiblockBlock) continue;
                if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                    event.accept(entry.item());
                }
                continue;
            }
            if (entry.hasToolSet()) {
                var tools = entry.toolSetEntry();
                if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                    event.accept(tools.sword());
                }
                if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                    event.accept(tools.pickaxe());
                    event.accept(tools.axe());
                    event.accept(tools.shovel());
                    event.accept(tools.hoe());
                }
                continue;
            }
            if (entry.hasArmorSet()) {
                var armor = entry.armorSetEntry();
                if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                    event.accept(armor.helmet());
                    event.accept(armor.chestplate());
                    event.accept(armor.leggings());
                    event.accept(armor.boots());
                }
                continue;
            }
            if(entry.hasItem()) {
                if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                    event.accept(entry.item());
                }
                continue;
            }
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
                isotope.variants().values().forEach(item -> event.accept(item));
            }
            for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
                if (!fuel.isEnabled()) continue;
                fuel.fuelItems().values().forEach(item -> event.accept(item));
                fuel.depletedItems().values().forEach(item -> event.accept(item));
            }
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
                if (!fuel.isEnabled()) continue;
                for (MaterialEntry mat : fuel.fluids()) {
                    event.accept(mat.bucket());
                }
            }
            for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
                for (MaterialEntry mat : isotope.fluids()) {
                    event.accept(mat.bucket());
                }
            }
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation rlFromString(String name) {
        return ResourceLocation.tryParse(name);
    }

    public static void debugLog(String message) {
        if (DEBUG_LOGGING.get()) {
            NuclearCraft.LOGGER.debug(message);
        }
    }
}
