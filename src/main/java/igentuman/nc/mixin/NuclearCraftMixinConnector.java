package igentuman.nc.mixin;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class NuclearCraftMixinConnector implements IMixinConnector {
    
    @Override
    public void connect() {
        Mixins.addConfiguration("nuclearcraft.mixins.json");
    }
}