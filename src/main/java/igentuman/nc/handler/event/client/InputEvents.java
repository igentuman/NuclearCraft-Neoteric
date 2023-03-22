package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT;
import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InputEvents {

    public static boolean SHIFT_PRESSED = false;

    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(InputEvents::onKeyPressed);
    }
    public static void onKeyPressed(InputEvent.Key event) {
        if (event.getKey() == KEY_LSHIFT || event.getKey() == KEY_RSHIFT) {
            if(event.getAction() == InputConstants.PRESS) {
                SHIFT_PRESSED = true;
            } else if(event.getAction() == InputConstants.RELEASE) {
                SHIFT_PRESSED = false;
            }
        }
    }
}
