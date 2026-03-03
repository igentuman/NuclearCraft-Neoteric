// getRadiationResistance moved from RadiationManager to RadiationUtil in Mekanism 10.7.x
package igentuman.nc.mixin;

import mekanism.common.lib.radiation.RadiationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.radiation.data.PlayerRadiation.getRadiationShielding;

@Mixin(value = RadiationUtil.class, remap = false)
public abstract class MekRadiationUtil {

    @Inject(method = "getRadiationResistance(Lnet/minecraft/world/entity/LivingEntity;)D", at = @At("TAIL"), remap=false, cancellable = true)
    private static void getRadiationResistance(LivingEntity entity, CallbackInfoReturnable<Double> callback) {
        if(entity instanceof Player player) {
            double shieldingRate = (double) getRadiationShielding(player, MODID) / 10;
            callback.setReturnValue(callback.getReturnValue()+shieldingRate);
        }
    }

}
