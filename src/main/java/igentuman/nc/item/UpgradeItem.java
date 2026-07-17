package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;

public class UpgradeItem extends Item {

    public UpgradeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
    {
        if(stack.is(NC_ITEMS.get("upgrade_energy").get()) && isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.upgrade_energy.tier", GTCEU_CONFIG.ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER.get()).withStyle(ChatFormatting.GOLD));
        }
        list.add(__("tooltip." + stack.getItem().toString()).withStyle(ChatFormatting.GREEN));
    }
}
