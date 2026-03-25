package igentuman.nc;

import igentuman.nc.handler.command.*;
import igentuman.nc.handler.config.*;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.network.PacketHandler;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.setup.ClientSetup;
import igentuman.nc.setup.ModSetup;
import igentuman.nc.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static igentuman.nc.handler.config.CommonConfig.MISC_CONFIG;
import static igentuman.nc.util.FileExtractor.preFetchProcessorsConfig;
import static igentuman.nc.util.FileExtractor.unpackFilesFromFolderToConfig;

@Mod(NuclearCraft.MODID)
public class NuclearCraft {

    public static final Logger LOGGER = LogManager.getLogger();
    public boolean isNcBeStopped = false;
    public static final String MODID = "nuclearcraft";
    public static NuclearCraft instance;
    private final PacketHandler packetHandler;
    private static boolean isBetaBuild = false;
    public static long currentTick = 0;

    public static void registerConfigs(ModContainer container) {
        preFetchProcessorsConfig();
        unpackFilesFromFolderToConfig("data/nuclearcraft/fission_fuel", "NuclearCraft/fission_fuel");
        unpackFilesFromFolderToConfig("data/nuclearcraft/heat_sinks", "NuclearCraft/heat_sinks");
        unpackFilesFromFolderToConfig("data/nuclearcraft/accelerator_coolers", "NuclearCraft/accelerator_coolers");

        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.spec, "NuclearCraft/client.toml");
        container.registerConfig(ModConfig.Type.COMMON, MaterialsConfig.spec, "NuclearCraft/materials.toml");
        container.registerConfig(ModConfig.Type.COMMON, OreGenConfig.spec, "NuclearCraft/ore_generation.toml");
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.spec, "NuclearCraft/common.toml");
        container.registerConfig(ModConfig.Type.COMMON, KugelblitzConfig.spec, "NuclearCraft/kugelblitz.toml");
        container.registerConfig(ModConfig.Type.COMMON, AcceleratorConfig.spec, "NuclearCraft/accelerator.toml");
        container.registerConfig(ModConfig.Type.COMMON, ProcessorsConfig.spec, "NuclearCraft/processors.toml");
        container.registerConfig(ModConfig.Type.COMMON, FissionConfig.spec, "NuclearCraft/fission.toml");
        container.registerConfig(ModConfig.Type.COMMON, FusionConfig.spec, "NuclearCraft/fusion.toml");
        container.registerConfig(ModConfig.Type.COMMON, TurbineConfig.spec, "NuclearCraft/turbine.toml");
        container.registerConfig(ModConfig.Type.COMMON, RadiationConfig.spec, "NuclearCraft/radiation.toml");
        container.registerConfig(ModConfig.Type.COMMON, WorldConfig.spec, "NuclearCraft/world.toml");
    }

    public NuclearCraft(IEventBus modbus, ModContainer container) {
        instance = this;
        registerConfigs(container);
        packetHandler = new PacketHandler();
        NeoForge.EVENT_BUS.addListener(this::serverStopped);
        NeoForge.EVENT_BUS.addListener(this::serverStarted);
        NeoForge.EVENT_BUS.addListener(this::gameShuttingDownEvent);
        ModSetup.setup();
        Registration.init(modbus);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        modbus.addListener(ModSetup::init);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modbus.addListener(ClientSetup::init);
            modbus.addListener(ClientSetup::registerEventHandlers);
        }
        modbus.addListener(this::onModConfigEvent);
        try {
            isBetaBuild = ModList.get().getModFileById("nuclearcraft").getMods().get(0).getVersion().getQualifier().contains("beta");
        } catch (Exception e) {
            isBetaBuild = false;
        }
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
            CommonConfig.setLoaded();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(VeinCheckCommand.register());
        event.getDispatcher().register(PatronsCommand.register());
        StructureCommand.register(event.getDispatcher());
        RadiationCommand.register(event.getDispatcher());
        DebugCommand.register(event.getDispatcher());
        FuelModelsCommand.register(event.getDispatcher());
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation neoforgeRl(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    public static ResourceLocation commonRl(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }

    public static ResourceLocation resourceLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }

    private void serverStopped(ServerStoppedEvent event) {
        NuclearCraft.instance.isNcBeStopped = true;
        RadiationEvents.stopTracking();
        for (ServerLevel level : event.getServer().getAllLevels()) {
            MultiblockHandler.get(level.dimension()).clear();
            RadiationManager.clear(level);
        }
    }

    private void gameShuttingDownEvent(GameShuttingDownEvent event) {
        NuclearCraft.instance.isNcBeStopped = true;
    }

    private void serverStarted(ServerStartedEvent event) {
        NuclearCraft.instance.isNcBeStopped = false;
        currentTick = 0;
        RadiationEvents.startTracking();
    }

    public static void debugLog(String message) {
        if (MISC_CONFIG.DEBUG_LOG.get() || isBetaBuild) {
            LOGGER.info(message);
        }
    }
}
