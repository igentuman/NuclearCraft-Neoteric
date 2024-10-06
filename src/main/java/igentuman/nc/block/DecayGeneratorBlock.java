package igentuman.nc.block;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.setup.registration.NCEnergyBlocks;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DecayGeneratorBlock extends Block implements EntityBlock {
    public DecayGeneratorBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(5.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public DecayGeneratorBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
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


    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(Component.translatable("rtg.fe_generation", TextUtils.numberFormat(RTGs.all().get(code()).config().getGeneration())), ChatFormatting.GOLD));
    }

}
