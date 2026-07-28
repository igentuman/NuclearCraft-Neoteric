package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.render.bomb.PrimedFissionBombRenderer;
import igentuman.nc.client.render.q36.Q36EnergyFlashRenderer;
import igentuman.nc.client.render.q36.Q36PulseProjectileRenderer;
import igentuman.nc.client.renderer.AnomalyShader;
import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.client.renderer.FeralGhoulRenderer;
import igentuman.nc.client.renderer.anomaly.AnomalyRenderer;
import igentuman.nc.client.renderer.anomaly.GravitationalAnomalyRenderer;
import igentuman.nc.entity.EntityFeralGhoul;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.setup.entries.*;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.DistortShader;
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
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static igentuman.nc.setup.entries.Parts.Q36_ENERGY_FLASH;
import static igentuman.nc.setup.entries.Parts.Q36_PULSE_PROJECTILE;

/** Client-only setup: registers screens, renderers, particle providers, and fluid render extensions. */
@Mod(value = NuclearCraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class Client {
    public Client(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register the game-bus render handlers (level-stage events).
            DelayedRenderHandler.register();
            DistortShader.register();
            AnomalyShader.register();
            registerFluidRenderLayers();
            ItemProperties.register(
                    Crafter.CRAFTING_PATTERN.get(),
                    NuclearCraft.rl("encoded"),
                    (stack, lvl, entity, seed) ->
                           CraftingPattern.isEncoded(stack) ? 1f : 0f);
        });
    }

    private static void registerFluidRenderLayers() {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                var materialFluid = mat.materialFluid();
                if (materialFluid.fluidType().get().getDensity() < 1000) {
                    ItemBlockRenderTypes.setRenderLayer(materialFluid.source().get(), RenderType.translucent());
                    ItemBlockRenderTypes.setRenderLayer(materialFluid.flowing().get(), RenderType.translucent());
                }
            }
        }
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
                    } else if (entry.name().equals("charging_station")) {
                        event.register(
                                (MenuType<UniversalProcessorContainer>) (MenuType<?>) entry.menu().get(),
                                igentuman.nc.screen.ChargingStationScreen::new
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
    static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(igentuman.nc.client.model.BatteryModelLoader.ID, new igentuman.nc.client.model.BatteryModelLoader());
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NcParticles.FUSION_BEAM.get(), FusionBeamParticle.Provider::new);
        event.registerSpriteSet(NcParticles.FIRE_VERTICAL1.get(), igentuman.nc.client.particle.FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticles.SMOKE.get(), igentuman.nc.client.particle.FireVerticalParticle.Factory::new);
        event.registerSpriteSet(NcParticles.EXPLOSION.get(), igentuman.nc.client.particle.ExplosionParticle.Factory::new);
        event.registerSpriteSet(NcParticles.EXPLOSION_SEED.get(), igentuman.nc.client.particle.ExplosionSeedParticle.Factory::new);
    }

    @SubscribeEvent
    static void registerGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAboveAll(NuclearCraft.rl("bomb_flash"), igentuman.nc.client.bomb.BombFlashOverlay.BOMB_FLASH);
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelFeralGhoul.LAYER_LOCATION, ModelFeralGhoul::createBodyLayer);
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
        // Empty renderer stub for the primed bomb entity; all visuals are client FX (Phase 7).
        event.registerEntityRenderer(
                (EntityType<? extends EntityFeralGhoul>) (EntityType<?>)
                        Ghouls.FERAL_GHOUL.get(),
                FeralGhoulRenderer::new);
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
     * Register client-side fluid rendering extensions (textures + tint color).
     */
    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return igentuman.nc.client.item.CraftingPatternRenderer.get();
            }
        }, igentuman.nc.setup.entries.Crafter.CRAFTING_PATTERN.get());
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                registerFluidExtension(event, mat.materialFluid().fluidType().get());
            }
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            for (MaterialEntry mat : fuel.fluids()) {
                registerFluidExtension(event, mat.materialFluid().fluidType().get());
            }
        }
        for (igentuman.nc.registration.IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            for (MaterialEntry mat : isotope.fluids()) {
                registerFluidExtension(event, mat.materialFluid().fluidType().get());
            }
        }
    }

    private static void registerFluidExtension(RegisterClientExtensionsEvent event, net.neoforged.neoforge.fluids.FluidType fluidType) {
        if (fluidType instanceof MaterialFluidType mft) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return mft.getStillTexture();
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return mft.getFlowingTexture();
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return mft.getOverlayTexture();
                }

                @Override
                public int getTintColor() {
                    // Full ARGB; alpha drives translucency for gases/acids.
                    return mft.getTintColor();
                }
            }, fluidType);
        }
    }

    /**
     * Tints fluid buckets by the fluid's color. {@link DynamicFluidContainerModel.Colors} reads the
     * fluid's {@link IClientFluidTypeExtensions#getTintColor()} for the masked fluid layer; the
     * matching bucket model is generated with {@code applyTint(true)}.
     */
    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        DynamicFluidContainerModel.Colors bucketColors = new DynamicFluidContainerModel.Colors();
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                event.register(bucketColors, mat.bucket());
            }
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            for (MaterialEntry mat : fuel.fluids()) {
                event.register(bucketColors, mat.bucket());
            }
        }
        for (igentuman.nc.registration.IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            for (MaterialEntry mat : isotope.fluids()) {
                event.register(bucketColors, mat.bucket());
            }
        }
    }
}
