package igentuman.nc.block.fusion;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.fusion.ElectromagnetDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

/** Horizontally-facing electromagnet block; shows power, magnetic field, efficiency and heat stats. */
public class ElectromagnetBlock extends MultiblockBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ElectromagnetBlock(Properties props) {
        super(props);
        BlockState state = stateDefinition.any();
        if (state.hasProperty(FACING)) {
            state = state.setValue(FACING, Direction.NORTH);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ElectromagnetDef def = ElectromagnetDef.get(stack.getItem().toString().replace("nuclearcraft:", ""));
        if (def == null) return;
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.power", TextUtils.numberFormat(def.power)),
                ChatFormatting.DARK_AQUA));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.electromagnet.magnetic_field", TextUtils.numberFormat(def.magneticField)),
                ChatFormatting.DARK_BLUE));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.electromagnet.efficiency", TextUtils.numberFormat(def.efficiency)),
                ChatFormatting.AQUA));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.electromagnet.heat", TextUtils.numberFormat(def.heat)),
                ChatFormatting.YELLOW));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.electromagnet.max_temp", TextUtils.numberFormat((double) def.maxTemp / 1000)),
                ChatFormatting.RED));
    }
}
