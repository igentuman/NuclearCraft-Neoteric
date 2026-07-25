package igentuman.nc.block;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.ChargingStationBE;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class ChargingStationBlock extends BaseEntityBlock {

    private MapCodec<ChargingStationBlock> codec;
    private final String name;

    public ChargingStationBlock(String name) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(2.5f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.name = name;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new ChargingStationBlock(name));
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
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ChargingStationBE be) {
            serverPlayer.openMenu(be, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, p, st, be) -> {
                if (be instanceof GlobalBlockEntity g) g.clientTick();
            };
        }
        return (lvl, p, st, be) -> {
            if (be instanceof GlobalBlockEntity g) g.serverTick();
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof GlobalBlockEntity g) g.drops();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TextUtils.applyFormat(__("tooltip.nc.charging_station"), ChatFormatting.GOLD));
    }
}
