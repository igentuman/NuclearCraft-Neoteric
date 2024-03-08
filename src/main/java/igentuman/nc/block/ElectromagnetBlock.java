package igentuman.nc.block;

import igentuman.nc.block.entity.ElectromagnetBE;
import igentuman.nc.content.Electromagnets;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.List;

import static igentuman.nc.setup.registration.NCBlocks.NC_BE;

public class ElectromagnetBlock extends Block implements EntityBlock {

    public ElectromagnetBlock(Properties pProperties) {
        super(pProperties);
    }

    public String name()
    {
        return asItem().toString().replace("_slope", "");
    }

    public Electromagnets.MagnetPrefab prefab()
    {
        return Electromagnets.all().get(name());
    }
    public void appendHoverText(@NotNull ItemStack pStack, BlockGetter pLevel, List<Component> list, TooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(
                new TranslatableComponent("tooltip.nc.rf_amplifier.power", TextUtils.numberFormat(prefab().getPower())),
                ChatFormatting.DARK_AQUA));
        list.add(TextUtils.applyFormat(
                new TranslatableComponent("tooltip.nc.electromagnet.magnetic_field", TextUtils.numberFormat(prefab().getMagneticField())),
                ChatFormatting.DARK_BLUE));
        list.add(TextUtils.applyFormat(
                new TranslatableComponent("tooltip.nc.description.efficiency", TextUtils.numberFormat(prefab().getEfficiency())),
                ChatFormatting.AQUA));
        list.add(TextUtils.applyFormat(
                new TranslatableComponent("tooltip.nc.electromagnet.heat", TextUtils.numberFormat(prefab().getHeat())),
                ChatFormatting.YELLOW));
        list.add(TextUtils.applyFormat(
                new TranslatableComponent("tooltip.nc.electromagnet.max_temp", TextUtils.numberFormat((double) prefab().getMaxTemp() /1000)),
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
                if (t instanceof ElectromagnetBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof ElectromagnetBE tile) {
                tile.tickServer();
            }
        };
    }
}
