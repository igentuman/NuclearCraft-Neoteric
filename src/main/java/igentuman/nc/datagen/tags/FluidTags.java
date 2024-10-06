package igentuman.nc.datagen.tags;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.data.event.GatherDataEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Tags.LIQUIDS_TAG;

public class FluidTags extends FluidTagsProvider
{
	public FluidTags(DataGenerator gen, GatherDataEvent event)
	{
		super(gen.getPackOutput(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider)
	{
		for(String name: NCFluids.NC_MATERIALS.keySet()) {
			tag(LIQUIDS_TAG.get(name)).add(NCFluids.NC_MATERIALS.get(name).getStill());
			tag(LIQUIDS_TAG.get(name)).add(NCFluids.NC_MATERIALS.get(name).getFlowing());
		}
		for(String name: NCFluids.NC_GASES.keySet()) {
			tag(LIQUIDS_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getStill());
			tag(LIQUIDS_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getFlowing());

			tag(Fluids.GASEOUS).add(NCFluids.NC_GASES.get(name).getStill());
			tag(Fluids.GASEOUS).add(NCFluids.NC_GASES.get(name).getFlowing());
		}
	}
}
