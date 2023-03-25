package igentuman.nc.setup;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

        });
        for(RegistryObject<Fluid> f : NCFluids.FLUIDS.getEntries())
            if(NCFluids.NC_GASES.containsKey(f.getId().getPath()))
                ItemBlockRenderTypes.setRenderLayer(f.get(), RenderType.translucent());
    }

}
