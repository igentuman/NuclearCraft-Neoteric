package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.recipes.type.RadShieldingRecipe;
import igentuman.nc.recipes.type.ResetNbtRecipe;
import igentuman.nc.world.dimension.Dimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(RadiationEvents::onPlayerCloned);
        bus.addGenericListener(Entity.class, RadiationEvents::attachPlayerRadiation);
        bus.addGenericListener(Level.class, RadiationEvents::attachWorldRadiation);
        bus.register(NuclearCraft.worldTickHandler);
        bus.register(new RadiationEvents());
    }

    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Dimensions.register();
            //CapabilityRegistration.register(event);

        });
        NuclearCraft.packetHandler().initialize();
    }
}
