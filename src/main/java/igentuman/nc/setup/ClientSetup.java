package igentuman.nc.setup;

import igentuman.nc.client.RuntimeFuelModelGenerator;
import igentuman.nc.client.block.BatteryBlockItemDecorator;
import igentuman.nc.client.block.BatteryBlockLoader;
import igentuman.nc.client.block.ChargingStationRenderer;
import igentuman.nc.client.block.fusion.FusionCoreRenderer;
import igentuman.nc.client.block.kugelblitz.EXPLRenderer;
import igentuman.nc.client.block.turbine.TurbineRotorRenderer;
import igentuman.nc.client.bomb.BombFlashOverlay;
import igentuman.nc.client.gui.*;
import igentuman.nc.client.gui.accelerator.*;
import igentuman.nc.client.gui.crafter.EngineersCrafterScreen;
import igentuman.nc.client.gui.crafter.EngineersEncoderScreen;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.gui.fission.FissionPortScreen;
import igentuman.nc.client.gui.fission.MSRControllerScreen;
import igentuman.nc.client.gui.fission.MSRPortScreen;
import igentuman.nc.client.gui.kugelblitz.ChamberPortScreen;
import igentuman.nc.client.gui.kugelblitz.ChamberTerminalScreen;
import igentuman.nc.client.gui.kugelblitz.EXPLScreen;
import igentuman.nc.client.gui.heat_exchanger.HeatExchangerControllerScreen;
import igentuman.nc.client.gui.heat_exchanger.HeatExchangerPortScreen;
import igentuman.nc.client.gui.turbine.TurbineControllerScreen;
import igentuman.nc.client.gui.turbine.TurbinePortScreen;
import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.particle.BombFlashParticle;
import igentuman.nc.client.particle.FireVerticalParticle;
import igentuman.nc.client.particle.FusionBeamParticle;
import igentuman.nc.client.particle.RadiationParticle;
import igentuman.nc.client.particle.ExplosionParticle;
import igentuman.nc.client.particle.ExplosionSeedParticle;
import igentuman.nc.client.particle.VanillaFlashParticle;
import igentuman.nc.client.renderer.BlackholeRenderer;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.renderer.Q36EnergyFlashRenderer;
import igentuman.nc.client.renderer.entity.PrimedFissionBombRenderer;
import igentuman.nc.client.renderer.Q36PulseProjectileRenderer;
import igentuman.nc.client.renderer.WastelandProjectileRenderer;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.compat.ponder.PonderUtil;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.event.client.*;
import igentuman.nc.item.Q36Item;
import igentuman.nc.radiation.client.ClientRadiationData;
import igentuman.nc.radiation.client.RadiationOverlay;
import igentuman.nc.radiation.client.WhiteNoiseOverlay;
import igentuman.nc.setup.registration.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.COLLISION_CHAMBER_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.DECAY_CHAMBER_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_PORT_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_PORT_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.MSR_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.MSR_PORT_CONTAINER;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BE;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_CORE_CONTAINER;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.*;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_CONTROLLER_CONTAINER;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_PORT_CONTAINER;
import static igentuman.nc.setup.registration.NCBlocks.CHARGING_STATION_BE;
import static igentuman.nc.setup.registration.NCBlocks.CHARGING_STATION_CONTAINER;
import static igentuman.nc.setup.registration.NCBlocks.MULTIBLOCK_BUILDER_CONTAINER;
import static igentuman.nc.setup.registration.NCBlocks.REDSTONE_DIMMER_CONTAINER;
import static igentuman.nc.setup.registration.NCCrafter.CRAFTING_PATTERN;
import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_CONTAINER;
import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_ENCODER_CONTAINER;
import static igentuman.nc.setup.registration.NCItems.GEIGER_COUNTER;
import static igentuman.nc.setup.registration.NCItems.Q36;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_CONTAINER;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_ITEM_CONTAINER;
import static igentuman.nc.setup.registration.Registries.FLUIDS;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.addListener(LOWEST, SoundHandler::onTilePlaySound);
            DistortShader.register();
            igentuman.nc.client.renderer.Q36FlashShader.register();
            igentuman.nc.client.renderer.AnomalyShader.register();
            BlockEntityRenderers.register(FUSION_BE.get("fusion_core").get(), FusionCoreRenderer::new);
            BlockEntityRenderers.register(EXPL_BE.get(), EXPLRenderer::new);
            BlockEntityRenderers.register(TURBINE_BE.get("turbine_rotor_shaft").get(), TurbineRotorRenderer::new);
            BlockEntityRenderers.register(KUGELBLITZ_BE.get("black_hole").get(), BlackholeRenderer::new);
            BlockEntityRenderers.register(CHARGING_STATION_BE.get(), ChargingStationRenderer::new);
            EntityRenderers.register(Entities.FERAL_GHOUL.get(), FeralGhoulRenderer::new);
            EntityRenderers.register(Entities.BLOCK_PROJECTILE.get(), WastelandProjectileRenderer::new);
            EntityRenderers.register(Entities.Q36_PULSE_PROJECTILE.get(), Q36PulseProjectileRenderer::new);
            EntityRenderers.register(Entities.Q36_ENERGY_FLASH.get(), Q36EnergyFlashRenderer::new);
            EntityRenderers.register(Entities.PRIMED_FISSION_BOMB.get(), PrimedFissionBombRenderer::new);
            MenuScreens.register(STORAGE_CONTAINER.get(), StorageContainerScreen::new);
            MenuScreens.register(STORAGE_ITEM_CONTAINER.get(), StorageContainerItemScreen::new);
            MenuScreens.register(FUSION_CORE_CONTAINER.get(), FusionCoreScreen::new);
            MenuScreens.register(EXPL_CONTAINER.get(), EXPLScreen::new);
            MenuScreens.register(TURBINE_CONTROLLER_CONTAINER.get(), TurbineControllerScreen::new);
            MenuScreens.register(TURBINE_PORT_CONTAINER.get(), TurbinePortScreen::new);
            MenuScreens.register(HX_CONTROLLER_CONTAINER.get(), HeatExchangerControllerScreen::new);
            MenuScreens.register(HX_PORT_CONTAINER.get(), HeatExchangerPortScreen::new);
            MenuScreens.register(FISSION_CONTROLLER_CONTAINER.get(), FissionControllerScreen::new);
            MenuScreens.register(FISSION_PORT_CONTAINER.get(), FissionPortScreen::new);
            MenuScreens.register(MSR_CONTROLLER_CONTAINER.get(), MSRControllerScreen::new);
            MenuScreens.register(MSR_PORT_CONTAINER.get(), MSRPortScreen::new);
            MenuScreens.register(CHAMBER_PORT_CONTAINER.get(), ChamberPortScreen::new);
            MenuScreens.register(CHAMBER_TERMINAL_CONTAINER.get(), ChamberTerminalScreen::new);
            MenuScreens.register(REDSTONE_DIMMER_CONTAINER.get(), RedstoneDimmerScreen::new);
            MenuScreens.register(CHARGING_STATION_CONTAINER.get(), ChargingStationScreen::new);
            MenuScreens.register(ENGINEERS_CRAFTING_TABLE_CONTAINER.get(), EngineersCrafterScreen::new);
            MenuScreens.register(ENGINEERS_ENCODER_CONTAINER.get(), EngineersEncoderScreen::new);
            MenuScreens.register(MULTIBLOCK_BUILDER_CONTAINER.get(), MultiblockBuilderScreen::new);
            MenuScreens.register(LINEAR_ACCELERATOR_CONTROLLER_CONTAINER.get(), LinearAcceleratorControllerScreen::new);
            MenuScreens.register(THOROIDAL_ACCELERATOR_CONTROLLER_CONTAINER.get(), RingAcceleratorControllerScreen::new);
            MenuScreens.register(ACCELERATOR_PORT_CONTAINER.get(), AcceleratorPortScreen::new);
            MenuScreens.register(ACCELERATOR_ION_SOURCE_PORT_CONTAINER.get(), AcceleratorIonSourcePortScreen::new);
            MenuScreens.register(TARGET_CHAMBER_CONTROLLER_CONTAINER.get(), TargetChamberControllerScreen::new);
            MenuScreens.register(TARGET_CHAMBER_PORT_CONTAINER.get(), TargetChamberPortScreen::new);
            MenuScreens.register(COLLISION_CHAMBER_CONTROLLER_CONTAINER.get(), CollisionChamberControllerScreen::new);
            MenuScreens.register(DECAY_CHAMBER_CONTROLLER_CONTAINER.get(), DecayChamberControllerScreen::new);
            MenuScreens.register(BEAM_DIVERTER_CONTROLLER_CONTAINER.get(), BeamDiverterControllerScreen::new);

            for(String name: NCProcessors.PROCESSORS_CONTAINERS.keySet()) {
                MenuScreens.register(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), Processors.all().get(name).getScreenConstructor());
            }
            
            // Generate runtime models for custom fuels
            RuntimeFuelModelGenerator.generateResources();
        });

        for(RegistryObject<Fluid> f : FLUIDS.getEntries())
            if(NCFluids.NC_GASES.containsKey(f.getId().getPath()))
                ItemBlockRenderTypes.setRenderLayer(f.get(), RenderType.translucent());

        event.enqueueWork(() -> {
            setPropertyOverride(GEIGER_COUNTER.get(), rl("radiation"), (stack, world, entity, seed) -> {
                if (entity instanceof Player) {
                    if(!((Player) entity).getInventory().contains(new ItemStack(GEIGER_COUNTER.get()))) return 0;
                    ClientRadiationData.setCurrentChunk(entity.chunkPosition().x, entity.chunkPosition().z, entity.level());
                    return ClientRadiationData.radiationStage();
                }
                return 0;
            });
            setPropertyOverride(Q36.get(), rl("firing"), (stack, world, entity, seed) -> {
                if (world == null) return 0.0F;
                return Q36Item.isFxActive(stack, world) ? 1.0F : 0.0F;
            });
            setPropertyOverride(CRAFTING_PATTERN.get(), rl("encoded"), (stack, world, entity, seed) ->
                    igentuman.nc.handler.crafter.CraftingPattern.isEncoded(stack) ? 1.0F : 0.0F);
        });
        PonderUtil.initPlugin();
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
        event.registerAboveAll("bomb_flash", BombFlashOverlay.BOMB_FLASH);
        event.registerAboveAll("multiblock_debug", MultiblockDebugOverlay.MULTIBLOCK_DEBUG);
        event.registerAboveAll("beam_particle_info", BeamParticleOverlay.BEAM_PARTICLE_INFO);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NcParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.FUSION_BEAM.get(), FusionBeamParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.FIRE_VERTICAL.get(), FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.FIRE_VERTICAL1.get(), FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.SMOKE.get(), FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.FLASH.get(), BombFlashParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.EXPLOSION.get(), ExplosionParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.EXPLOSION_SEED.get(), ExplosionSeedParticle.Factory::new);
        event.registerSpriteSet(NcParticleTypes.VANILLA_FLASH.get(), VanillaFlashParticle.Factory::new);
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
