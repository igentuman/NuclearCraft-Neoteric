package igentuman.nc.util;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.Objects;

public final class CapabilityUtils {

    private CapabilityUtils() {
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addListener(@NotNull LazyOptional<?> lazyOptional, @NotNull NonNullConsumer listener) {
        lazyOptional.addListener(listener);
    }

    public static <T> T getPresentCapability(ICapabilityProvider provider, Capability<T> cap)
    {
        return Objects.requireNonNull(getCapability(provider, cap, null));
    }

    @Nullable
    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap)
    {
        return getCapability(provider, cap, null);
    }

    @Nullable
    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap, @Nullable Direction side)
    {
        LazyOptional<T> optional = provider.getCapability(cap, side);
        if(optional.isPresent())
            return optional.orElseThrow(RuntimeException::new);
        else
            return null;
    }
}