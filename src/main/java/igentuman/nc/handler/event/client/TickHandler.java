package igentuman.nc.handler.event.client;

import igentuman.nc.client.sound.GeigerSound;
import igentuman.nc.client.sound.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class TickHandler {

    public static String currentScreenCode = "";

    public static final Minecraft minecraft = Minecraft.getInstance();

    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TickHandler::onTick);
    }
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickStart();
        }
    }

    protected static GeigerSound geigerSound;
    public static void tickStart() {
        if (minecraft.player == null) {
            return;
        }
        GeigerSound toPlay = GeigerSound.create(minecraft.player);
        if(toPlay != null && (geigerSound == null || geigerSound.radiationLevel != toPlay.radiationLevel)) {
            if(geigerSound != null) {
         //       SoundHandler.stopSound(geigerSound);
            }
            geigerSound = toPlay;
         //   SoundHandler.playSound(geigerSound);
        }

        if(toPlay == null && geigerSound != null) {
         //   SoundHandler.stopSound(geigerSound);
            geigerSound = null;
        }
    }
}
