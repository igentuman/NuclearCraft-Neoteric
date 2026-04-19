package igentuman.nc;

import igentuman.nc.content.particles.CapabilityParticleStackHandler;
import igentuman.nc.handler.command.*;
import igentuman.nc.handler.config.*;
import igentuman.nc.handler.event.server.WorldEvents;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.network.PacketHandler;
import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.radiation.data.WorldRadiation;
import igentuman.nc.setup.ClientSetup;
import igentuman.nc.setup.ModSetup;
import igentuman.nc.setup.Registration;
import igentuman.nc.util.insitu_leaching.WorldVeinOres;
import igentuman.nc.util.insitu_leaching.WorldVeinsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static igentuman.nc.handler.config.CommonConfig.MISC_CONFIG;
import static igentuman.nc.util.FileExtractor.preFetchProcessorsConfig;
import static igentuman.nc.util.FileExtractor.unpackFilesFromFolderToConfig;

@Mod(NuclearCraft.MODID)
public class NuclearCraft {

    public static final Logger LOGGER = LogManager.getLogger();
    public boolean isNcBeStopped = false;
    public static final WorldEvents worldTickHandler = new WorldEvents();
    public static final String MODID = "nuclearcraft";
    public static NuclearCraft instance;
    private final PacketHandler packetHandler;
    private static boolean isBetaBuild = false;
    public static long currentTick = 0;

    public static void registerConfigs(FMLJavaModLoadingContext context)
    {
        preFetchProcessorsConfig();
        unpackFilesFromFolderToConfig("data/nuclearcraft/fission_fuel", "NuclearCraft/fission_fuel");
        unpackFilesFromFolderToConfig("data/nuclearcraft/heat_sinks", "NuclearCraft/heat_sinks");
        unpackFilesFromFolderToConfig("data/nuclearcraft/accelerator_coolers", "NuclearCraft/accelerator_coolers");

        ModContainer container = ModLoadingContext.get().getActiveContainer();
        container.addConfig(new ModConfig(ModConfig.Type.CLIENT, ClientConfig.spec, container,"NuclearCraft/client.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, MaterialsConfig.spec, container,"NuclearCraft/materials.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, OreGenConfig.spec, container,"NuclearCraft/ore_generation.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, CommonConfig.spec, container,"NuclearCraft/common.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, KugelblitzConfig.spec, container,"NuclearCraft/kugelblitz.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, AcceleratorConfig.spec, container,"NuclearCraft/accelerator.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, ProcessorsConfig.spec, container,"NuclearCraft/processors.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, FissionConfig.spec, container,"NuclearCraft/fission.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, FusionConfig.spec, container,"NuclearCraft/fusion.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, TurbineConfig.spec, container,"NuclearCraft/turbine.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, RadiationConfig.spec, container,"NuclearCraft/radiation.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.COMMON, WorldConfig.spec, container,"NuclearCraft/world.toml"));
    }

    @Deprecated
    public NuclearCraft() {
        this(FMLJavaModLoadingContext.get());
    }

    public NuclearCraft(FMLJavaModLoadingContext context) {
        instance = this;
        IEventBus modbus = context.getModEventBus();
        registerConfigs(context);
        packetHandler = new PacketHandler();
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::gameShuttingDownEvent);
        ModSetup.setup();
        Registration.init(context);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        modbus.addListener(ModSetup::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(ClientSetup::init));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(this::registerClientEventHandlers));
        try {
            isBetaBuild = ModList.get().getModFileById("nuclearcraft").getMods().get(0).getVersion().getQualifier().contains("beta");
        } catch (Exception e) {
            isBetaBuild = false;
        }
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
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

    private void registerClientEventHandlers(FMLClientSetupEvent event) {
        ClientSetup.registerEventHandlers(event);
    }

    public static ResourceLocation rl(String path)
    {
        return ResourceLocation.tryBuild(MODID, path);
    }

    public static ResourceLocation forgeRl(String path)
    {
        return ResourceLocation.tryBuild("forge", path);
    }

    public static ResourceLocation resourceLoc(String path)
    {
        return ResourceLocation.tryBuild("c", path);
    }

    private void serverStopped(ServerStoppedEvent event) {
        NuclearCraft.instance.isNcBeStopped = true;
        //stop capability tracking
        RadiationEvents.stopTracking();
        WorldVeinsProvider.stopTracking();
        for(ServerLevel level: event.getServer().getAllLevels()) {
            MultiblockHandler.get(level.dimension()).clear();
            RadiationManager.clear(level);
        }
        MultiblockHandler.clearAll();
        RadiationManager.clearAll();
    }
    private void gameShuttingDownEvent(GameShuttingDownEvent event) {
        NuclearCraft.instance.isNcBeStopped = true;
    }

    private void serverStarted(ServerStartedEvent event) {
        NuclearCraft.instance.isNcBeStopped = false;
        currentTick = 0;
        RadiationEvents.startTracking();
        WorldVeinsProvider.startTracking();
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(PlayerRadiation.class);
        event.register(WorldVeinOres.class);
        event.register(CapabilityParticleStackHandler.class);
    }

    public static void debugLog(String message) {
        if (MISC_CONFIG.DEBUG_LOG.get() || isBetaBuild) {
            LOGGER.info(message);
        }
    }
}
