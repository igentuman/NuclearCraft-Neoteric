package igentuman.nc.setup;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.client.render.fusion.FusionCoreRenderer;
import igentuman.nc.screen.FissionReactorScreen;
import igentuman.nc.screen.FusionReactorScreen;
import igentuman.nc.screen.MultiblockControllerScreen;
import igentuman.nc.screen.MultiblockPortScreen;
import igentuman.nc.screen.UniversalProcessorScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = NuclearCraft.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
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

    /**
     * Modded fluids default to the solid render layer, so tint alpha has no effect.
     * Lighter fluids (gases, acids - density below water) must render translucent for
     * their alpha to show; dense molten metals stay opaque on the solid layer.
     */
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
                        } else {
                            event.register(menuType, MultiblockControllerScreen::new);
                        }
                    } else if (block instanceof MultiblockPartBlock) {
                        event.register(
                                (MenuType<MultiblockPortContainer>) (MenuType<?>) entry.menu().get(),
                                MultiblockPortScreen::new
                        );
                    } else {
                        event.register(
                                (MenuType<UniversalProcessorContainer>) (MenuType<?>) entry.menu().get(),
                                UniversalProcessorScreen::new
                        );
                    }
                });
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (BlockEntityType<FusionReactorControllerBE>) (BlockEntityType<?>)
                        ModEntries.get("fusion_reactor_core").blockEntity().get(),
                FusionCoreRenderer::new);
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
