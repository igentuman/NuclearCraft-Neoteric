package igentuman.nc.datagen;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;

class FluidTags extends FluidTagsProvider
{
	public FluidTags(DataGenerator gen, ExistingFileHelper existingFileHelper)
	{
		super(gen, MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
	{
		for(String name: NCFluids.NC_MATERIALS.keySet()) {
			tag(NCFluids.LIQUIDS_TAG.get(name)).add(NCFluids.NC_MATERIALS.get(name).getStill());
		}
		for(String name: NCFluids.NC_GASES.keySet()) {
			tag(NCFluids.LIQUIDS_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getStill());

			tag(Fluids.GASEOUS).add(NCFluids.NC_GASES.get(name).getStill());
		}
	}
}
