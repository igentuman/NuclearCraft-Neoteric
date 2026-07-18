package igentuman.nc.datagen.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class PoiTypeTags extends PoiTypeTagsProvider {
    public PoiTypeTags(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(net.minecraft.tags.PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .addOptional(rl("analyzer_poi"));
    }
}
