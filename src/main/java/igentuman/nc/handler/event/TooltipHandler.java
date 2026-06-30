package igentuman.nc.handler.event;

import igentuman.nc.config.Multiblocks;
import igentuman.nc.util.TagUtil;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static igentuman.nc.Main.MODID;
import static igentuman.nc.util.TextUtils.__;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TooltipHandler {
    private static ItemTooltipEvent processedEvent;

    @SubscribeEvent
    public static void handle(ItemTooltipEvent event) {
        if(event.equals(processedEvent)) return;
        processedEvent = event;
        addModeratorTooltip(event, event.getItemStack());
    }

    private static void addModeratorTooltip(ItemTooltipEvent event, ItemStack itemStack) {
        for(Block block: TagUtil.getBlocksByTagKey("nuclearcraft:moderators")) {
            if(itemStack.is(block.asItem())) {
                event.getToolTip().add(__("tooltip.nuclearcraft.moderator", TextUtils.numberFormat(Multiblocks.FISSION_MODERATOR_FE_MULTIPLIER.get()), TextUtils.numberFormat(Multiblocks.FISSION_MODERATOR_HEAT_MULTIPLIER.get())).withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
