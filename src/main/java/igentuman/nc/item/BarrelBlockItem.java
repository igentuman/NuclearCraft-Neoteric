package igentuman.nc.item;

import igentuman.nc.block.storage.AbstractStorageBlock;
import igentuman.nc.content.storage.StorageDefs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.formatLiquid;

public class BarrelBlockItem extends BlockItem {

    public BarrelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (getBlock() instanceof AbstractStorageBlock block) {
            int capacity = StorageDefs.barrelCapacityMb(block.storageName());
            tooltip.accept(Component.translatable("tooltip.nuclearcraft.liquid_capacity",
                    formatLiquid(capacity)).withStyle(ChatFormatting.BLUE));
        }
        tooltip.accept(Component.translatable("tooltip.nuclearcraft.use_wrench").withStyle(ChatFormatting.YELLOW));
    }
}
