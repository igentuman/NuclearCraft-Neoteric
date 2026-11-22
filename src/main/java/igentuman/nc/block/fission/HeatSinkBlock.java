package igentuman.nc.block.fission;

import igentuman.nc.api.multiblock.part.HeatSinkDef;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

public class HeatSinkBlock extends Block implements TooltipBlock {

    private final HeatSinkDef def;

    public HeatSinkBlock(Properties props, HeatSinkDef def) {
        super(props
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
        this.def = def;
    }

    public HeatSinkDef getDef() {
        return def;
    }

    public boolean isValid(Level level, BlockPos pos, @Nullable Object multiblock) {
        return false;
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(__("tooltip.nuclearcraft.heat_sink.description").withStyle(ChatFormatting.GRAY));
        tooltip.accept(__("tooltip.nuclearcraft.heat_sink.heat", TextUtils.numberFormat(def.heat))
                .withStyle(ChatFormatting.GOLD));
        if(flag.hasShiftDown()) {
            if (def.rules.length > 0) {
                tooltip.accept(TextUtils.applyFormat(def.getPlacementRule(), ChatFormatting.AQUA));
            }
        } else {
            tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.shift"), ChatFormatting.GRAY));
        }
        if (def.isActive()) {
            tooltip.accept(__("tooltip.nuclearcraft.heat_sink.active")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
