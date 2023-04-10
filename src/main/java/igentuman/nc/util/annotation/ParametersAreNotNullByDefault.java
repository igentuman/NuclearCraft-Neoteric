package igentuman.nc.util.annotation;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@NotNull
@Nonnull
@TypeQualifierDefault(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface ParametersAreNotNullByDefault {
}