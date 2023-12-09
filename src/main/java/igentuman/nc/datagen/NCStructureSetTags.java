package igentuman.nc.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import static igentuman.nc.NuclearCraft.MODID;

public class NCStructureSetTags extends TagsProvider<StructureSet> {

    public NCStructureSetTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), Registries.STRUCTURE_SET, event.getLookupProvider(), MODID, event.getExistingFileHelper());
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

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // TODO Auto-generated method stub
    }
}