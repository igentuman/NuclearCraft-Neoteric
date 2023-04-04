package igentuman.nc.util.provider;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

@MethodsReturnNonnullByDefault
public interface IHasTextComponent {
    Component getTextComponent();
}