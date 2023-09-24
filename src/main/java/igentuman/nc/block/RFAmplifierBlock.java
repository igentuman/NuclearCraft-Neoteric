package igentuman.nc.block;

import igentuman.nc.block.entity.RFAmplifierBE;
import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.content.RFAmplifier;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.setup.registration.NCBlocks.NC_BE;

public class RFAmplifierBlock extends Block implements EntityBlock {
    public RFAmplifierBlock(Properties pProperties) {
        super(pProperties);
    }

    public String name()
    {
        return asItem().toString();
    }

    public RFAmplifier.RFAmplifierPrefab prefab()
    {
        return RFAmplifier.all().get(name());
    }
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.rf_amplifier.power", TextUtils.numberFormat(prefab().getPower())),
                ChatFormatting.DARK_AQUA));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.rf_amplifier.voltage", TextUtils.numberFormat((double) prefab().getVoltage() /1000)),
                ChatFormatting.DARK_BLUE));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.rf_amplifier.efficiency", TextUtils.numberFormat(prefab().getEfficiency())),
                ChatFormatting.AQUA));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.rf_amplifier.heat", TextUtils.numberFormat(prefab().getHeat())),
                ChatFormatting.YELLOW));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.rf_amplifier.max_temp", TextUtils.numberFormat((double) prefab().getMaxTemp() /1000)),
                ChatFormatting.RED));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NC_BE.get(name()).get().create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof RFAmplifierBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof RFAmplifierBE tile) {
                tile.tickServer();
            }
        };
    }
}
