package igentuman.nc;

import igentuman.nc.handler.command.CommandNcPlayerRadiation;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.handler.event.client.ColorHandler;
import igentuman.nc.handler.event.client.InputEvents;
import igentuman.nc.setup.ClientSetup;
import igentuman.nc.setup.ModSetup;
import igentuman.nc.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NuclearCraft.MODID)
public class NuclearCraft {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "nuclearcraft";

    public NuclearCraft() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.spec);
        ModSetup.setup();
        Registration.init();
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        modbus.addListener(ModSetup::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(ClientSetup::init));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(this::registerClientEventHandlers));
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
            CommonConfig.setLoaded();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(CommandNcPlayerRadiation.register());
    }
    private void registerClientEventHandlers(FMLClientSetupEvent event) {
        InputEvents.register(event);
        ColorHandler.register(event);
    }

    public static ResourceLocation rl(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
