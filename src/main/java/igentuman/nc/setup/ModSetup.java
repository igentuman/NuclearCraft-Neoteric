package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.handler.config.AcceleratorConfig;
import igentuman.nc.handler.event.server.PlayerEvents;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.setup.registration.GameEvents;
import igentuman.nc.util.GTCEUCompatibilityCondition;
import igentuman.nc.util.WastelandEnabledCondition;
import igentuman.nc.util.insitu_leaching.WorldVeinsProvider;
import igentuman.nc.world.structure.ScientistHouseStructure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Entities.registerSpawnPlacements;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(RadiationEvents::onPlayerCloned);
        Particles.init();
        bus.addGenericListener(Entity.class, RadiationEvents::attachPlayerRadiation);
        bus.addGenericListener(Level.class, RadiationEvents::attachWorldRadiation);
        bus.addGenericListener(Level.class, WorldVeinsProvider::attachVeinCapability);
        bus.register(NuclearCraft.worldTickHandler);
        bus.register(new PlayerEvents());
        bus.register(new RadiationEvents());
        bus.register(new ScientistHouseStructure());
    }

    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CraftingHelper.register(new WastelandEnabledCondition.Serializer());
            CraftingHelper.register(new GTCEUCompatibilityCondition.Serializer());
            registerSpawnPlacements();
            GameEvents.commonSetup();
        });
        NuclearCraft.packetHandler().initialize();
    }
}
