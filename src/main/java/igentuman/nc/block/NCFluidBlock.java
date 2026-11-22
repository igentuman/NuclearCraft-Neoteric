package igentuman.nc.block;

import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.util.NCDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

public class NCFluidBlock extends LiquidBlock {

    public NCFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    private FluidType fluidType() {
        return fluid.getFluidType();
    }

    private boolean isMolten() {
        return fluidType() instanceof MaterialFluidType mft && mft.isMolten();
    }

    private boolean isGas() {
        return fluidType().getDensity() < 0;
    }

    private boolean isToxic() {
        return fluidType() instanceof MaterialFluidType mft && mft.isToxic();
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return isMolten();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return fluidType().getLightLevel();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (isMolten()) {
            entity.igniteForSeconds(1.0F);
        }
        if (isToxic()) {
            entity.hurtServer(serverLevel, NCDamageSources.of(level, NCDamageSources.ACID), 1.0F);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (isGas()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
