package igentuman.nc.setup;

import igentuman.nc.client.block.BatteryBlockLoader;
import igentuman.nc.client.block.fusion.FusionCoreRenderer;
import igentuman.nc.client.gui.FusionCoreScreen;
import igentuman.nc.client.gui.StorageContainerScreen;
import igentuman.nc.client.gui.fission.FissionPortScreen;
import igentuman.nc.client.gui.processor.LeacherScreen;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.client.gui.turbine.TurbineControllerScreen;
import igentuman.nc.client.gui.turbine.TurbinePortScreen;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.particle.FusionBeamParticle;
import igentuman.nc.client.particle.RadiationParticle;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.event.client.*;
import igentuman.nc.radiation.client.ClientRadiationData;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.NuclearCraft.*;
import static igentuman.nc.multiblock.fission.FissionReactor.*;
import static igentuman.nc.multiblock.fusion.FusionReactor.*;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_PORT_CONTAINER;
import static igentuman.nc.setup.registration.NCItems.GEIGER_COUNTER;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS_CONTAINERS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_CONTAINER;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
          //  MinecraftForge.EVENT_BUS.addListener(LOWEST, SoundHandler::onTilePlaySound);
            ScreenManager.register(STORAGE_CONTAINER.get(), StorageContainerScreen::new);
            ScreenManager.register(FUSION_CORE_CONTAINER.get(), FusionCoreScreen::new);
            ScreenManager.register(TURBINE_CONTROLLER_CONTAINER.get(), TurbineControllerScreen::new);
            ScreenManager.register(TURBINE_PORT_CONTAINER.get(), TurbinePortScreen::new);
            ScreenManager.register(FISSION_CONTROLLER_CONTAINER.get(), FissionControllerScreen::new);
            ScreenManager.register(FISSION_PORT_CONTAINER.get(), FissionPortScreen::new);

            for(String name: PROCESSORS_CONTAINERS.keySet()) {
                if(name.contains("leacher")) {
                    ScreenManager.register(PROCESSORS_CONTAINERS.get(name).get(), LeacherScreen::new);
                    continue;
                }
                  ScreenManager.register(PROCESSORS_CONTAINERS.get(name).get(), NCProcessorScreen::new);

               //` ScreenManager.register(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), Processors.all().get(name).getScreenConstructor());
            }
            ClientRegistry.bindTileEntityRenderer(FUSION_BE.get("fusion_core").get(), FusionCoreRenderer::new);

        });

        for(RegistryObject<Fluid> f : NCFluids.FLUIDS.getEntries()) {
            if (NCFluids.NC_GASES.containsKey(f.getId().getPath()))
                RenderTypeLookup.setRenderLayer(f.get(), RenderType.translucent());
        }
        RenderTypeLookup.setRenderLayer(FISSION_BLOCKS.get("fission_reactor_glass").get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(FUSION_BLOCKS.get("fusion_reactor_casing_glass").get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(FUSION_CORE_PROXY.get(), RenderType.cutout());
        registerGuiOverlays();
        event.enqueueWork(() -> {
            setPropertyOverride(GEIGER_COUNTER.get(), rl("radiation"), (stack, world, entity) -> {
                if (entity instanceof PlayerEntity) {
                    if(!((PlayerEntity) entity).inventory.contains(new ItemStack(GEIGER_COUNTER.get()))) return 0;
                    ClientRadiationData.setCurrentChunk(entity.xChunk, entity.zChunk);
                    return (int)((float) ClientRadiationData.getCurrentWorldRadiation()/400000);
                }
                return 0;
            });
        });
    }

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(BatteryBlockLoader.BATTERY_LOADER, new BatteryBlockLoader());
    }


    public static void setPropertyOverride(IItemProvider itemProvider, ResourceLocation override, IItemPropertyGetter propertyGetter) {
        ItemModelsProperties.register(itemProvider.asItem(), override, propertyGetter);
    }


    public static void registerGuiOverlays() {
        //(HOTBAR_ELEMENT, "radiation_bar", RadiationOverlay.RADIATION_BAR);
      //  OverlayRegistry.registerOverlayAbove(HOTBAR_ELEMENT, "white_noise", WhiteNoiseOverlay.WHITE_NOISE);
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(NcParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
        Minecraft.getInstance().particleEngine.register(NcParticleTypes.FUSION_BEAM.get(), FusionBeamParticle.Factory::new);
    }

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
    }

    public static void registerEventHandlers(FMLClientSetupEvent event) {
        InputEvents.register(event);
        ColorHandler.register(event);
        ServerLoad.register(event);
        RecipesUpdated.register(event);
        TagsUpdated.register(event);
        TooltipHandler.register(event);
        TickHandler.register(event);
        BlockOverlayHandler.register(event);
    }
}
