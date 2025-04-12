package igentuman.nc.datagen;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.TagUtil;
import igentuman.nc.util.TextureUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
		for (String name: FuelManager.all().keySet()) {
			for(String subType: FuelManager.all().get(name).keySet()) {
				for(String type: new String[]{"", "_za", "_ox","_ni"}) {
					String key = "fuel_"+name +"_"+ subType+type;
					key = key.replace("-", "_");
					tag(NCFluids.LIQUIDS_TAG.get(key)).add(NCFluids.NC_MATERIALS.get(key).getStill());
					tag(NCFluids.LIQUIDS_TAG.get(key)).add(NCFluids.NC_MATERIALS.get(key).getFlowing());
					tag(NCFluids.LIQUIDS_TAG.get("depleted_"+key)).add(NCFluids.NC_MATERIALS.get("depleted_"+key).getStill());
					tag(NCFluids.LIQUIDS_TAG.get("depleted_"+key)).add(NCFluids.NC_MATERIALS.get("depleted_"+key).getFlowing());
				}
			}
		}
		for(String name: Materials.isotopes()) {
			for(String type: new String[]{"", "_za", "_ox","_ni"}) {
				tag(NCFluids.LIQUIDS_TAG.get(name+type)).add(NCFluids.NC_MATERIALS.get(name+type).getStill());
				tag(NCFluids.LIQUIDS_TAG.get(name+type)).add(NCFluids.NC_MATERIALS.get(name+type).getFlowing());
			}
		}
		for(String name: NCFluids.NC_MATERIALS.keySet()) {
			tag(NCFluids.LIQUIDS_TAG.get(name)).add(NCFluids.NC_MATERIALS.get(name).getStill());
			tag(NCFluids.LIQUIDS_TAG.get(name)).add(NCFluids.NC_MATERIALS.get(name).getFlowing());
		}
		for(String name: NCFluids.NC_GASES.keySet()) {
			tag(NCFluids.LIQUIDS_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getStill());
			tag(NCFluids.LIQUIDS_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getFlowing());

			tag(NCFluids.GASES_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getStill());
			tag(NCFluids.GASES_TAG.get(name)).add(NCFluids.NC_GASES.get(name).getFlowing());
		}
	}
}
