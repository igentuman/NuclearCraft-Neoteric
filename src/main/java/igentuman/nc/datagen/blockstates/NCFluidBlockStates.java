package igentuman.nc.datagen.blockstates;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.mutable.MutableObject;

public class NCFluidBlockStates extends ExtendedBlockstateProvider {

    public NCFluidBlockStates(DataGenerator gen, ExistingFileHelper exHelper)
    {
        super(gen, exHelper);
    }


    @Override
    protected void registerStatesAndModels() {
        for(NCFluids.FluidEntry entry : NCFluids.ALL_FLUID_ENTRIES.values())
        {
            Fluid still = entry.getStill();
            ResourceLocation stillTexture = still.getAttributes().getStillTexture();
            ModelFile model = models().getBuilder("block/fluid/"+still.getRegistryName().getPath())
                    .texture("particle", stillTexture);
            getVariantBuilder(entry.getBlock()).partialState().setModels(new ConfiguredModel(model));
        }
    }
}
