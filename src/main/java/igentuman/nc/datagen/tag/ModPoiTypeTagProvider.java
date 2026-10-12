package igentuman.nc.datagen.tag;

import igentuman.nc.setup.NCVillagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.NuclearCraft.MODID;

public class ModPoiTypeTagProvider extends PoiTypeTagsProvider {
    public ModPoiTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(NCVillagers.ANALYZER_POI.getKey());
    }
}
