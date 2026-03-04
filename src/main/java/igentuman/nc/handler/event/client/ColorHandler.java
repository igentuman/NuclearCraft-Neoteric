package igentuman.nc.handler.event.client;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BucketItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCFluids.NC_MATERIALS;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ColorHandler {

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (Map.Entry<DeferredHolder<FluidType, FluidType>, IClientFluidTypeExtensions> entry :
                NCFluids.CLIENT_EXTENSIONS.entrySet()) {
            event.registerFluidType(entry.getValue(), entry.getKey().get());
        }
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        registerBucketColorHandler(event);
    }

    // Look up bucket color directly by item identity from FluidEntry registry.
    private static final ItemColor BUCKET_ITEM_COLOR = (stack, tintIndex) -> {
        if (tintIndex != 1) return 0xFFFFFFFF;
        net.minecraft.world.item.Item item = stack.getItem();
        for (NCFluids.FluidEntry entry : NCFluids.ALL_FLUID_ENTRIES.values()) {
            if (entry.getBucket() == item) {
                return entry.color() | 0xFF000000;
            }
        }
        return 0xFFFFFFFF;
    };

    public static void registerBucketColorHandler(RegisterColorHandlersEvent.Item event) {
        for (String name: NC_MATERIALS.keySet()) {
            event.register(BUCKET_ITEM_COLOR, NC_MATERIALS.get(name).getBucket());
        }
        for (String name: NCFluids.NC_GASES.keySet()) {
            event.register(BUCKET_ITEM_COLOR, NCFluids.NC_GASES.get(name).getBucket());
        }
    }
}
