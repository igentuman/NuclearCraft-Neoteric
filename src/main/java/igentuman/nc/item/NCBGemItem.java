package igentuman.nc.item;

import igentuman.nc.content.materials.Blocks;
import igentuman.nc.content.materials.Gems;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCBGemItem extends Item {
    public NCBGemItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Gems.get().registered().containsKey(this.toString().replace("_gem", ""));
    }
}
