package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionIrradiationChamberBE;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IrradiationChamberBlock extends Block implements EntityBlock {

    public IrradiationChamberBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }

    public IrradiationChamberBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return FissionReactor.FISSION_BE.get("fission_reactor_irradiation_chamber").get().create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof FissionIrradiationChamberBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FissionIrradiationChamberBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(Component.translatable("irradiation_chamber.descr"), ChatFormatting.AQUA));
    }

}
