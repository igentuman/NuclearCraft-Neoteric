package igentuman.nc.util.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Nonnull
@TypeQualifierDefault(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface ParametersAreNotNullByDefault {
}