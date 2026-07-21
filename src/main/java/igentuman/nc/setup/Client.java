package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.render.bomb.PrimedFissionBombRenderer;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.container.MultiblockPortContainer;
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
import igentuman.nc.setup.entries.Bomb;
import igentuman.nc.setup.entries.Storage;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
            registerFluidRenderLayers();
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
                (EntityType<? extends PrimedFissionBombEntity>) (EntityType<?>)
                        Bomb.PRIMED_FISSION_BOMB.get(),
                PrimedFissionBombRenderer::new);
    }

    /**
     * Register client-side fluid rendering extensions (textures + tint color).
     */
    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
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
