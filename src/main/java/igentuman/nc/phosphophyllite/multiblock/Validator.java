package igentuman.nc.phosphophyllite.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface Validator<T> {
    
    boolean validate(T t);
    
    static <T> Validator<T> and(Validator<T> left, Validator<T> right) {
        return (t) -> left.validate(t) && right.validate(t);
    }
    
    static <T> Validator<T> or(Validator<T> left, Validator<T> right) {
        return (t) -> left.validate(t) || right.validate(t);
    }
}
