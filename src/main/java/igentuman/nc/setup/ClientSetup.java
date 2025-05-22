package igentuman.nc.setup;

import igentuman.nc.client.block.BatteryBlockItemDecorator;
import igentuman.nc.client.block.BatteryBlockLoader;
import igentuman.nc.client.block.fusion.FusionCoreRenderer;
import igentuman.nc.client.block.kugelblitz.EXPLRenderer;
import igentuman.nc.client.block.turbine.TurbineRotorRenderer;
import igentuman.nc.client.gui.FusionCoreScreen;
import igentuman.nc.client.gui.RedstoneDimmerScreen;
import igentuman.nc.client.gui.StorageContainerScreen;
import igentuman.nc.client.gui.accelerator.AcceleratorPortScreen;
import igentuman.nc.client.gui.accelerator.LinearAcceleratorControllerScreen;
import igentuman.nc.client.gui.fission.FissionPortScreen;
import igentuman.nc.client.gui.kugelblitz.ChamberPortScreen;
import igentuman.nc.client.gui.kugelblitz.ChamberTerminalScreen;
import igentuman.nc.client.gui.kugelblitz.EXPLScreen;
import igentuman.nc.client.gui.turbine.TurbineControllerScreen;
import igentuman.nc.client.gui.turbine.TurbinePortScreen;
import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.particle.FusionBeamParticle;
import igentuman.nc.client.particle.RadiationParticle;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.handler.event.client.*;
import igentuman.nc.radiation.client.ClientRadiationData;
import igentuman.nc.radiation.client.RadiationOverlay;
import igentuman.nc.radiation.client.WhiteNoiseOverlay;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.Entities;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import igentuman.nc.client.renderer.BlackholeRenderer;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_PORT_CONTAINER;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.LINEAR_ACCELERATOR_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_PORT_CONTAINER;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BE;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_CORE_CONTAINER;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.*;
import static igentuman.nc.setup.registration.NCBlocks.REDSTONE_DIMMER_CONTAINER;
import static igentuman.nc.setup.registration.NCItems.GEIGER_COUNTER;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_CONTAINER;
import static igentuman.nc.setup.registration.Registries.FLUIDS;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.addListener(LOWEST, SoundHandler::onTilePlaySound);
            DistortShader.register();
            BlockEntityRenderers.register(FUSION_BE.get("fusion_core").get(), FusionCoreRenderer::new);
            BlockEntityRenderers.register(EXPL_BE.get(), EXPLRenderer::new);
            BlockEntityRenderers.register(TURBINE_BE.get("turbine_rotor_shaft").get(), TurbineRotorRenderer::new);
            BlockEntityRenderers.register(KUGELBLITZ_BE.get("black_hole").get(), BlackholeRenderer::new);
            
            // Register the Feral Ghoul renderer
            EntityRenderers.register(Entities.FERAL_GHOUL.get(), FeralGhoulRenderer::new);
            
            MenuScreens.register(STORAGE_CONTAINER.get(), StorageContainerScreen::new);
            MenuScreens.register(FUSION_CORE_CONTAINER.get(), FusionCoreScreen::new);
            MenuScreens.register(EXPL_CONTAINER.get(), EXPLScreen::new);
            MenuScreens.register(TURBINE_CONTROLLER_CONTAINER.get(), TurbineControllerScreen::new);
            MenuScreens.register(TURBINE_PORT_CONTAINER.get(), TurbinePortScreen::new);
            MenuScreens.register(FISSION_CONTROLLER_CONTAINER.get(), FissionControllerScreen::new);
            MenuScreens.register(FISSION_PORT_CONTAINER.get(), FissionPortScreen::new);
            MenuScreens.register(CHAMBER_PORT_CONTAINER.get(), ChamberPortScreen::new);
            MenuScreens.register(CHAMBER_TERMINAL_CONTAINER.get(), ChamberTerminalScreen::new);
            MenuScreens.register(REDSTONE_DIMMER_CONTAINER.get(), RedstoneDimmerScreen::new);
            MenuScreens.register(LINEAR_ACCELERATOR_CONTROLLER_CONTAINER.get(), LinearAcceleratorControllerScreen::new);
            MenuScreens.register(ACCELERATOR_PORT_CONTAINER.get(), AcceleratorPortScreen::new);

            for(String name: NCProcessors.PROCESSORS_CONTAINERS.keySet()) {
                MenuScreens.register(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), Processors.all().get(name).getScreenConstructor());
            }
        });

        for(RegistryObject<Fluid> f : FLUIDS.getEntries())
            if(NCFluids.NC_GASES.containsKey(f.getId().getPath()))
                ItemBlockRenderTypes.setRenderLayer(f.get(), RenderType.translucent());

        event.enqueueWork(() -> {
            setPropertyOverride(GEIGER_COUNTER.get(), rl("radiation"), (stack, world, entity, seed) -> {
                if (entity instanceof Player) {
                    if(!((Player) entity).getInventory().contains(new ItemStack(GEIGER_COUNTER.get()))) return 0;
                    ClientRadiationData.setCurrentChunk(entity.chunkPosition().x, entity.chunkPosition().z);
                    return (int)((float)ClientRadiationData.getCurrentWorldRadiation()/400000);
                }
                return 0;
            });
        });
    }


    @SubscribeEvent
    public static void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        event.register(BatteryBlockLoader.BATTERY_LOADER.getPath(), new BatteryBlockLoader());
    }


    public static void setPropertyOverride(ItemLike itemProvider, ResourceLocation override, ItemPropertyFunction propertyGetter) {
        ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("radiation_bar", RadiationOverlay.RADIATION_BAR);
        event.registerAboveAll("white_noise", WhiteNoiseOverlay.WHITE_NOISE);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NcParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.FUSION_BEAM.get(), FusionBeamParticle.Factory::new);
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

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        for(String name: BatteryBlocks.all().keySet()) {
            event.register(NCEnergyBlocks.BLOCK_ITEMS.get(name).get(), BatteryBlockItemDecorator.INSTANCE);
        }
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelFeralGhoul.LAYER_LOCATION, ModelFeralGhoul::createBodyLayer);
    }
}
