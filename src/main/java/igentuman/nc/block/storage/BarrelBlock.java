package igentuman.nc.block.storage;

import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.BarrelBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import static igentuman.nc.util.TextUtils.formatLiquid;

public class BarrelBlock extends AbstractStorageBlock {

    // Approximate the model's octagonal footprint with one-pixel steps.
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5, 0, 0, 11, 16, 16),
            Block.box(4, 0, 1, 12, 16, 15),
            Block.box(3, 0, 2, 13, 16, 14),
            Block.box(2, 0, 3, 14, 16, 13),
            Block.box(1, 0, 4, 15, 16, 12),
            Block.box(0, 0, 5, 16, 16, 11)
    ).optimize();

    public BarrelBlock(Properties properties, String name) {
        super(properties, name);
    }

    @Override
    protected AbstractStorageBlock create(Properties properties) {
        return new BarrelBlock(properties, name);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean persistOnDrop(AbstractStorageBE be) {
        return be instanceof BarrelBE barrel && barrel.hasContent();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (tryWrench(stack, level, pos, player, hit)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BarrelBE barrel) {
            if (!level.isClientSide) {
                boolean handled = FluidUtil.interactWithFluidHandler(player, hand, barrel.fluidTank);
                if (!handled) sendInfo(player, barrel);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BarrelBE barrel) {
            sendInfo(player, barrel);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void sendInfo(Player player, BarrelBE barrel) {
        FluidStack fluid = barrel.fluidTank.getFluidInTank(0);
        int capacity = barrel.getCapacity();
        if (fluid.isEmpty()) {
            player.sendSystemMessage(Component.translatable("tooltip.nuclearcraft.liquid_empty",
                    formatLiquid(capacity)).withStyle(ChatFormatting.BLUE));
        } else {
            player.sendSystemMessage(Component.translatable("tooltip.nuclearcraft.liquid_stored",
                    fluid.getHoverName(), formatLiquid(fluid.getAmount()), formatLiquid(capacity)).withStyle(ChatFormatting.BLUE));
        }
    }
}
