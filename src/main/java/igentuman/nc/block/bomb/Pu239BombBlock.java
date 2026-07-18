package igentuman.nc.block.bomb;

import igentuman.nc.block.bomb.entity.Pu239BombBE;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.setup.registration.NCBlocks.PU_239_BOMB_BE;
import static igentuman.nc.util.TextUtils.__;

public class Pu239BombBlock extends Block implements EntityBlock {

    public Pu239BombBlock() {
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(5.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return PU_239_BOMB_BE.get().create(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (world.isClientSide) return;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Pu239BombBE bomb) {
            if (placer instanceof net.minecraft.world.entity.player.Player player && !(placer instanceof FakePlayer)) {
                bomb.placerUuid = player.getUUID().toString();
                bomb.placedChecked = true;
                bomb.setChanged();
            } else {
                Block.popResource(world, pos, new ItemStack(this));
                if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.destroyBlock(pos, false);
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        if (level.isClientSide) return;
        if (level.hasNeighborSignal(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Pu239BombBE bomb) {
                bomb.arm();
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, pos, st, t) -> {
                if (t instanceof Pu239BombBE be) be.tickClient();
            };
        }
        return (lvl, pos, st, t) -> {
            if (t instanceof Pu239BombBE be) be.tickServer();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        list.add(TextUtils.applyFormat(__("block.nuclearcraft.pu_239_bomb.desc"), ChatFormatting.RED));
    }
}
