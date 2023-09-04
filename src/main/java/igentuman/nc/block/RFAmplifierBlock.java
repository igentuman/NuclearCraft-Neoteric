package igentuman.nc.block;

import igentuman.nc.content.RFAmplifier;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

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

}
