package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static igentuman.nc.NuclearCraft.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class InputEvents {

    public static boolean DESCRIPTIONS_SHOW = false;
    public static boolean SHIFT_PRESSED = false;

    public static void register(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(InputEvents::onKeyPressed);
        NeoForge.EVENT_BUS.addListener(InputEvents::onScreenKeyPressed);
        NeoForge.EVENT_BUS.addListener(InputEvents::onScreenKeyReleased);
    }
    public static void onKeyPressed(InputEvent.Key event) {
        if (event.getKey() == KEY_N && event.getModifiers() == MOD_CONTROL) {
            if(event.getAction() == RELEASE) {
                DESCRIPTIONS_SHOW = !DESCRIPTIONS_SHOW;
            }
        }

        if (event.getKey() == KEY_LSHIFT || event.getKey() == KEY_RSHIFT) {
            if(event.getAction() == PRESS) {
                SHIFT_PRESSED = true;
            } else
            if(event.getAction() == RELEASE) {
                SHIFT_PRESSED = false;
            }
        }
    }

    public static void onScreenKeyPressed(ScreenEvent.KeyPressed event) {
        if (event.getKeyCode() == KEY_LSHIFT || event.getKeyCode() == KEY_RSHIFT) {
            SHIFT_PRESSED = true;
        }
    }

    public static void onScreenKeyReleased(ScreenEvent.KeyReleased event) {
        if (event.getKeyCode() == KEY_LSHIFT || event.getKeyCode() == KEY_RSHIFT) {
            SHIFT_PRESSED = false;
        }
    }
}
