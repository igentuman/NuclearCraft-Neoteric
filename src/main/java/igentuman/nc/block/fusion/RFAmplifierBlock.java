package igentuman.nc.block.fusion;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.api.multiblock.part.RFAmplifierDef;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.translatable;

public class RFAmplifierBlock extends MultiblockBlock implements TooltipBlock {

    public RFAmplifierBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    private RFAmplifierDef def() {
        return RFAmplifierDef.get(asItem().toString());
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        RFAmplifierDef def = RFAmplifierDef.get(stack.getItem().toString().replace("nuclearcraft:", ""));
        if (def == null) return;
        tooltip.accept(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.power", TextUtils.numberFormat(def.power)),
                ChatFormatting.DARK_AQUA));
        tooltip.accept(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.voltage", TextUtils.numberFormat((double) def.voltage / 1000)),
                ChatFormatting.DARK_BLUE));
        tooltip.accept(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.efficiency", TextUtils.numberFormat(def.efficiency)),
                ChatFormatting.AQUA));
        tooltip.accept(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.heat", TextUtils.numberFormat(def.heat)),
                ChatFormatting.YELLOW));
        tooltip.accept(TextUtils.applyFormat(
                translatable("tooltip.nuclearcraft.rf_amplifier.max_temp", TextUtils.numberFormat((double) def.maxTemp / 1000)),
                ChatFormatting.RED));
    }
}
