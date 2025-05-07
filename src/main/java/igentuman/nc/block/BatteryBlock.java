package igentuman.nc.block;

import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static igentuman.nc.util.StackUtils.isMultiTool;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class BatteryBlock extends Block implements EntityBlock {
    public BatteryBlock(Properties pProperties) {
        super(pProperties);
    }

    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        BlockEntity blockEntity = pLevel.getExistingBlockEntity(pPos);
        if (blockEntity instanceof BatteryBE batteryBE) {
            return (int) ((batteryBE.energyStorage.getEnergyStored()/(double)batteryBE.energyStorage.getMaxEnergyStored())*15);
        }
        return 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getExistingBlockEntity(pos);
            if (be instanceof BatteryBE batteryBE)  {
                if(isMultiTool(player.getItemInHand(hand))) {
                    Direction dirToChange = result.getDirection();
                    if(player.isShiftKeyDown()) {
                        dirToChange = dirToChange.getOpposite();
                    }
                    SideModeToggleable.SideMode mode = batteryBE.toggleSideConfig(dirToChange.ordinal());
                    player.sendSystemMessage(__("message.nc.barrel.side_config", mode.name()));
                } else {
                    player.sendSystemMessage(__("tooltip.nc.energy_stored", formatEnergy(batteryBE.energyStorage.getEnergyStored()), formatEnergy(batteryBE.energyStorage.getMaxEnergyStored())).withStyle(ChatFormatting.BLUE));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getExistingBlockEntity(pPos);
            if (blockEntity instanceof BatteryBE) {

            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NCEnergyBlocks.ENERGY_BE.get(code()).get().create(pPos, pState);
    }

    public String code()
    {
        return asItem().toString();
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof NCEnergy tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof NCEnergy tile) {
                tile.tickServer();
            }
        };

    }
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (stack.hasTag()) {
            BatteryBE tileEntity = (BatteryBE) world.getExistingBlockEntity(pos);
            CompoundTag nbtData = stack.getTag();
            tileEntity.load(nbtData);
        }
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        BatteryBE batteryBE = (BatteryBE) pBlockEntity;
        CompoundTag data = batteryBE.getUpdateTag();

        ItemStack drop = new ItemStack(this);
        drop.setTag(data);
        if (!pLevel.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), drop);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable BlockGetter world, List<Component> list, TooltipFlag flag)
    {
        int storage = ENERGY_STORAGE.getCapacityFor(asItem().toString());

        list.add(__("tooltip.nc.energy_capacity", formatEnergy(storage)).withStyle(ChatFormatting.BLUE));
        list.add(__("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
    }

    public boolean registered() {
        return BatteryBlocks.registered().containsKey(code());
    }
}
