package igentuman.nc.block;

import igentuman.nc.content.Electromagnets;
import igentuman.nc.content.RFAmplifier;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ElectromagnetBlock extends Block {
    public ElectromagnetBlock(Properties pProperties) {
        super(pProperties);
    }

    public String name()
    {
        return asItem().toString();
    }

    public Electromagnets.MagnetPrefab prefab()
    {
        return Electromagnets.all().get(name());
    }
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.rf_amplifier.power", TextUtils.numberFormat(prefab().getPower())),
                ChatFormatting.DARK_AQUA));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.electromagnet.magnetic_field", TextUtils.numberFormat(prefab().getMagneticField())),
                ChatFormatting.DARK_BLUE));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.electromagnet.efficiency", TextUtils.numberFormat(prefab().getEfficiency())),
                ChatFormatting.AQUA));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.electromagnet.heat", TextUtils.numberFormat(prefab().getHeat())),
                ChatFormatting.YELLOW));
        list.add(TextUtils.applyFormat(
                Component.translatable("tooltip.nc.electromagnet.max_temp", TextUtils.numberFormat((double) prefab().getMaxTemp() /1000)),
                ChatFormatting.RED));
    }

}
