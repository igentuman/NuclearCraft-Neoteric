package igentuman.nc.util.provider;

import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public interface IHasTranslationKey {

    /**
     * Gets the translation key for this object.
     */
    String getTranslationKey();
}