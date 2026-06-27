package igentuman.nc.setup;

import igentuman.nc.Main;
import igentuman.nc.client.renderer.DelayedRenderHandler;
import igentuman.nc.client.renderer.DistortShader;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.MultiblockControllerScreen;
import igentuman.nc.screen.MultiblockPortScreen;
import igentuman.nc.screen.UniversalProcessorScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Main.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
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
        });
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        ModEntries.ENTRIES.values().stream()
                .filter(ModEntry::hasMenu)
                .forEach(entry -> {
                    var block = entry.block() != null ? entry.block().get() : null;
                    if (block instanceof MultiblockControllerBlock) {
                        event.register(
                                (MenuType<MultiblockControllerContainer>) (MenuType<?>) entry.menu().get(),
                                MultiblockControllerScreen::new
                        );
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

    /**
     * Register client-side fluid rendering extensions (textures + tint color).
     */
    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() instanceof MaterialEntry mat && mat.hasFluid()) {
                var materialFluid = mat.materialFluid();
                var fluidType = materialFluid.fluidType().get();

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
                            // ARGB format: full alpha + material color
                            return 0xFF000000 | mft.getTintColor();
                        }
                    }, fluidType);
                }
            }
        }
    }
}
