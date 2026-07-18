package igentuman.nc.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;

import static igentuman.nc.handler.config.WorldConfig.BIOME_CONFIG;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND_BIOME;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    @Shadow @Final private ResourceKey<Biome>[][] MIDDLE_BIOMES;
    @Shadow @Final private ResourceKey<Biome>[][] PLATEAU_BIOMES;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void nc$injectWasteland(CallbackInfo ci) {
        boolean enabled;
        try {
            enabled = BIOME_CONFIG.registerWasteland.get();
        } catch (Exception ignore) {
            enabled = true;
        }
        if (!enabled) return;

        // temperatures[4]=hottest, temperatures[3]=warm; humidities[0]=driest
        // Replace desert with wasteland at hot+dry climate
        MIDDLE_BIOMES[4][0] = WASTELAND_BIOME;
        MIDDLE_BIOMES[3][0] = WASTELAND_BIOME;

        // Replace hot plateau biomes (badlands) with wasteland
        PLATEAU_BIOMES[4][0] = WASTELAND_BIOME;
        PLATEAU_BIOMES[4][1] = WASTELAND_BIOME;
        PLATEAU_BIOMES[4][2] = WASTELAND_BIOME;
    }
}
