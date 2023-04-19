package igentuman.nc.block;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.setup.energy.SolarPanels;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NCSolarPanelBlock extends Block implements EntityBlock {
    public NCSolarPanelBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public NCSolarPanelBlock(Properties pProperties) {
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
        return NCEnergyBlocks.ENERGY_BE.get(code()).get().create(pPos, pState);
    }

    public String code()
    {
        return SolarPanels.getCode(asItem().toString());
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof NCProcessorBE tile) {
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
        list.add(TextUtils.applyFormat(Component.translatable("solar_panel.fe_generation", TextUtils.numberFormat(SolarPanels.all().get(asItem().toString().replace("solar_panel_","")).getGeneration())), ChatFormatting.GOLD));
    }

}
