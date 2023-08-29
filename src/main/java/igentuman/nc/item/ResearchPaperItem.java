package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ResearchPaperItem extends Item {
    public ResearchPaperItem(Properties pProperties) {
        super(pProperties);
    }

    public ResearchPaperItem(Properties props, CreativeModeTab group)
    {
        super(props.tab(group));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
    {
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("vein")) {
            list.add(Component.translatable(tag.getString("vein")).withStyle(ChatFormatting.AQUA));
        }
        if(tag.contains("pos")) {
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            list.add(Component.translatable("tooltip.nc.chunk_position", pos.toShortString()).withStyle(ChatFormatting.BLUE));
            list.add(Component.translatable("tooltip.nc.use_in_leacher", pos.toShortString()).withStyle(ChatFormatting.GREEN));
        }
    }

}
