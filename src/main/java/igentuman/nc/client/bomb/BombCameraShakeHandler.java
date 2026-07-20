package igentuman.nc.client.bomb;

import igentuman.nc.NuclearCraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
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
