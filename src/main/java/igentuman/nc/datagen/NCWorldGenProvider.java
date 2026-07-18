package igentuman.nc.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;

public class NCWorldGenProvider implements DataProvider {
    // Data generation for 1.19.2 - bootstrap methods removed (1.20+ API)
    public NCWorldGenProvider(DataGenerator generator, GatherDataEvent event) {
    }

    @Override
    public void run(CachedOutput pOutput) {
        // No world gen data to generate in 1.19.2
    }

    @Override
    public String getName() {
        return "NuclearCraft World Gen";
    }
}
