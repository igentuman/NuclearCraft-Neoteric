package igentuman.nc.util.provider;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IBaseProvider extends IHasTextComponent, IHasTranslationKey {

    ResourceLocation getRegistryName();

    default String getName() {
        return getRegistryName().getPath();
    }

    @Override
    default Component getTextComponent() {
        return Component.translatable(getTranslationKey());
    }
}