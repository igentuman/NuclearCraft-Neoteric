package igentuman.nc.datagen.blockstates;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class NCFluidBlockStates extends ExtendedBlockstateProvider {

    public NCFluidBlockStates(DataGenerator gen, GatherDataEvent event)
    {
        super(gen, event.getExistingFileHelper());
    }


    @Override
    protected void registerStatesAndModels() {
        for(NCFluids.FluidEntry entry : NCFluids.ALL_FLUID_ENTRIES.values())
        {
            Fluid still = entry.getStill();
            Mutable<IClientFluidTypeExtensions> box = new MutableObject<>();
            still.getFluidType().initializeClient(box::setValue);
            ResourceLocation stillTexture = box.getValue().getStillTexture();
            String renderType = "minecraft:solid";
            if(still.getFluidType().getDensity() < 1000) {
                renderType = "minecraft:translucent";
            }
            ModelFile model = models().getBuilder("block/fluid/"+ ForgeRegistries.FLUIDS.getKey(still).getPath())
                    .texture("particle", stillTexture);
            getVariantBuilder(entry.getBlock()).partialState().setModels(new ConfiguredModel(model));
        }
    }
}
