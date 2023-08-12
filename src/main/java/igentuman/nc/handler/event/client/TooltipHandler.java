package igentuman.nc.handler.event.client;

import igentuman.nc.content.materials.ItemRadiation;
import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCFluids.ALL_FLUID_ENTRIES;
import static igentuman.nc.setup.registration.NCFluids.NC_MATERIALS;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TooltipHandler {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TooltipHandler::handle);
    }
    @SubscribeEvent
    public static void handle(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
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
            //for some reason it runs twice
            if(radiationTooltipExists(event.getToolTip())) {
                return;
            }
            event.getToolTip().add(Component.translatable("tooltip.nc.radiation", format(radiation)).withStyle(color));
        }
    }

    private static boolean radiationTooltipExists(List<Component> toolTip) {
        for(Component component: toolTip) {
            if(component.getString().contains("Rad/s")) {
                return true;
            }
        }
        return false;
    }

    private static String format(Double radiation) {
        if(radiation >= 1) {
            return String.format("%.0f", radiation)+" Rad/s";
        }
        if(radiation >= 0.000999) {
            return String.format("%.0f", radiation*1000)+" mRad/s";
        }
        if(radiation >= 0.000000999) {
            return String.format("%.0f", radiation*1000000)+" uRad/s";
        }
        return String.format("%.0f", radiation*1000000000)+" pRad/s";
    }

}
