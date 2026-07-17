package igentuman.nc.handler.event.client;

import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCFluids.ALL_FLUID_ENTRIES;
import static igentuman.nc.setup.registration.NCFluids.NC_MATERIALS;
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
        event.register(CRYSTAL_ITEM_COLOR, NCItems.RESONITE_CRYSTAL.get());
    }

    /**
     * Tints the resonite crystal (layer0, tintIndex 0) with its rolled buff's particle colour so the
     * crystal's hue signals which effect it grants. Raw or effect-less crystals return white (no tint).
     */
    private static final ItemColor CRYSTAL_ITEM_COLOR = (stack, tintIndex) -> {
        if (tintIndex != 0) {
            return 0xFFFFFF;
        }
        MobEffect effect = ResoniteCrystalItem.effect(stack);
        return effect == null ? 0xFFFFFF : effect.getColor();
    };

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        for(String gas: NCFluids.NC_GASES.keySet()) {
            ItemBlockRenderTypes.setRenderLayer(ALL_FLUID_ENTRIES.get(gas).getFlowing(), RenderType.translucent());
        }

        for(String fluid: NC_MATERIALS.keySet()) {
            if(fluid.contains("molten")) return;
            ItemBlockRenderTypes.setRenderLayer(NC_MATERIALS.get(fluid).getStill(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(NC_MATERIALS.get(fluid).getFlowing(), RenderType.translucent());
        }
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
