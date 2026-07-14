package igentuman.nc.datagen.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

/** Generates entity loot tables. */
public class ModEntityLootTableProvider  extends EntityLootSubProvider {

    public ModEntityLootTableProvider(HolderLookup.Provider lookupProvider) {
        super(FeatureFlags.DEFAULT_FLAGS, lookupProvider);

    }
    @Override
    public void generate() {

    }
}
