package igentuman.nc.block.kugelblitz;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.kugelblitz.EXPLBE;
import igentuman.nc.block_entity.kugelblitz.EXPLProxyBE;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EXPLProxyBlock extends BaseEntityBlock {

    private MapCodec<EXPLProxyBlock> codec;
    private final String name;

    public EXPLProxyBlock(BlockBehaviour.Properties props, String name) {
        super(props);
        this.name = name;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new EXPLProxyBlock(props, name));
        }
        return codec;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get(name).blockEntity().get().create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof EXPLProxyBE proxy) {
            EXPLBE core = proxy.getCoreBE();
            if (core != null) {
                serverPlayer.openMenu(core, core.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof EXPLProxyBE proxy) {
            proxy.destroyCore();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
