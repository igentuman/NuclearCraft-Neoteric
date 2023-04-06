package igentuman.nc.util.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Nonnull
@TypeQualifierDefault({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullDefault {
    // so i don't rely on any minecraft/mojang code, but can still use this
}
