package igentuman.nc.datagen.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.data.event.GatherDataEvent;

import static igentuman.nc.NuclearCraft.MODID;

public class NCStructureSetTags extends TagsProvider<StructureSet> {

    public NCStructureSetTags(DataGenerator generator, GatherDataEvent event) {
        super(generator, BuiltinRegistries.STRUCTURE_SETS, MODID, event.getExistingFileHelper());
    }


    protected void addTags() {
       /* tag(Registration.WASTELAND_DIMENSION_STRUCTURE_SET)
                .add(ResourceKey.create(BuiltinRegistries.STRUCTURE_SETS.key(), new ResourceLocation(MODID, "portal")))
                .add(ResourceKey.create(BuiltinRegistries.STRUCTURE_SETS.key(), new ResourceLocation(MODID, "nc_laboratory")))
        ;*/
    }

    @Override
    public String getName() {
        return "NuclearCraft Structure Tags";
    }
}
