package igentuman.nc.mixin;

import igentuman.nc.block_entity.storage.BatteryBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningRodBlock.class)
public class LightningRodBlockMixin {

    @Inject(method = "onLightningStrike", at = @At("TAIL"))
    private void nc$onLightningStrike(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        BlockPos below = pos.relative(state.getValue(LightningRodBlock.FACING).getOpposite());
        if (level.getBlockEntity(below) instanceof BatteryBE battery) {
            battery.onLightningStrike();
        }
    }
}
