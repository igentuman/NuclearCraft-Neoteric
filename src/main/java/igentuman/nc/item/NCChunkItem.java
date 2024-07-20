package igentuman.nc.item;

import igentuman.nc.content.materials.Chunks;
import igentuman.nc.content.materials.Dusts;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class NCChunkItem extends Item {
    public NCChunkItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet pEnabledFeatures) {
        return Chunks.get().registered().containsKey(this.toString().replace("_chunk", ""));
    }
}
