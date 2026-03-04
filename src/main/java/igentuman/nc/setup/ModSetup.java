package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.handler.event.server.PlayerEvents;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.setup.registration.GameEvents;
import igentuman.nc.world.structure.ScientistHouseStructure;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static igentuman.nc.NuclearCraft.MODID;

public class ModSetup {

    public static void setup() {
        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(RadiationEvents::onPlayerCloned);
        Particles.init();
        // AttachCapabilitiesEvent handlers removed -- PlayerRadiation uses Data Attachments,
        // WorldRadiation uses RadiationManager (SavedData), WorldVeins uses WorldVeinsManager (SavedData)
        // WorldEvents, PlayerEvents, ScientistHouseStructure registered via @EventBusSubscriber (static methods)
        bus.register(new RadiationEvents()); // RadiationEvents has non-static handlers, needs instance registration
    }

    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Condition serializers now registered via DeferredRegister in Registries.init()
            GameEvents.commonSetup();
        });
    }


}
