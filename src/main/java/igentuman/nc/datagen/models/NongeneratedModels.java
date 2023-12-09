package igentuman.nc.datagen.models;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;

public class NongeneratedModels extends ModelProvider<NongeneratedModels.NongeneratedModel>
{
	public NongeneratedModels(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator.getPackOutput(), MODID, "block", NongeneratedModel::new, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
	}

	@Override
	public String getName()
	{
		return "Non-generated models";
	}

	public static class NongeneratedModel extends ModelBuilder<NongeneratedModel>
	{

		protected NongeneratedModel(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
		{
			super(outputLocation, existingFileHelper);
		}
	}
}
