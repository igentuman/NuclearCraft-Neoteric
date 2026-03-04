package igentuman.nc.block;

import igentuman.api.platform.NCNames;
import igentuman.nc.content.Electromagnets;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.*;
import java.util.List;

import static igentuman.nc.util.TextUtils.numberFormat;
import static net.minecraft.network.chat.Component.translatable;

public class ElectromagnetBlock extends MultiblockBlock {

    public ElectromagnetBlock(Properties pProperties) {
        super(pProperties);
    }

    public String name()
    {
        return NCNames.of(asItem()).replace("_slope", "");
    }

    public Electromagnets.MagnetPrefab prefab()
    {
        return Electromagnets.all().get(name());
    }
    public double getStrength() {
        return prefab().getMagneticField();
    }
    public double getEfficiency() {
        return prefab().getEfficiency();
    }
    public int getPower() {
        return prefab().getPower();
    }
    public int getMaxTemperature() {
        return prefab().getMaxTemp();
    }

    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(
                translatable("tooltip.nc.rf_amplifier.power", numberFormat(prefab().getPower())),
                ChatFormatting.DARK_AQUA));
        list.add(TextUtils.applyFormat(
                translatable("tooltip.nc.electromagnet.magnetic_field", numberFormat(prefab().getMagneticField())),
                ChatFormatting.DARK_BLUE));
        list.add(TextUtils.applyFormat(
                translatable("tooltip.nc.description.efficiency", numberFormat(prefab().getEfficiency())),
                ChatFormatting.AQUA));
        list.add(TextUtils.applyFormat(
                translatable("tooltip.nc.electromagnet.heat", numberFormat(prefab().getHeat())),
                ChatFormatting.YELLOW));
        list.add(TextUtils.applyFormat(
                translatable("tooltip.nc.electromagnet.max_temp", numberFormat((double) prefab().getMaxTemp() /1000)),
                ChatFormatting.RED));
    }

    public int getHeatRate() {
        return prefab().getHeatRate();
    }
}
