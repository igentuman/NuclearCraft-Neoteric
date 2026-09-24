package igentuman.nc.setup.entries;

import igentuman.nc.block.fusion.ElectromagnetBlock;
import igentuman.nc.block.fusion.ElectromagnetSlopeBlock;
import igentuman.nc.block.fusion.RFAmplifierBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

import static igentuman.nc.registration.ModEntryBuilder.add;

public final class HighEnergyComponents {

    private static boolean initialized;

    public static final List<String> TIERS = List.of(
            "basic", "magnesium_diboride", "niobium_tin", "niobium_titanium", "bscco"
    );

    private HighEnergyComponents() {
    }

    public static void highEnergyComponents() {
        if (initialized) return;
        initialized = true;
        for (String tier : TIERS) {
            add(tier + "_electromagnet").block(name -> new ElectromagnetBlock(properties())).build();
            add(tier + "_electromagnet_slope").block(name -> new ElectromagnetSlopeBlock(properties().noOcclusion())).build();
            add(tier + "_rf_amplifier").block(name -> new RFAmplifierBlock(properties())).build();
        }
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops();
    }
}
