package igentuman.nc.handler.event.client;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.client.color.item.ItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCFluids.NC_MATERIALS;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ColorHandler {
    public static void register(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ColorHandler::registerItemColorHandlers);
        NeoForge.EVENT_BUS.addListener(ColorHandler::registerBlockColorHandlers);
    }
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        registerBucketColorHandler(event);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        for (String name: NCFluids.NC_GASES.keySet()) {

        }
    }

    private static final ItemColor BUCKET_ITEM_COLOR = new DynamicFluidContainerModel.Colors();
    public static void registerBucketColorHandler(RegisterColorHandlersEvent.Item event) {
        for (String name: NC_MATERIALS.keySet()) {
            event.register(BUCKET_ITEM_COLOR, NC_MATERIALS.get(name).getBucket());
        }
        for (String name: NCFluids.NC_GASES.keySet()) {
            event.register(BUCKET_ITEM_COLOR, NCFluids.NC_GASES.get(name).getBucket());
        }
    }
}
