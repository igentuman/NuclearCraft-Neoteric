package igentuman.nc.handler.event.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InputEvents {

    public static boolean DESCRIPTIONS_SHOW = true;
    public static boolean SHIFT_PRESSED = false;

    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(InputEvents::onKeyPressed);
        MinecraftForge.EVENT_BUS.addListener(InputEvents::onScreenKeyPressed);
        MinecraftForge.EVENT_BUS.addListener(InputEvents::onScreenKeyReleased);
    }
    public static void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (event.getKey() == KEY_N && event.getModifiers() == MOD_CONTROL) {
            if(event.getAction() == RELEASE) {
                DESCRIPTIONS_SHOW = !DESCRIPTIONS_SHOW;
            }
        }

        if (event.getKey() == KEY_LSHIFT || event.getKey() == KEY_RSHIFT) {
            if(event.getAction() == RELEASE) {
                SHIFT_PRESSED = true;
            }
        }
    }

    public static void onScreenKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getKeyCode() == KEY_LSHIFT || event.getKeyCode() == KEY_RSHIFT) {
            SHIFT_PRESSED = true;
        }
    }

    public static void onScreenKeyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent event) {
        if (event.getKeyCode() == KEY_LSHIFT || event.getKeyCode() == KEY_RSHIFT) {
            SHIFT_PRESSED = false;
        }
    }
}
