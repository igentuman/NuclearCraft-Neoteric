package igentuman.nc.client.bomb;

import igentuman.nc.config.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class GroundSmokeEmitter {

    public static final int SPAWNS_PER_BOMB = 12;
    public static final int SPAWNS_GLOBAL = 24;
    public static final int MAX_LIFETIME = 120;
    public static final double MAX_DISTANCE = 512.0;
    private static final int SECTORS = 24;

    private static final List<Particle> LIVE = new ArrayList<>();
    private static int rotation;

    private GroundSmokeEmitter() {}

    public static void tick(List<ActiveBomb> bombs, Vec3 camera) {
        LIVE.removeIf(p -> !p.isAlive());
        Minecraft mc = Minecraft.getInstance();
        ParticleStatus status = mc.options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;

        int budget = Math.min(SPAWNS_GLOBAL, Client.nukeQuality.smokeLiveCap - LIVE.size());
        if (status == ParticleStatus.DECREASED) budget /= 2;
        if (budget <= 0) return;

        int eligible = 0;
        for (ActiveBomb bomb : bombs) {
            if (emits(bomb, camera)) eligible++;
        }
        if (eligible == 0) return;

        rotation = (rotation + 1) % SECTORS;
        int share = Math.min(SPAWNS_PER_BOMB, Mth.positiveCeilDiv(budget, eligible));
        for (ActiveBomb bomb : bombs) {
            if (budget <= 0) break;
            if (!emits(bomb, camera)) continue;
            int count = Math.min(budget, Math.round(share * bomb.visual.groundIntensity));
            budget -= emit(mc, bomb, count, camera);
        }
    }

    private static boolean emits(ActiveBomb bomb, Vec3 camera) {
        NukeVisualState v = bomb.visual;
        if (v.groundIntensity <= 0f || v.groundRadius <= 0f) return false;
        double reach = MAX_DISTANCE + v.groundRadius;
        return camera.distanceToSqr(Vec3.atCenterOf(bomb.epicenter)) <= reach * reach;
    }

    private static int emit(Minecraft mc, ActiveBomb bomb, int count, Vec3 camera) {
        if (count <= 0) return 0;
        NukeVisualState v = bomb.visual;
        RandomSource rng = RandomSource.create(bomb.seed ^ (long) bomb.tickCounter * 0x9E3779B97F4A7C15L);
        int front = count < 4 ? count : count * 2 / 3;
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            boolean onFront = i < front;
            float angle;
            float radius;
            if (onFront) {
                int sector = (i * SECTORS / front + rotation) % SECTORS;
                angle = (sector + rng.nextFloat()) * Mth.TWO_PI / SECTORS;
                radius = v.groundRadius * (0.92f + 0.08f * rng.nextFloat());
            } else {
                angle = rng.nextFloat() * Mth.TWO_PI;
                radius = v.groundRadius * 0.9f * Mth.sqrt(rng.nextFloat());
            }
            float dx = Mth.cos(angle) * radius;
            float dz = Mth.sin(angle) * radius;
            float h = bomb.surface.heightOffset(dx, dz);
            if (Float.isNaN(h)) continue;
            double x = bomb.epicenter.getX() + 0.5 + dx;
            double y = bomb.epicenter.getY() + 0.5 + h + 0.5;
            double z = bomb.epicenter.getZ() + 0.5 + dz;
            if (camera.distanceToSqr(x, y, z) > MAX_DISTANCE * MAX_DISTANCE) continue;
            float out = onFront ? 0.12f : 0.03f;
            Particle p = mc.particleEngine.createParticle(ParticleTypes.LARGE_SMOKE, x, y, z,
                    Mth.cos(angle) * out, onFront ? 0.02 : 0.04, Mth.sin(angle) * out);
            if (p == null) continue;
            p.setLifetime(Math.min(p.getLifetime(), MAX_LIFETIME));
            LIVE.add(p);
            spawned++;
        }
        return spawned;
    }

    public static int liveCount() {
        return LIVE.size();
    }

    public static void clear() {
        LIVE.clear();
    }
}
