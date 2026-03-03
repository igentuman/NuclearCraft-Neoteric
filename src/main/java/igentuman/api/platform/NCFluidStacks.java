package igentuman.api.platform;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Platform translation layer for FluidStack custom data.
 * <p>
 * MC 1.21 removed {@code FluidStack.hasTag()} and {@code getTag()}.
 * All custom fluid data now lives in the {@link DataComponents#CUSTOM_DATA}
 * component backed by {@link CustomData}.
 */
public final class NCFluidStacks {

    private NCFluidStacks() {}

    /** Replaces {@code fluidStack.hasTag()}. */
    public static boolean hasCustomData(FluidStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /**
     * Replaces {@code fluidStack.getTag()}.
     * Returns a COPY, or null if no data exists.
     */
    @Nullable
    public static CompoundTag getTag(FluidStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : null;
    }
}
