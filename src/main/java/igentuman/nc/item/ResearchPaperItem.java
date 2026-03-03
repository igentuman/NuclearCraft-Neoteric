package igentuman.nc.item;

import igentuman.api.platform.NCItemStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class ResearchPaperItem extends Item {
    public ResearchPaperItem(Properties pProperties) {
        super(pProperties);
    }

    public ResearchPaperItem(Properties props, CreativeModeTab group)
    {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> list, TooltipFlag flag)
    {
        if(NCItemStacks.contains(stack, "vein")) {
            list.add(__(NCItemStacks.getString(stack, "vein")).withStyle(ChatFormatting.AQUA));
        }
        if(NCItemStacks.contains(stack, "pos")) {
            BlockPos pos = BlockPos.of(NCItemStacks.getLong(stack, "pos"));
            list.add(__("tooltip.nc.chunk_position", pos.toShortString()).withStyle(ChatFormatting.BLUE));
            list.add(__("tooltip.nc.use_in_leacher", pos.toShortString()).withStyle(ChatFormatting.GREEN));
        }
    }

}
