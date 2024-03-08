package igentuman.nc.util.math;

import net.minecraftforge.common.util.NonNullSupplier;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.function.Supplier;

/**
 * Represents a supplier of {@link FloatingLong}-valued results.  This is a specialization of {@link Supplier} for {@link FloatingLong}s, used to make it cleaner and
 * easier to declare {@link Supplier}'s for {@link FloatingLong}s.
 */
@FunctionalInterface
public interface FloatingLongSupplier extends Supplier<FloatingLong>, NonNullSupplier<FloatingLong> {

    @NotNull
    @Override
    FloatingLong get();
}