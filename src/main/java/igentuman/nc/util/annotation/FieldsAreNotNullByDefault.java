package igentuman.nc.util.annotation;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@NotNull
@Nonnull//Note: Must use the javax nonnull for intellij to recognize it properly in warnings
@TypeQualifierDefault(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface FieldsAreNotNullByDefault {
}