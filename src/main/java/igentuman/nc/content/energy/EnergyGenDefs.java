package igentuman.nc.content.energy;

import igentuman.nr.registry.Isotopes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public class EnergyGenDefs {

    public static final Map<String, int[]> RTGS = new LinkedHashMap<>();
    public static final Map<String, String> RTG_ISOTOPES = new LinkedHashMap<>();
    public static final Map<String, int[]> SOLAR_PANELS = new LinkedHashMap<>();

    public static final int DECAY_GENERATOR_CAPACITY = 10__000;
    public static final int DECAY_GENERATOR_DURATION = 36_000;
    public static final int DECAY_GENERATOR_BASE_FE = 50;

    public static final TagKey<Block> DECAY_GENERATOR_BLOCKS =
            BlockTags.create(rl("decay_generator_blocks"));

    static {
        RTGS.put("uranium_rtg", new int[]{112, 560});
        RTGS.put("americium_rtg", new int[]{448, 57_800});
        RTGS.put("plutonium_rtg", new int[]{1792, 200_000});
        RTGS.put("californium_rtg", new int[]{4096, 1_900_000});

        RTG_ISOTOPES.put("uranium_rtg", Isotopes.U_238);
        RTG_ISOTOPES.put("americium_rtg", Isotopes.AM_241);
        RTG_ISOTOPES.put("plutonium_rtg", Isotopes.PU_238);
        RTG_ISOTOPES.put("californium_rtg", Isotopes.CF_250);

        SOLAR_PANELS.put("solar_panel_basic", new int[]{28});
        SOLAR_PANELS.put("solar_panel_advanced", new int[]{112});
        SOLAR_PANELS.put("solar_panel_du", new int[]{448});
        SOLAR_PANELS.put("solar_panel_elite", new int[]{1792});
    }

    public static int rtgGeneration(String name) {
        return RTGS.getOrDefault(name, new int[]{0, 0})[0];
    }

    public static int rtgRadiation(String name) {
        return RTGS.getOrDefault(name, new int[]{0, 0})[1];
    }

    public static String rtgIsotope(String name) {
        return RTG_ISOTOPES.get(name);
    }

    public static int solarGeneration(String name) {
        return SOLAR_PANELS.getOrDefault(name, new int[]{0})[0];
    }
}
