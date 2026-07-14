package igentuman.nc.block.fusion;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.fusion.RFAmplifierDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

/** RF amplifier block; shows power, voltage, efficiency, heat and max-temperature stats in the tooltip. */
public class RFAmplifierBlock extends MultiblockBlock {

    public RFAmplifierBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    private RFAmplifierDef def() {
        return RFAmplifierDef.get(asItem().toString());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RFAmplifierDef def = RFAmplifierDef.get(stack.getItem().toString().replace("nuclearcraft:", ""));
        if (def == null) return;
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.power", TextUtils.numberFormat(def.power)),
                ChatFormatting.DARK_AQUA));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.voltage", TextUtils.numberFormat((double) def.voltage / 1000)),
                ChatFormatting.DARK_BLUE));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.efficiency", TextUtils.numberFormat(def.efficiency)),
                ChatFormatting.AQUA));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.heat", TextUtils.numberFormat(def.heat)),
                ChatFormatting.YELLOW));
        tooltip.add(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.max_temp", TextUtils.numberFormat((double) def.maxTemp / 1000)),
                ChatFormatting.RED));
    }
}
