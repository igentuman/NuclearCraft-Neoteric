package igentuman.nc.item;

import igentuman.nc.content.materials.Nuggets;
import igentuman.nc.content.materials.Plates;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCPlateItem extends Item {
    public NCPlateItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Plates.get().registered().containsKey(this.toString().replace("_plate", ""));
    }
}
