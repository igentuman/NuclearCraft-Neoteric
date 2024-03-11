package igentuman.nc.block;

import igentuman.nc.content.RFAmplifier;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.block.Block;

import java.util.List;


public class RFAmplifierBlock extends Block {
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
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.rf_amplifier.power", TextUtils.numberFormat(prefab().getPower())),
                TextFormatting.DARK_AQUA));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.rf_amplifier.voltage", TextUtils.numberFormat((double) prefab().getVoltage() /1000)),
                TextFormatting.DARK_BLUE));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.rf_amplifier.efficiency", TextUtils.numberFormat(prefab().getEfficiency())),
                TextFormatting.AQUA));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.rf_amplifier.heat", TextUtils.numberFormat(prefab().getHeat())),
                TextFormatting.YELLOW));
        list.add(TextUtils.applyFormat(
                new TranslationTextComponent("tooltip.nc.rf_amplifier.max_temp", TextUtils.numberFormat((double) prefab().getMaxTemp() /1000)),
                TextFormatting.RED));
    }

/*    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NC_BE.get(name()).get().create(pPos, pState);
    }*/
}
