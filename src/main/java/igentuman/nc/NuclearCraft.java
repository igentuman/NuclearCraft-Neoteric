package igentuman.nc;

import igentuman.nc.handler.config.*;
import igentuman.nc.handler.event.server.WorldEvents;
import igentuman.nc.handler.command.CommandNcPlayerRadiation;
import igentuman.nc.handler.command.CommandNcVeinCheck;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.network.PacketHandler;
import igentuman.nc.setup.ClientSetup;
import igentuman.nc.setup.ModSetup;
import igentuman.nc.setup.Registration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;

@Mod(NuclearCraft.MODID)
public class NuclearCraft {

    public static final Logger LOGGER = LogManager.getLogger();
    public boolean isNcBeStopped = false;
    public static final WorldEvents worldTickHandler = new WorldEvents();
    public static final String MODID = "nuclearcraft";
    public static NuclearCraft instance;
    private final PacketHandler packetHandler;

    /**
     * Sorry but has to load config before registration stage
     */
    @SuppressWarnings("unchecked")
    private void forceLoadConfig()
    {
        try {
            Method openConfig = ConfigTracker.INSTANCE.getClass()
                    .getDeclaredMethod("openConfig", ModConfig.class, Path.class);
            openConfig.setAccessible(true);
            Field configSets = ConfigTracker.INSTANCE.getClass().getDeclaredField("configSets");
            configSets.setAccessible(true);
            EnumMap<ModConfig.Type, Set<ModConfig>> configSetsValue = (EnumMap<ModConfig.Type, Set<ModConfig>>) configSets.get(ConfigTracker.INSTANCE);
            ModConfig ncConfig = null;
            for(ModConfig config : configSetsValue.get(ModConfig.Type.COMMON)) {
                if(config.getModId().equals(MODID)) {
                    ncConfig = config;
                    break;
                }
            }
            openConfig.invoke(ConfigTracker.INSTANCE, ncConfig, FMLPaths.CONFIGDIR.get());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error("Unable to force load NC config. And this is why:");
            LOGGER.error(e);
        }
    }

    public static void registerConfigs()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MaterialsConfig.spec, "NuclearCraft/materials.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.spec, "NuclearCraft/common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ProcessorsConfig.spec, "NuclearCraft/processors.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FissionConfig.spec, "NuclearCraft/fission.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FusionConfig.spec, "NuclearCraft/fusion.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TurbineConfig.spec, "NuclearCraft/turbine.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RadiationConfig.spec, "NuclearCraft/radiation.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WorldConfig.spec, "NuclearCraft/world.toml");
    }

    public NuclearCraft() {
        instance = this;
        registerConfigs();
        packetHandler = new PacketHandler();
        forceLoadConfig();
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        //MinecraftForge.EVENT_BUS.addListener(this::gameShuttingDownEvent);
        ModSetup.setup();
        Registration.init();
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        modbus.addListener(ModSetup::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(ClientSetup::init));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(this::registerClientEventHandlers));
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
            CommonConfig.setLoaded();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(CommandNcPlayerRadiation.register());
        event.getDispatcher().register(CommandNcVeinCheck.register());
    }
    private void registerClientEventHandlers(FMLClientSetupEvent event) {
        ClientSetup.registerEventHandlers(event);
    }

    public static ResourceLocation rl(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    private void serverStopped(FMLServerStoppedEvent event) {
        NuclearCraft.instance.isNcBeStopped = true;
        //stop capability tracking
        RadiationEvents.stopTracking();
        for(ServerWorld level: event.getServer().getAllLevels()) {
            RadiationManager.clear(level);
        }
    }
/*    private void gameShuttingDownEvent(GameShuttingDownEvent event) {
        NuclearCraft.instance.isNcBeStopped = true;
    }*/
    public MinecraftServer server;
    private void serverStarted(FMLServerStartedEvent event) {
        NuclearCraft.instance.isNcBeStopped = false;
        RadiationEvents.startTracking();
        server = event.getServer();
    }



/*    @SubscribeEvent
    public void registerCaps(Capabilit event) {
        event.register(WorldRadiation.class);
        event.register(PlayerRadiation.class);
    }*/
}
