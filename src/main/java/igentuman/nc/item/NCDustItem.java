package igentuman.nc.item;

import igentuman.nc.content.materials.Dusts;
import igentuman.nc.content.materials.Plates;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCDustItem extends Item {
    public NCDustItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Dusts.get().registered().containsKey(this.toString().replace("_dust", ""));
    }
}
