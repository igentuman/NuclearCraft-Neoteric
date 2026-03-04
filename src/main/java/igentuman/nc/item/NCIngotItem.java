package igentuman.nc.item;

import igentuman.api.platform.NCNames;
import igentuman.nc.content.materials.Ingots;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCIngotItem extends Item {
    public NCIngotItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Ingots.get().registered().containsKey(NCNames.of(this).replace("_ingot", ""));
    }
}
