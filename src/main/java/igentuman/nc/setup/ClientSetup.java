package igentuman.nc.setup;

import igentuman.nc.client.particle.RadiationParticle;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(FissionReactor.FISSION_CONTROLLER_CONTAINER.get(), FissionControllerScreen::new);

            for(String name: NCProcessors.PROCESSORS_CONTAINERS.keySet()) {
                MenuScreens.register(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), Processors.all().get(name).getScreenConstructor());
            }

        });
        for(RegistryObject<Fluid> f : NCFluids.FLUIDS.getEntries())
            if(NCFluids.NC_GASES.containsKey(f.getId().getPath()))
                ItemBlockRenderTypes.setRenderLayer(f.get(), RenderType.translucent());
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.register(NcParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
    }

}
