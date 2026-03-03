package igentuman.api.platform;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Platform wrapper for FluidHandlerItemStack construction.
 * In 1.21.1: FluidHandlerItemStack requires Supplier&lt;DataComponentType&lt;SimpleFluidContent&gt;&gt;
 * as first parameter (was just ItemStack, int in Forge).
 * Registers NC's own DataComponentType for fluid content on items.
 */
public final class NCFluidCapability {
    private NCFluidCapability() {}

    private static DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_CONTENT;

    public static void init(DeferredRegister.DataComponents registry) {
        FLUID_CONTENT = registry.registerComponentType("fluid_content",
                builder -> builder.persistent(SimpleFluidContent.CODEC)
                                  .networkSynchronized(SimpleFluidContent.STREAM_CODEC));
    }

    public static Supplier<DataComponentType<SimpleFluidContent>> fluidContentType() {
        return FLUID_CONTENT;
    }

    public static FluidHandlerItemStack createItemFluidHandler(ItemStack stack, int capacity) {
        return new FluidHandlerItemStack(FLUID_CONTENT, stack, capacity);
    }
}
