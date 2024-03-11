package igentuman.nc.block;

import igentuman.nc.block.entity.ElectromagnetBE;
import igentuman.nc.content.Electromagnets;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.List;

import static igentuman.nc.setup.registration.NCBlocks.NC_BE;

public class ElectromagnetBlock extends Block {

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
    public void appendHoverText(@NotNull ItemStack pStack, IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.rf_amplifier.power", TextUtils.numberFormat(prefab().getPower())),
                TextFormatting.DARK_AQUA));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.electromagnet.magnetic_field", TextUtils.numberFormat(prefab().getMagneticField())),
                TextFormatting.DARK_BLUE));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.description.efficiency", TextUtils.numberFormat(prefab().getEfficiency())),
                TextFormatting.AQUA));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.electromagnet.heat", TextUtils.numberFormat(prefab().getHeat())),
                TextFormatting.YELLOW));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.electromagnet.max_temp", TextUtils.numberFormat((double) prefab().getMaxTemp() /1000)),
                TextFormatting.RED));
    }

/*
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NC_BE.get(name()).get().create(pPos, pState);
    }
*/

}
