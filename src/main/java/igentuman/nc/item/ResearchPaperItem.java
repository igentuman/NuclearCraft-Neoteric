package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/** Item that displays vein type and chunk position from its NBT data in the tooltip. */
public class ResearchPaperItem extends Item {

    public ResearchPaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("vein")) {
            list.add(__(tag.getString("vein")).withStyle(ChatFormatting.AQUA));
        }
        if (tag.contains("pos")) {
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            list.add(__("tooltip.nc.chunk_position", pos.toShortString()).withStyle(ChatFormatting.BLUE));
            list.add(__("tooltip.nc.use_in_leacher", pos.toShortString()).withStyle(ChatFormatting.GREEN));
        }
    }
}
