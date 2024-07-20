package igentuman.nc.mixin;

import igentuman.nc.block.entity.energy.BatteryBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningRodBlock.class)
public class LightningRodBlockMixin {

    @Inject(method = "onLightningStrike", at = @At("TAIL"), remap = false)
    private void onLightningStrike(BlockState pState, Level pLevel, BlockPos pPos, CallbackInfo ci) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos.below());
        if (blockEntity instanceof BatteryBE batteryBE) {
            batteryBE.onLightningStrike();
        }
    }
}
