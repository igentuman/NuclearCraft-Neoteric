package igentuman.nc.handler.event.client;

import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.ItemShielding;
import igentuman.nc.radiation.RadiationCleaningItems;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactor.moderators;

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
        miscTooltips(event, event.getItemStack());
        addRadiationLevelTooltip(event, item);
        addShieldingTooltip(event, event.getItemStack());
        addRadiationCleaningEffect(event, event.getItemStack());
        addModeratorTooltip(event, event.getItemStack());
    }

    private static void addModeratorTooltip(ItemTooltipEvent event, ItemStack itemStack) {
        for(Block block: moderators) {
/*            if(itemStack.is(block.asItem())) {
                event.getToolTip().add(new TranslationTextComponent("tooltip.nc.moderator.desc", FISSION_CONFIG.MODERATOR_FE_MULTIPLIER.get(), FISSION_CONFIG.MODERATOR_HEAT_MULTIPLIER.get()).withStyle(TextFormatting.GOLD));
            }*/
        }
    }

    private static void miscTooltips(ItemTooltipEvent event, ItemStack itemStack) {
        if(!itemStack.hasTag()) return;
        assert itemStack.getTag() != null;
        if(itemStack.getTag().contains("is_nc_analyzed")) {
            event.getToolTip().add(new TranslationTextComponent("tooltip.nc.analyzed").withStyle(TextFormatting.GOLD));
/*            if(itemStack.getItem().equals(FILLED_MAP)) {
                event.getToolTip().add(new TranslationTextComponent("tooltip.nc.use_in_leacher").withStyle(TextFormatting.GOLD));
            }*/
        }
    }

    private static void addRadiationCleaningEffect(ItemTooltipEvent event, ItemStack itemStack) {
        int radiation = RadiationCleaningItems.byItem(itemStack.getItem());
        if(radiation == 0) return;
        TextFormatting color = TextFormatting.GREEN;
        event.getToolTip().add(new TranslationTextComponent("tooltip.nc.radiation_removal", format(((double)radiation)/1000000000)+"Rad").withStyle(color));
    }

    private static void addShieldingTooltip(ItemTooltipEvent event, ItemStack item) {
        int shielding = ItemShielding.byItem(item.getItem());
        if((!item.hasTag() || !item.getTag().contains("rad_shielding")) &&  shielding == 0) return;
        TextFormatting color = TextFormatting.GOLD;
        if(item.hasTag() && item.getTag().contains("rad_shielding")) {
            shielding += item.getTag().getInt("rad_shielding");
        }
        event.getToolTip().add(new TranslationTextComponent("tooltip.nc.rad_shielding", shielding).withStyle(color));
    }

    private static void addRadiationLevelTooltip(ItemTooltipEvent event, Item item) {
        double radiation = ItemRadiation.byItem(item);
        TextFormatting color = TextFormatting.GRAY;
        if(radiation > 0) {
            if(radiation > 0.0001) {
                color = TextFormatting.YELLOW;
            }
            if(radiation > 0.001) {
                color = TextFormatting.GOLD;
            }
            if(radiation > 0.1) {
                color = TextFormatting.RED;
            }
            event.getToolTip().add(new TranslationTextComponent("tooltip.nc.radiation", format(radiation)+"Rad/s").withStyle(color));
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
