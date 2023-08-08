package igentuman.nc.handler.event.client;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraft.world.level.block.Blocks.WATER;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorHandler {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ColorHandler::registerItemColorHandlers);
        MinecraftForge.EVENT_BUS.addListener(ColorHandler::registerBlockColorHandlers);
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
        for (String name: NCFluids.NC_MATERIALS.keySet()) {
            event.register(BUCKET_ITEM_COLOR, NCFluids.NC_MATERIALS.get(name).getBucket());
        }
        for (String name: NCFluids.NC_GASES.keySet()) {
            event.register(BUCKET_ITEM_COLOR, NCFluids.NC_GASES.get(name).getBucket());
        }
    }
}
