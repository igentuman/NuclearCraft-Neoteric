package igentuman.nc.setup;

import igentuman.nc.client.RuntimeFuelModelGenerator;
import igentuman.nc.client.block.BatteryBlockItemDecorator;
import igentuman.nc.client.block.BatteryBlockLoader;
import igentuman.nc.client.block.fusion.FusionCoreRenderer;
import igentuman.nc.client.block.kugelblitz.EXPLRenderer;
import igentuman.nc.client.block.turbine.TurbineRotorRenderer;
import igentuman.nc.client.gui.*;
import igentuman.nc.client.gui.accelerator.*;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.gui.fission.FissionPortScreen;
import igentuman.nc.client.gui.fission.MSRControllerScreen;
import igentuman.nc.client.gui.kugelblitz.ChamberPortScreen;
import igentuman.nc.client.gui.kugelblitz.ChamberTerminalScreen;
import igentuman.nc.client.gui.kugelblitz.EXPLScreen;
import igentuman.nc.client.gui.turbine.TurbineControllerScreen;
import igentuman.nc.client.gui.turbine.TurbinePortScreen;
import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.particle.FusionBeamParticle;
import igentuman.nc.client.particle.RadiationParticle;
import igentuman.nc.client.renderer.BlackholeRenderer;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.renderer.WastelandProjectileRenderer;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.event.client.*;
import igentuman.nc.radiation.client.ClientRadiationData;
import igentuman.nc.radiation.client.RadiationOverlay;
import igentuman.nc.radiation.client.WhiteNoiseOverlay;
import igentuman.nc.setup.registration.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_PORT_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_PORT_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.MSR_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BE;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_CORE_CONTAINER;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.*;
import static igentuman.nc.setup.registration.NCBlocks.MULTIBLOCK_BUILDER_CONTAINER;
import static igentuman.nc.setup.registration.NCBlocks.REDSTONE_DIMMER_CONTAINER;
import static igentuman.nc.setup.registration.NCItems.GEIGER_COUNTER;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_CONTAINER;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_ITEM_CONTAINER;
import static net.neoforged.bus.api.EventPriority.LOWEST;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.addListener(LOWEST, SoundHandler::onTilePlaySound);
            DistortShader.register();
            BlockEntityRenderers.register(FUSION_BE.get("fusion_core").get(), FusionCoreRenderer::new);
            BlockEntityRenderers.register(EXPL_BE.get(), EXPLRenderer::new);
            BlockEntityRenderers.register(TURBINE_BE.get("turbine_rotor_shaft").get(), TurbineRotorRenderer::new);
            BlockEntityRenderers.register(KUGELBLITZ_BE.get("black_hole").get(), BlackholeRenderer::new);
            EntityRenderers.register(Entities.FERAL_GHOUL.get(), FeralGhoulRenderer::new);
            EntityRenderers.register(Entities.WASTELAND_PROJECTILE.get(), WastelandProjectileRenderer::new);

            // Generate runtime models for custom fuels
            RuntimeFuelModelGenerator.generateResources();
        });

       /* event.enqueueWork(() -> {
            setPropertyOverride(GEIGER_COUNTER.get(), rl("radiation"), (stack, world, entity, seed) -> {
                if (entity instanceof Player) {
                    if(!((Player) entity).getInventory().contains(new ItemStack(GEIGER_COUNTER.get()))) return 0;
                    ClientRadiationData.setCurrentChunk(entity.chunkPosition().x, entity.chunkPosition().z, entity.level());
                    return ClientRadiationData.radiationStage();
                }
                return 0;
            });
        });*/
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(STORAGE_CONTAINER.get(), StorageContainerScreen::new);
        event.register(STORAGE_ITEM_CONTAINER.get(), StorageContainerItemScreen::new);
        event.register(FUSION_CORE_CONTAINER.get(), FusionCoreScreen::new);
        event.register(EXPL_CONTAINER.get(), EXPLScreen::new);
        event.register(TURBINE_CONTROLLER_CONTAINER.get(), TurbineControllerScreen::new);
        event.register(TURBINE_PORT_CONTAINER.get(), TurbinePortScreen::new);
        event.register(FISSION_CONTROLLER_CONTAINER.get(), FissionControllerScreen::new);
        event.register(FISSION_PORT_CONTAINER.get(), FissionPortScreen::new);
        event.register(MSR_CONTROLLER_CONTAINER.get(), MSRControllerScreen::new);
        event.register(CHAMBER_PORT_CONTAINER.get(), ChamberPortScreen::new);
        event.register(CHAMBER_TERMINAL_CONTAINER.get(), ChamberTerminalScreen::new);
        event.register(REDSTONE_DIMMER_CONTAINER.get(), RedstoneDimmerScreen::new);
        event.register(MULTIBLOCK_BUILDER_CONTAINER.get(), MultiblockBuilderScreen::new);
        event.register(LINEAR_ACCELERATOR_CONTROLLER_CONTAINER.get(), LinearAcceleratorControllerScreen::new);
        event.register(THOROIDAL_ACCELERATOR_CONTROLLER_CONTAINER.get(), RingAcceleratorControllerScreen::new);
        event.register(ACCELERATOR_PORT_CONTAINER.get(), AcceleratorPortScreen::new);
        event.register(ACCELERATOR_ION_SOURCE_PORT_CONTAINER.get(), AcceleratorIonSourcePortScreen::new);
        event.register(TARGET_CHAMBER_CONTROLLER_CONTAINER.get(), TargetChamberControllerScreen::new);
        event.register(TARGET_CHAMBER_PORT_CONTAINER.get(), TargetChamberPortScreen::new);

        for(String name: NCProcessors.PROCESSORS_CONTAINERS.keySet()) {
            event.register(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), Processors.all().get(name).getScreenConstructor());
        }
    }

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        event.register(BatteryBlockLoader.BATTERY_LOADER, new BatteryBlockLoader());
    }


    public static void setPropertyOverride(ItemLike itemProvider, ResourceLocation override, ItemPropertyFunction propertyGetter) {
        ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(rl("radiation_bar"), RadiationOverlay.RADIATION_BAR);
        event.registerAboveAll(rl("white_noise"), WhiteNoiseOverlay.WHITE_NOISE);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NcParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.FUSION_BEAM.get(), FusionBeamParticle.Factory::new);
    }

    public static void setup() {
        IEventBus bus = NeoForge.EVENT_BUS;
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
