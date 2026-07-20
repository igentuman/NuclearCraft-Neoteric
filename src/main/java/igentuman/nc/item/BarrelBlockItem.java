package igentuman.nc.item;

import igentuman.nc.block.storage.AbstractStorageBlock;
import igentuman.nc.content.storage.StorageDefs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static igentuman.nc.util.TextUtils.formatLiquid;

public class BarrelBlockItem extends BlockItem {

    public BarrelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (getBlock() instanceof AbstractStorageBlock block) {
            int capacity = StorageDefs.barrelCapacityMb(block.storageName());
            tooltip.add(Component.translatable("tooltip.nuclearcraft.liquid_capacity",
                    formatLiquid(capacity)).withStyle(ChatFormatting.BLUE));
        }
        tooltip.add(Component.translatable("tooltip.nuclearcraft.use_wrench").withStyle(ChatFormatting.YELLOW));
    }
}
