package igentuman.nc.util.functions;

import java.util.Objects;

@FunctionalInterface
public interface FourPredicate<T, U, V, Z>
{
    boolean test(T t, U u, V v, Z z);

    default FourPredicate<T, U, V, Z> and(FourPredicate<? super T, ? super U, ? super V, ? super Z> other) {
        Objects.requireNonNull(other);
        return (T t, U u, V v, Z z) -> test(t, u, v, z) && other.test(t, u, v, z);
    }

    default FourPredicate<T, U, V, Z> negate() {
        return (T t, U u, V v, Z z) -> !test(t, u, v, z);
    }

    default FourPredicate<T, U, V, Z> or(FourPredicate<? super T, ? super U, ? super V, ? super Z> other) {
        Objects.requireNonNull(other);
        return (T t, U u, V v, Z z) -> test(t, u, v, z) || other.test(t, u, v, z);
    }
}
