package igentuman.nc.util.annotation;

import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@TypeQualifierDefault(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface NcRecipeGenerator {
}