package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.render.bomb.PrimedFissionBombRenderer;
import igentuman.nc.client.render.q36.Q36EnergyFlashRenderer;
import igentuman.nc.client.render.q36.Q36PulseProjectileRenderer;
import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.renderer.WastelandBossRenderer;
import igentuman.nc.client.renderer.WastelandProjectileRenderer;
import igentuman.nc.client.renderer.anomaly.AnomalyRenderer;
import igentuman.nc.client.renderer.anomaly.GravitationalAnomalyRenderer;
import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.entity.EntityWastelandBoss;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import igentuman.nc.setup.entries.*;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.ScenePostEffects;
import igentuman.nc.compat.ponder.PonderUtil;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.container.PipeConnectorContainer;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.entity.PrimedFissionBombEntity;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.block_entity.kugelblitz.EXPLBE;
import igentuman.nc.client.particle.FusionBeamParticle;
import igentuman.nc.client.render.fusion.FusionCoreRenderer;
import igentuman.nc.client.render.kugelblitz.BlackholeRenderer;
import igentuman.nc.client.render.kugelblitz.EXPLRenderer;
import igentuman.nc.container.EXPLContainer;
import igentuman.nc.screen.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import igentuman.nc.client.item.ResoniteCrystalTint;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import static igentuman.nc.setup.entries.Parts.Q36_ENERGY_FLASH;
import static igentuman.nc.setup.entries.Parts.Q36_PULSE_PROJECTILE;

