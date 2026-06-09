package igentuman.nc.client.bomb;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BombCameraShakeHandler {

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles ev) {
        float shake = BombFxManager.cameraShakeAmount((float) ev.getPartialTick());
        if (shake <= 0.001f) return;
        float secs = (System.nanoTime() % 1_000_000_000_000L) / 1_000_000_000f;
        float roll = Mth.sin(secs * 90f) * shake * 6f;
        float pitch = Mth.sin(secs * 70f + 1.3f) * shake * 2.5f;
        float yaw = Mth.cos(secs * 80f + 0.5f) * shake * 2.5f;
        ev.setRoll(ev.getRoll() + roll);
        ev.setPitch(ev.getPitch() + pitch);
        ev.setYaw(ev.getYaw() + yaw);
    }
}
