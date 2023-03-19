package igentuman.nc.datagen;

import igentuman.nc.setup.Registration;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;

public class NCStructureSetTags extends TagsProvider<StructureSet> {

    public NCStructureSetTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, BuiltinRegistries.STRUCTURE_SETS, MODID, helper);
    }

    @Override
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