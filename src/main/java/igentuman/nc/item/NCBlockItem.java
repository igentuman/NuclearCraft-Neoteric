package igentuman.nc.item;

import igentuman.nc.content.materials.Blocks;
import igentuman.nc.content.materials.Chunks;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCBlockItem extends Item {
    public NCBlockItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Blocks.get().registered().containsKey(this.toString().replace("_block", ""));
    }
}
