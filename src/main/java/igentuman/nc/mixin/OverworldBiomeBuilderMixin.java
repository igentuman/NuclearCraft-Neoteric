package igentuman.nc.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static igentuman.nc.handler.config.WorldConfig.BIOME_CONFIG;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND_BIOME;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    @Shadow @Final private Climate.Parameter[] temperatures;

    @Shadow @Final private Climate.Parameter nearInlandContinentalness;

    @Shadow @Final private Climate.Parameter farInlandContinentalness;

    @Shadow @Final private Climate.Parameter[] erosions;

    @Shadow @Final private Climate.Parameter[] humidities;

    @Shadow @Final private Climate.Parameter coastContinentalness;

    @Shadow @Final private Climate.Parameter midInlandContinentalness;

    @Shadow @Final private Climate.Parameter FULL_RANGE;


    @Inject(at = @At("RETURN"), method = "addLowSlice", remap = false)
    private void GE$writeLowBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter weirdness, CallbackInfo ci) {
        try {
            if (!BIOME_CONFIG.registerWasteland.get()) return;
        } catch (Exception ignore) {
            return;
        }
        injectBiome(consumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness), this.erosions[6], weirdness, 0.0f, WASTELAND_BIOME);
        for (int i = 3; i < this.temperatures.length; ++i) {
            for (int j = 0; j < 1; ++j) {
                injectBiome(consumer, this.temperatures[i], this.humidities[j], Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness), this.erosions[4], weirdness, 0.0F, WASTELAND_BIOME);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "addMidSlice", remap = false)
    private void GE$writeMidBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters, Climate.Parameter weirdness, CallbackInfo ci) {
        try {
            if (!BIOME_CONFIG.registerWasteland.get()) return;
        } catch (Exception ignore) {
            return;
        }
        for (int i = 3; i < this.temperatures.length; ++i) {
            for (int j = 0; j < 1; ++j) {
                injectBiome(parameters, this.temperatures[i], this.humidities[j], Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness), this.erosions[4], weirdness, 0.0F, WASTELAND_BIOME);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "addHighSlice", remap = false)
    private void GE$writeHighBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters, Climate.Parameter weirdness, CallbackInfo ci) {
        try {
            if(!BIOME_CONFIG.registerWasteland.get()) return;
        } catch (Exception ignore) {
            return;
        }
        for (int i = 3; i < this.temperatures.length; ++i) {
            for (int j = 0; j < 1; ++j) {
                injectBiome(parameters, this.temperatures[i], this.humidities[j], Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness), this.erosions[4], weirdness, 0.0F, WASTELAND_BIOME);
                injectBiome(parameters, this.temperatures[i], this.humidities[j], this.midInlandContinentalness, this.erosions[4], weirdness, 0.0F, WASTELAND_BIOME);
                injectBiome(parameters, this.temperatures[i], this.humidities[j], Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], weirdness, 0.0F, WASTELAND_BIOME);
                injectBiome(parameters, this.temperatures[i], this.humidities[j], Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[4], weirdness, 0.0f, WASTELAND_BIOME);
            }
        }
    }

    @Unique
    private void injectBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters, Climate.Parameter temperatureRange, Climate.Parameter parameterRange, Climate.Parameter nearInlandContinentalness, Climate.Parameter erosions, Climate.Parameter weirdness, float offset, ResourceKey<Biome> oakHammockForestKey) {
        ((OverworldBiomeBuilderInvoker) this).callAddSurfaceBiome(parameters, temperatureRange, parameterRange, nearInlandContinentalness, erosions, weirdness, offset, oakHammockForestKey);
    }
}