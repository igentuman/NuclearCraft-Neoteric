package igentuman.nc.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.config.WorldGen;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.List;

public class ConfigurableOreFeature extends Feature<ConfigurableOreFeature.Config> {

    public ConfigurableOreFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> ctx) {
        Config cfg = ctx.config();
        WorldGen.OreGenConfig oreConfig = WorldGen.ORE_CONFIGS.get(cfg.material());
        int size = (oreConfig != null && WorldGen.SPEC != null && WorldGen.SPEC.isLoaded())
                ? oreConfig.veinSize().get() : cfg.fallbackSize();
        if (size <= 0) return false;
        OreConfiguration ore = new OreConfiguration(cfg.targets(), size);
        FeaturePlaceContext<OreConfiguration> delegate = new FeaturePlaceContext<>(
                ctx.topFeature(), ctx.level(), ctx.chunkGenerator(), ctx.random(), ctx.origin(), ore);
        return Feature.ORE.place(delegate);
    }

    public record Config(String material, List<OreConfiguration.TargetBlockState> targets, int fallbackSize) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("material").forGetter(Config::material),
                OreConfiguration.TargetBlockState.CODEC.listOf().fieldOf("targets").forGetter(Config::targets),
                Codec.INT.fieldOf("fallback_size").forGetter(Config::fallbackSize)
        ).apply(instance, Config::new));
    }
}
