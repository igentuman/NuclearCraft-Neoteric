package igentuman.nc.util.math;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single {@link FloatingLong}-valued argument and returns no result.  This is a specialization of {@link Consumer} for {@link
 * FloatingLong}s, used to make it cleaner and easier to declare {@link Consumer}'s for {@link FloatingLong}s.
 */
@FunctionalInterface
public interface FloatingLongConsumer extends Consumer<FloatingLong> {

    @Override
    void accept(@NotNull FloatingLong floatingLong);
}