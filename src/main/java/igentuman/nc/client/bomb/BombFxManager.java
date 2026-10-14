package igentuman.nc.client.bomb;

import igentuman.nc.NuclearCraft;
import igentuman.nc.config.Client;
import igentuman.nc.setup.NCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class BombFxManager {

    public static final int NEAR_RANGE = 128;
    public static final int MID_RANGE = 512;
    public static final int FAR_RANGE = 1500;
    public static final int MAX_RANGE = 5000;
    private static final int IDLE_SURFACE_INTERVAL = 20;

    private static final List<ActiveBomb> ACTIVE = new ArrayList<>();
    private static ClientLevel trackedLevel;

    public static void onDetonationStart(int id, BlockPos epicenter, float yield) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        syncLevel(mc.level);
        for (ActiveBomb existing : ACTIVE) {
            if (existing.id == id && existing.epicenter.equals(epicenter)) return;
        }
        double d = Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(epicenter)));
        int delay;
        if (d <= NEAR_RANGE) delay = 0;
        else if (d > MAX_RANGE) delay = -1;
        else delay = (int) Math.floor(d / 17.0);
        ACTIVE.add(new ActiveBomb(id, epicenter, yield, delay, d));
    }

    public static void clear() {
        ACTIVE.clear();
        trackedLevel = null;
    }

    private static void syncLevel(ClientLevel level) {
        if (trackedLevel != level) {
            ACTIVE.clear();
            GroundSmokeEmitter.clear();
            DebrisFireEmitter.clear();
            ShockwaveDebrisEmitter.clear();
            trackedLevel = level;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post ev) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clear();
            return;
        }
        syncLevel(mc.level);
        if (mc.isPaused() || ACTIVE.isEmpty()) return;
        Iterator<ActiveBomb> it = ACTIVE.iterator();
        while (it.hasNext()) {
            ActiveBomb b = it.next();
            b.tickCounter++;
            if (!b.soundPlayed && b.tickCounter >= b.soundDelayTicks) {
                playZoneSound(b);
                b.soundPlayed = true;
            }
            if (b.expired()) {
                it.remove();
                continue;
            }
            if (b.tickCounter <= IDLE_SURFACE_INTERVAL * 2 || b.visual.groundIntensity > 0f
                    || b.tickCounter % IDLE_SURFACE_INTERVAL == 0) {
                b.surface.tick(mc.level);
            }
            b.visual.evaluate(b, 0f);
        }
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        GroundSmokeEmitter.tick(ACTIVE, camera);
        DebrisFireEmitter.tick(ACTIVE, mc.level, camera);
        ShockwaveDebrisEmitter.tick(ACTIVE, mc.level, camera);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) ShockwaveDebrisEmitter.render(event);
    }

    public static float flashAlpha(float partialTick) {
        if (!Client.nukeHudFlash) return 0f;
        float max = 0f;
        for (ActiveBomb b : ACTIVE) {
            float a = b.flashAlpha(partialTick);
            if (a > max) max = a;
        }
        return max;
    }

    public static float cameraShakeAmount(float partialTick) {
        float max = 0f;
        for (ActiveBomb b : ACTIVE) {
            float s = b.cameraShakeAmount(partialTick);
            if (s > max) max = s;
        }
        return max;
    }

    private static void playZoneSound(ActiveBomb b) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double d = Math.sqrt(mc.player.distanceToSqr(Vec3.atCenterOf(b.epicenter)));
        DeferredHolder<SoundEvent, SoundEvent> sound = pickSound(d);
        if (sound == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound.get(), 1.0f, 1.0f));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> pickSound(double d) {
        if (d < NEAR_RANGE) return NCSounds.BOMB_BLAST_FIRST;
        if (d < MID_RANGE) return NCSounds.BOMB_BLAST_SECOND;
        if (d < FAR_RANGE) return NCSounds.BOMB_BLAST_THIRD;
        if (d < MAX_RANGE) return NCSounds.BOMB_BLAST_FOURTH;
        return null;
    }

    public static List<ActiveBomb> active() {
        return ACTIVE;
    }
}
