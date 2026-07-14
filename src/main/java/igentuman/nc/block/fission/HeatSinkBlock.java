package igentuman.nc.block.fission;

import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/** Fission reactor heat sink block; carries a {@link HeatSinkDef} and shows its cooling stats in the tooltip. */
public class HeatSinkBlock extends Block {

    private final HeatSinkDef def;

    public HeatSinkBlock(HeatSinkDef def) {
        super(BlockBehaviour.Properties.of()
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(__("tooltip.nuclearcraft.heat_sink.heat", TextUtils.numberFormat(def.heat))
                .withStyle(ChatFormatting.GOLD));
        if(flag.hasShiftDown()) {
            if (def.rules.length > 0) {
                tooltip.add(TextUtils.applyFormat(def.getPlacementRule(), ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.shift"), ChatFormatting.GRAY));
        }
        if (def.isActive()) {
            tooltip.add(__("tooltip.nuclearcraft.heat_sink.active")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
