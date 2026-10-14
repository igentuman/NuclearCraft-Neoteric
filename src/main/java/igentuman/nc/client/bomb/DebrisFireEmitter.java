package igentuman.nc.client.bomb;

import igentuman.nc.config.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class DebrisFireEmitter {

    public static final double MAX_DISTANCE = 384.0;
    public static final int MIN_BURN_TICKS = 300;
    public static final int MAX_BURN_TICKS = 900;
    public static final int MAX_BOMBS = 2;
    private static final int IGNITE_TICKS = 20;
    private static final int SPAWNS_PER_TICK = 96;
    private static final float SMOKE_RATE = 0.12f;
    private static final float MIN_SCALE = 2.5f;
    private static final float MAX_SCALE = 4.5f;
    private static final double MIN_FACING_DISTANCE = 16.0;

    private static final List<Fire> FIRES = new ArrayList<>();
    private static final List<Particle> LIVE = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static int cursor;

    private record Fire(ActiveBomb bomb, double x, double y, double z, int born, int burn, float strength, float shade) {

        int age() {
            return bomb.tickCounter - born;
        }

        boolean burnedOut() {
            int age = age();
            return age < 0 || age >= burn;
        }

        float envelope() {
            int age = age();
            float ignite = Mth.clamp(age / (float) IGNITE_TICKS, 0f, 1f);
            float decay = 1f - NukeTimeline.smootherstep((age - burn * 0.4f) / (burn * 0.6f));
            return ignite * decay * strength;
        }
    }

    private DebrisFireEmitter() {}

    public static void tick(List<ActiveBomb> bombs, ClientLevel level, Vec3 camera) {
        LIVE.removeIf(p -> !p.isAlive());
        FIRES.removeIf(f -> f.burnedOut() || !bombs.contains(f.bomb));

        int maxFires = Client.nukeQuality.debrisFires;
        for (ActiveBomb bomb : bombs) ignite(bomb, level, camera, maxFires);

        Minecraft mc = Minecraft.getInstance();
        ParticleStatus status = mc.options.particles().get();
        if (status == ParticleStatus.MINIMAL || FIRES.isEmpty()) return;
        int budget = Math.min(SPAWNS_PER_TICK, Client.nukeQuality.debrisLiveCap - LIVE.size());
        if (budget <= 0) return;
        float rate = status == ParticleStatus.DECREASED ? 0.5f : 1f;

        int n = FIRES.size();
        cursor = (cursor + 1) % n;
        for (int i = 0; i < n && budget > 0; i++) {
            budget -= burn(mc, FIRES.get((cursor + i) % n), camera, rate, budget);
        }
    }

    private static void ignite(ActiveBomb bomb, ClientLevel level, Vec3 camera, int maxFires) {
        NukeVisualState v = bomb.visual;
        float outer = v.groundIntensity > 0f ? v.groundRadius : 0f;
        float inner = Math.max(Math.min(bomb.debrisSweep, outer), v.baseRadius);
        bomb.debrisSweep = outer;
        if (outer <= inner) return;

        float max = NukeTimeline.GROUND_MAX_RADIUS * v.r;
        float expected = maxFires * (outer * outer - inner * inner) / (max * max);
        int count = Mth.floor(expected + RANDOM.nextFloat());
        double toCameraX = camera.x - (bomb.epicenter.getX() + 0.5);
        double toCameraZ = camera.z - (bomb.epicenter.getZ() + 0.5);
        boolean facing = toCameraX * toCameraX + toCameraZ * toCameraZ > MIN_FACING_DISTANCE * MIN_FACING_DISTANCE;
        float facingAngle = (float) Mth.atan2(toCameraZ, toCameraX);
        for (int i = 0; i < count && FIRES.size() < maxFires * MAX_BOMBS; i++) {
            float radius = Mth.sqrt(Mth.lerp(RANDOM.nextFloat(), inner * inner, outer * outer));
            float angle = facing
                    ? facingAngle + (RANDOM.nextFloat() - 0.5f) * Mth.PI
                    : RANDOM.nextFloat() * Mth.TWO_PI;
            int x = Mth.floor(bomb.epicenter.getX() + 0.5f + Mth.cos(angle) * radius);
            int z = Mth.floor(bomb.epicenter.getZ() + 0.5f + Mth.sin(angle) * radius);
            if (!level.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (y <= level.getMinBuildHeight()) continue;
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) continue;
            float strength = 1f - 0.6f * Mth.clamp(radius / max, 0f, 1f);
            int burn = Math.round(Mth.nextInt(RANDOM, MIN_BURN_TICKS, MAX_BURN_TICKS) * (0.5f + 0.5f * strength));
            float shade = 0.6f + 0.25f * RANDOM.nextFloat();
            FIRES.add(new Fire(bomb, x + 0.5, y, z + 0.5, bomb.tickCounter, burn, strength, shade));
        }
    }

    private static int burn(Minecraft mc, Fire fire, Vec3 camera, float rate, int budget) {
        float e = fire.envelope();
        if (e <= 0f) return 0;
        if (camera.distanceToSqr(fire.x, fire.y, fire.z) > MAX_DISTANCE * MAX_DISTANCE) return 0;
        if (budget <= 0 || RANDOM.nextFloat() >= SMOKE_RATE * rate * e) return 0;
        Particle p = mc.particleEngine.createParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                fire.x + jitter(1.0), fire.y + 0.5, fire.z + jitter(1.0),
                jitter(0.01), 0.06 + 0.04 * RANDOM.nextDouble(), jitter(0.01));
        if (p == null) return 0;
        p.scale(Mth.lerp(RANDOM.nextFloat(), MIN_SCALE, MAX_SCALE) * (0.6f + 0.4f * fire.strength));
        p.setColor(fire.shade, fire.shade * 0.97f, fire.shade * 0.93f);
        LIVE.add(p);
        return 1;
    }

    private static double jitter(double amount) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * amount;
    }

    public static int fireCount() {
        return FIRES.size();
    }

    public static int liveCount() {
        return LIVE.size();
    }

    public static void clear() {
        FIRES.clear();
        LIVE.clear();
        cursor = 0;
    }
}
