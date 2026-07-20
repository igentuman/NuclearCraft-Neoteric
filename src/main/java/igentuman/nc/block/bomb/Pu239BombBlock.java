package igentuman.nc.block.bomb;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.bomb.Pu239BombBE;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class Pu239BombBlock extends BaseEntityBlock {

    private final String name;
    private final MapCodec<? extends BaseEntityBlock> codec;

    public Pu239BombBlock(BlockBehaviour.Properties props, String name) {
        super(props);
        this.name = name;
        this.codec = simpleCodec(p -> new Pu239BombBlock(p, name));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get(name).blockEntity().get().create(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Pu239BombBE bomb)) return;
        if (placer instanceof Player player && !(placer instanceof FakePlayer)) {
            bomb.placerUuid = player.getUUID().toString();
            bomb.placedChecked = true;
            bomb.setChanged();
        } else {
            Block.popResource(level, pos, new ItemStack(this));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlock(pos, false, null, 512);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        if (level.isClientSide) return;
        if (!level.hasNeighborSignal(pos)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Pu239BombBE bomb) {
            bomb.arm();
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof Pu239BombBE bomb) bomb.clientTick();
            };
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof Pu239BombBE bomb) bomb.serverTick();
        };
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TextUtils.applyFormat(__("block.nuclearcraft.pu_239_bomb.desc"), ChatFormatting.RED));
    }
}
