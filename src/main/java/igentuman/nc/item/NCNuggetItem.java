package igentuman.nc.item;

import igentuman.nc.content.materials.Ingots;
import igentuman.nc.content.materials.Nuggets;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCNuggetItem extends Item {
    public NCNuggetItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Nuggets.get().registered().containsKey(this.toString().replace("_nugget", ""));
    }
}
