package igentuman.nc;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.setup.entries.Anomalies;
import igentuman.nc.setup.entries.Ghouls;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.config.*;
import igentuman.nc.compat.cc.CCCompatHandler;
import igentuman.nc.handler.event.PipeEvents;
import igentuman.nc.handler.event.ServerEvents;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.network.*;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.item.HEVItem;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.NcParticles;
import igentuman.nc.setup.Registers;
import igentuman.nc.setup.entries.Crafter;
import igentuman.nc.setup.entries.Energy;
import igentuman.nc.setup.entries.Pipes;
import igentuman.nc.setup.entries.Storage;
import igentuman.nc.setup.level.ModFeatures;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import static igentuman.nc.config.Common.DEBUG_LOGGING;

@Mod(NuclearCraft.MODID)
public class NuclearCraft {
    public static final String MODID = "nuclearcraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static int TICK_COUNTER = 0;

    public NuclearCraft(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerTicketControllers);
        Registers.init(modEventBus);
        ModFeatures.init();
        NcParticles.init();
        NCSounds.init();
        ModEntries.init();
        NeoForge.EVENT_BUS.register(new ServerEvents());
        NeoForge.EVENT_BUS.register(new PipeEvents());
        WorldGen.init(
                ModEntries.ENTRIES.values().stream()
                        .map(ModEntry::materialEntry)
                        .filter(mat -> mat != null && mat.hasWorldgenConfig())
                        .toList()
        );
        modEventBus.addListener(this::registerEntityAttributes);
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
        modContainer.registerConfig(ModConfig.Type.COMMON, igentuman.nc.config.Q36.SPEC, MODID + "/q36.toml");
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
        Pipes.registerCapabilities(event);

        int qnpCap = Common.QNP_ENERGY_STORAGE.get();
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> new net.neoforged.neoforge.energy.ComponentEnergyStorage(
                        stack, Registers.QNP_ENERGY.get(), qnpCap, qnpCap, qnpCap / 4),
                ModEntries.get("qnp").item().get());

        int hevCap = Common.HEV_ENERGY_STORAGE.get();
        var hevArmor = ModEntries.get("hev_armor").armorSetEntry();
        var hevPieces = java.util.List.of(hevArmor.helmet(), hevArmor.chestplate(), hevArmor.leggings(), hevArmor.boots());
        for (var piece : hevPieces) {
            event.registerItem(Capabilities.EnergyStorage.ITEM,
                    (stack, ctx) -> new net.neoforged.neoforge.energy.ComponentEnergyStorage(
                            stack, Registers.HEV_ENERGY.get(), hevCap, 5000, hevCap / 4),
                    piece.get());
        }
        net.minecraft.world.level.material.Fluid quantiteFluid = ModEntries.fluidOf("quantite_energy");
        if (quantiteFluid != null) {
            for (var piece : hevPieces) {
                event.registerItem(Capabilities.FluidHandler.ITEM,
                        (stack, ctx) -> new IFluidHandlerItem() {
                            @Override public net.minecraft.world.item.ItemStack getContainer() { return stack; }
                            @Override public int getTanks() { return 1; }
                            @Override public FluidStack getFluidInTank(int tank) {
                                int qe = HEVItem.getQeCharge(stack);
                                return qe > 0 ? new FluidStack(quantiteFluid, qe) : FluidStack.EMPTY;
                            }
                            @Override public int getTankCapacity(int tank) { return HEVItem.MAX_QE_CHARGE; }
                            @Override public boolean isFluidValid(int tank, FluidStack fs) { return fs.getFluid() == quantiteFluid; }
                            @Override public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
                                if (resource.getFluid() != quantiteFluid) return 0;
                                int currentQe = HEVItem.getQeCharge(stack);
                                int toFill = Math.min(resource.getAmount(), HEVItem.MAX_QE_CHARGE - currentQe);
                                if (action.execute() && toFill > 0) HEVItem.setQeCharge(stack, currentQe + toFill);
                                return toFill;
                            }
                            @Override public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) { return FluidStack.EMPTY; }
                            @Override public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) { return FluidStack.EMPTY; }
                        },
                        piece.get());
            }
        }

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

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(Anomalies.GRAVITATIONAL_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(Anomalies.ELECTRIC_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(Anomalies.RADIOACTIVE_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(Anomalies.BURNING_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(Anomalies.PSYCHO_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(Anomalies.TELEPORTING_ANOMALY.get(), AnomalyEntity.createAttributes().build());
        event.put(Ghouls.FERAL_GHOUL.get(), EntityFeralGhoul.createAttributes().build());
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
                    if (mat.hasDeepslateOre() && Materials.isTypeEnabled(matName, "deepslate_ore")) event.accept(mat.deepslateOreItem());
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
                if (entry.item().get() instanceof igentuman.nc.item.QNPItem) {
                    if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                        event.accept(entry.item());
                    }
                    continue;
                }
                if (entry.item().get() instanceof igentuman.nc.item.Q36Item) {
                    if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                        event.accept(entry.item());
                    }
                    continue;
                }
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
