package igentuman.nc.mixin;

import igentuman.nc.content.materials.Ores;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.Reference;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.MaterialsConfig.ORE_CONFIG;

@Mixin(PlacedFeature.class)
public class PlacedFeatureMixin {

    @Inject(method = "placeWithContext", at = @At("HEAD"), cancellable = true, remap = false)
    private void placeWithBiomeCheck(PlacementContext pContext, RandomSource pSource, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {

/*        Holder<ConfiguredFeature<?, ?>> feature = pContext.topFeature().get().feature();
        if (feature.unwrapKey().get().location().getNamespace().equals(MODID)) {
            String name = feature.unwrapKey().get().location().getPath().replace("_ore", "");
            int veinSize = Ores.all().get(name).config().veinSize;
            if(veinSize < 1 || !Ores.all().get(name).config().registered) {
                cir.setReturnValue(false);
                cir.cancel();
            }
            CountPlacement newPlacement = CountPlacement.of(veinSize);
           // pContext.topFeature().get().placement().set(0, newPlacement);
        }*/
    }

}
