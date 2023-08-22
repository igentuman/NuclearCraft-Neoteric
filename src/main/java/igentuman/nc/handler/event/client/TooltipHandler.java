package igentuman.nc.handler.event.client;

import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.ItemShielding;
import igentuman.nc.radiation.RadiationCleaningItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TooltipHandler {
    private static ItemTooltipEvent processedEvent;
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TooltipHandler::handle);
    }
    @SubscribeEvent
    public static void handle(ItemTooltipEvent event) {
        if(event.equals(processedEvent)) return;
        processedEvent = event;
        Item item = event.getItemStack().getItem();
        addRadiationLevelTooltip(event, item);
        addShieldintTooltip(event, event.getItemStack());
        addRadiationCleaningEffect(event, event.getItemStack());
    }

    private static void addRadiationCleaningEffect(ItemTooltipEvent event, ItemStack itemStack) {
        int radiation = RadiationCleaningItems.byItem(itemStack.getItem());
        if(radiation == 0) return;
        ChatFormatting color = ChatFormatting.GREEN;
        event.getToolTip().add(Component.translatable("tooltip.nc.radiation_removal", format(((double)radiation)/1000000000)+"Rad").withStyle(color));
    }

    private static void addShieldintTooltip(ItemTooltipEvent event, ItemStack item) {
        int shielding = ItemShielding.byItem(item.getItem());
        if(!item.getOrCreateTag().contains("rad_shielding") &&  shielding == 0) return;
        ChatFormatting color = ChatFormatting.GOLD;
        if(item.getOrCreateTag().contains("rad_shielding")) {
            shielding += item.getOrCreateTag().getInt("rad_shielding");
        }
        event.getToolTip().add(Component.translatable("tooltip.nc.rad_shielding", shielding).withStyle(color));
    }

    private static void addRadiationLevelTooltip(ItemTooltipEvent event, Item item) {
        double radiation = ItemRadiation.byItem(item);
        ChatFormatting color = ChatFormatting.GRAY;
        if(radiation > 0) {
            if(radiation > 0.0001) {
                color = ChatFormatting.YELLOW;
            }
            if(radiation > 0.001) {
                color = ChatFormatting.GOLD;
            }
            if(radiation > 0.1) {
                color = ChatFormatting.RED;
            }
            event.getToolTip().add(Component.translatable("tooltip.nc.radiation", format(radiation)+"Rad/s").withStyle(color));
        }
    }

    private static String format(Double radiation) {
        if(radiation >= 1) {
            return String.format("%.0f", radiation)+" ";
        }
        if(radiation >= 0.000999) {
            return String.format("%.0f", radiation*1000)+" m";
        }
        if(radiation >= 0.000000999) {
            return String.format("%.0f", radiation*1000000)+" u";
        }
        return String.format("%.0f", radiation*1000000000)+" p";
    }
}
