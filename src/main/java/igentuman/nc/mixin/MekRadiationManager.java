// Verified against Mekanism 10.7.18.84 for NeoForge 1.21.1:
// - isRadiationEnabled() and radiate(Level, BlockPos, double) confirmed on RadiationManager
// - getRadiationResistance(LivingEntity) MOVED to RadiationUtil — split into MekRadiationUtil mixin
package igentuman.nc.mixin;

import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;

@Mixin(value = RadiationManager.class, remap = false)
public abstract class MekRadiationManager {

    public boolean isMekRadiationEnabled()
    {
        return MekanismConfig.general.radiationEnabled.getOrDefault();
    }

    @Inject(method = "isRadiationEnabled", at = @At("TAIL"), remap=false, cancellable = true)
    public void isRadiationEnabled(CallbackInfoReturnable<Boolean> callback)
    {
        if(!RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) return;
        callback.setReturnValue(true);
    }

    @Inject(method = "radiate(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;D)V", at = @At("HEAD"), remap=false, cancellable = true)
    public void radiate(Level level, BlockPos source, double magnitude, CallbackInfo callback) {
        if(!RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) return;
        if(level == null) return;
        igentuman.nc.radiation.data.RadiationManager.get(level).addRadiation(level, magnitude*10, source.getX(), source.getY(), source.getZ());
        if(!isMekRadiationEnabled()) {
            callback.cancel();
        }
    }

}