/** Client-only setup: registers screens, renderers, particle providers, and fluid render extensions. */
@Mod(value = NuclearCraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class Client {
    public Client(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        if (ModList.get().isLoaded("ponder")) {
            igentuman.nc.compat.ponder.PonderConfigFix.registerEarly();
            PonderUtil.registerRendererEarly();
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register the game-bus render handlers (level-stage events).
            DelayedRenderHandler.register();
            ScenePostEffects.register();
            if (ModList.get().isLoaded("ponder")) {
                PonderUtil.initPlugin();
            }
        });
    }

    @SubscribeEvent
    static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(NuclearCraft.rl("scene_post_effects"),
                (ResourceManagerReloadListener) manager -> ScenePostEffects.close());
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        ModEntries.ENTRIES.values().stream()
                .filter(ModEntry::hasMenu)
                .forEach(entry -> {
                    var block = entry.block() != null ? entry.block().get() : null;
                    if (block instanceof MultiblockControllerBlock) {
                        MenuType<MultiblockControllerContainer> menuType =
                                (MenuType<MultiblockControllerContainer>) (MenuType<?>) entry.menu().get();
                        if (entry.name().equals("fission_reactor_controller")) {
                            event.register(menuType, FissionReactorScreen::new);
                        } else if (entry.name().equals("fusion_reactor_core")) {
                            event.register(menuType, FusionReactorScreen::new);
                        } else if (entry.name().equals("chamber_terminal")) {
                            event.register(menuType, igentuman.nc.screen.ChamberTerminalScreen::new);
                        } else if (entry.name().equals("turbine_controller")) {
                            event.register(menuType, TurbineControllerScreen::new);
                        } else if (entry.name().equals("heat_exchanger_controller")) {
                            event.register(menuType, HeatExchangerControllerScreen::new);
                        } else if (entry.name().equals("msr_controller")) {
                            event.register(menuType, MsrControllerScreen::new);
                        } else if (entry.name().equals("beam_diverter_controller")) {
                            event.register(menuType, igentuman.nc.screen.BeamDiverterScreen::new);
                        } else if (entry.name().equals("target_chamber_controller")) {
                            event.register(menuType, igentuman.nc.screen.TargetChamberScreen::new);
                        } else if (entry.name().equals("decay_chamber_controller")) {
                            event.register(menuType, igentuman.nc.screen.DecayChamberScreen::new);
                        } else if (entry.name().equals("collision_chamber_controller")) {
                            event.register(menuType, igentuman.nc.screen.CollisionChamberScreen::new);
                        } else if (entry.name().equals("ring_accelerator_controller")) {
                            event.register(menuType, igentuman.nc.screen.RingAcceleratorScreen::new);
                        } else if (entry.name().equals("linear_accelerator_controller")) {
                            event.register(menuType, igentuman.nc.screen.LinearAcceleratorScreen::new);
                        } else {
                            event.register(menuType, MultiblockControllerScreen::new);
                        }
                    } else if (block instanceof MultiblockPartBlock) {
                        event.register(
                                (MenuType<MultiblockPortContainer>) (MenuType<?>) entry.menu().get(),
                                MultiblockPortScreen::new
                        );
                    } else if (entry.name().equals("expl")) {
                        event.register(
                                (MenuType<EXPLContainer>) (MenuType<?>) entry.menu().get(),
                                EXPLScreen::new
                        );
                    } else if (entry.name().equals("pipe_connector")) {
                        event.register(
                                (MenuType<PipeConnectorContainer>) (MenuType<?>) entry.menu().get(),
                                PipeConnectorScreen::new
                        );
                    } else if (entry.name().equals("fission_reactor_designer")) {
                        event.register(
                                (MenuType<igentuman.nc.container.FissionDesignerContainer>) (MenuType<?>) entry.menu().get(),
                                FissionDesignerScreen::new
                        );
                    } else if (entry.name().equals("multiblock_builder")) {
                        event.register(
                                (MenuType<igentuman.nc.container.MultiblockBuilderContainer>) (MenuType<?>) entry.menu().get(),
                                MultiblockBuilderScreen::new
                        );
                    } else if (entry.name().equals("charging_station")) {
                        event.register(
                                (MenuType<UniversalProcessorContainer>) (MenuType<?>) entry.menu().get(),
                                igentuman.nc.screen.ChargingStationScreen::new
                        );
                    } else if (entry.name().equals("creative_particle_source")) {
                        event.register(
                                (MenuType<UniversalProcessorContainer>) (MenuType<?>) entry.menu().get(),
                                CreativeParticleSourceScreen::new
                        );
                    } else if (entry.name().equals("leacher")) {
                        event.register(
                                (MenuType<UniversalProcessorContainer>) (MenuType<?>) entry.menu().get(),
                                LeacherScreen::new
                        );
                    } else {
                        event.register(
                                (MenuType<UniversalProcessorContainer>) (MenuType<?>) entry.menu().get(),
                                UniversalProcessorScreen::new
                        );
                    }
                });
        event.register(Storage.STORAGE_MENU.get(),
                igentuman.nc.screen.StorageContainerScreen::new);
        event.register(Storage.STORAGE_ITEM_MENU.get(),
                StorageContainerItemScreen::new);
        event.register(igentuman.nc.setup.entries.Crafter.ENGINEERS_CRAFTING_TABLE_MENU.get(),
                EngineersCrafterScreen::new);
        event.register(igentuman.nc.setup.entries.Crafter.ENGINEERS_ENCODER_MENU.get(),
                EngineersEncoderScreen::new);
    }

    @SubscribeEvent
    static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        igentuman.nc.client.gui.fission.designer.ClientFissionDesignCache.onClientTick(event);
    }

    @SubscribeEvent
    static void onClientLogout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        igentuman.nc.client.gui.fission.designer.ClientFissionDesignCache.onLogout(event);
    }

    @SubscribeEvent
    static void registerSideConfigModels(ModelEvent.RegisterStandalone event) {
        igentuman.nc.client.model.SideConfigFaces.registerModels(event);
    }

    @SubscribeEvent
    static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(igentuman.nc.client.model.BatteryUnbakedModel.ID, igentuman.nc.client.model.BatteryUnbakedModel.CODEC);
    }

    @SubscribeEvent
    static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(igentuman.nc.client.item.CraftingPatternItemModel.ID, igentuman.nc.client.item.CraftingPatternItemModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NcParticles.FUSION_BEAM.get(), FusionBeamParticle.Provider::new);
        event.registerSpriteSet(NcParticles.FIRE_VERTICAL1.get(), igentuman.nc.client.particle.FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticles.SMOKE.get(), igentuman.nc.client.particle.FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticles.EXPLOSION.get(), igentuman.nc.client.particle.ExplosionParticle.Factory::new);
        event.registerSpriteSet(NcParticles.EXPLOSION_SEED.get(), igentuman.nc.client.particle.ExplosionSeedParticle.Factory::new);
    }

    /** 3D structure previews in screens and recipe viewers render through picture-in-picture. */
    @SubscribeEvent
    static void registerPictureInPictureRenderers(net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) {
        event.register(igentuman.nc.client.render.builder.StructurePreviewState.class,
                igentuman.nc.client.render.builder.StructurePreviewPipRenderer::new);
    }

    @SubscribeEvent
    static void registerGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAboveAll(NuclearCraft.rl("bomb_flash"), igentuman.nc.client.bomb.BombFlashOverlay.BOMB_FLASH);
        event.registerAboveAll(NuclearCraft.rl("multiblock_debug"),
                igentuman.nc.client.multiblock.MultiblockDebugOverlay.INSTANCE);
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelFeralGhoul.LAYER_LOCATION, ModelFeralGhoul::createBodyLayer);
        event.registerLayerDefinition(ModelWastelandBoss.LAYER_LOCATION, ModelWastelandBoss::createBodyLayer);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (BlockEntityType<FusionReactorControllerBE>) (BlockEntityType<?>)
                        ModEntries.get("fusion_reactor_core").blockEntity().get(),
                FusionCoreRenderer::new);
        event.registerBlockEntityRenderer(
                (BlockEntityType<EXPLBE>) (BlockEntityType<?>)
                        ModEntries.get("expl").blockEntity().get(),
                EXPLRenderer::new);
        event.registerBlockEntityRenderer(
                (BlockEntityType<BlackHoleBE>) (BlockEntityType<?>)
                        ModEntries.get("black_hole").blockEntity().get(),
                BlackholeRenderer::new);
        event.registerBlockEntityRenderer(
                (BlockEntityType<igentuman.nc.block_entity.turbine.TurbineRotorBE>) (BlockEntityType<?>)
                        ModEntries.get("turbine_rotor_shaft").blockEntity().get(),
                igentuman.nc.client.render.turbine.TurbineRotorRenderer::new);
        event.registerBlockEntityRenderer(
                (BlockEntityType<igentuman.nc.block_entity.MultiblockBuilderBE>) (BlockEntityType<?>)
                        ModEntries.get("multiblock_builder").blockEntity().get(),
                igentuman.nc.client.render.builder.MultiblockBuilderRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends EntityFeralGhoul>) (EntityType<?>)
                        Ghouls.FERAL_GHOUL.get(),
                FeralGhoulRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends EntityWastelandBoss>) (EntityType<?>)
                        Ghouls.FERAL_GHOUL_BOSS.get(),
                WastelandBossRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends EntityBlockProjectile>) (EntityType<?>)
                        Ghouls.WASTELAND_PROJECTILE.get(),
                WastelandProjectileRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends PrimedFissionBombEntity>) (EntityType<?>)
                        Bomb.PRIMED_FISSION_BOMB.get(),
                PrimedFissionBombRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends igentuman.nc.entity.Q36PulseProjectile>) (EntityType<?>)
                        Q36_PULSE_PROJECTILE.get(),
                Q36PulseProjectileRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends igentuman.nc.entity.Q36EnergyFlash>) (EntityType<?>)
                        Q36_ENERGY_FLASH.get(),
                Q36EnergyFlashRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends GravitationalAnomalyEntity>) (EntityType<?>)
                        Anomalies.GRAVITATIONAL_ANOMALY.get(),
                GravitationalAnomalyRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends AnomalyEntity>) (EntityType<?>)
                        Anomalies.ELECTRIC_ANOMALY.get(),
                AnomalyRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends AnomalyEntity>) (EntityType<?>)
                        Anomalies.RADIOACTIVE_ANOMALY.get(),
                AnomalyRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends AnomalyEntity>) (EntityType<?>)
                        Anomalies.BURNING_ANOMALY.get(),
                AnomalyRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends AnomalyEntity>) (EntityType<?>)
                        Anomalies.PSYCHO_ANOMALY.get(),
                AnomalyRenderer::new);
        event.registerEntityRenderer(
                (EntityType<? extends AnomalyEntity>) (EntityType<?>)
                        Anomalies.TELEPORTING_ANOMALY.get(),
                AnomalyRenderer::new);
    }

    /**
     * Registers fluid models (still/flowing sprites and ARGB tint) for every material fluid.
     * Fluids lighter than water render translucent, as the old translucent render layer did;
     * tint alpha still drives gas/acid translucency there.
     */
    @SubscribeEvent
    static void registerFluidModels(RegisterFluidModelsEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                registerFluidModel(event, mat);
            }
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            for (MaterialEntry mat : fuel.fluids()) {
                registerFluidModel(event, mat);
            }
        }
        for (igentuman.nc.registration.IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            for (MaterialEntry mat : isotope.fluids()) {
                registerFluidModel(event, mat);
            }
        }
    }

    private static void registerFluidModel(RegisterFluidModelsEvent event, MaterialEntry mat) {
        var materialFluid = mat.materialFluid();
        if (!(materialFluid.fluidType().get() instanceof MaterialFluidType mft)) {
            return;
        }
        boolean translucent = mft.getDensity() < 1000;
        Material still = new Material(mft.getStillTexture(), translucent);
        Material flowing = new Material(mft.getFlowingTexture(), translucent);
        Material overlay = mft.getOverlayTexture() == null ? null : new Material(mft.getOverlayTexture(), translucent);
        event.register(new FluidModel.Unbaked(still, flowing, overlay, FluidTintSources.constant(mft.getTintColor())),
                materialFluid.source(), materialFluid.flowing());
    }

    /** Resonite crystals are tinted by their effect colour; buckets are tinted by their fluid_container model. */
    @SubscribeEvent
    static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ResoniteCrystalTint.ID, ResoniteCrystalTint.MAP_CODEC);
    }
}
