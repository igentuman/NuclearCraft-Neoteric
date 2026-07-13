package igentuman.nc.client.bomb;

import igentuman.nc.setup.registration.NCSounds;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BombFxManager {

    public static final int NEAR_RANGE = 128;
    public static final int MID_RANGE = 512;
    public static final int FAR_RANGE = 1500;
    public static final int MAX_RANGE = 5000;

    public static final int PARTICLE_VIEW_DISTANCE = 800;

    private static final List<ActiveBomb> ACTIVE = new ArrayList<>();

    public static void onDetonationStart(int id, BlockPos epicenter, float yield) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        double d = Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(epicenter)));
        int delay;
        if (d <= NEAR_RANGE) delay = 0;
        else if (d > MAX_RANGE) delay = -1;
        else delay = (int) Math.floor(d / 17.0);
        ACTIVE.add(new ActiveBomb(id, epicenter, yield, delay, d));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ACTIVE.clear();
            return;
        }
        Iterator<ActiveBomb> it = ACTIVE.iterator();
        while (it.hasNext()) {
            ActiveBomb b = it.next();
            b.tickCounter++;
            if (!b.soundPlayed && b.tickCounter >= b.soundDelayTicks) {
                playZoneSound(b);
                b.soundPlayed = true;
            }
            if (b.distance <= PARTICLE_VIEW_DISTANCE) {
                spawnPhaseParticles(mc.level, b);
            }
            if (b.tickCounter > b.lifeEnd()) it.remove();
        }
    }

    private static void spawnPhaseParticles(ClientLevel level, ActiveBomb b) {
        if (b.tickCounter >= b.fireballStart() && b.tickCounter < b.fireballEnd()) {
            spawnFireball(level, b);
        }
        if (b.tickCounter >= b.groundCloudStart() && b.tickCounter < b.groundCloudEnd()) {
            spawnGroundCloud(level, b);
        }
        if (b.tickCounter >= b.stemStart() && b.tickCounter < b.mushroomEnd()) {
            spawnMushroom(level, b);
        }
    }

    private static final DustParticleOptions DUST_DARK = new DustParticleOptions(new Vector3f(0.22f, 0.18f, 0.15f), 4.0f);
    private static final DustParticleOptions DUST_BROWN = new DustParticleOptions(new Vector3f(0.38f, 0.28f, 0.20f), 4.0f);
    private static final DustParticleOptions DUST_EMBER = new DustParticleOptions(new Vector3f(0.95f, 0.45f, 0.10f), 3.0f);

    private static void spawnFireball(ClientLevel level, ActiveBomb b) {
        RandomSource rng = level.random;
        int local = b.tickCounter - b.fireballStart();
        float maxR = Math.max(8f, b.yield * 32f);
        float growT = Mth.clamp(local / 30f, 0f, 1f);
        float radius = maxR * growT;
        double cx = b.epicenter.getX() + 0.5;
        double cy = b.epicenter.getY() + 0.5;
        double cz = b.epicenter.getZ() + 0.5;

        int fireCount = Math.max(0, 60 - local / 2);
        for (int i = 0; i < fireCount; i++) {
            double theta = rng.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * rng.nextDouble() - 1.0);
            double sinPhi = Math.sin(phi);
            double r = radius * (0.7 + 0.3 * rng.nextDouble());
            double x = cx + r * sinPhi * Math.cos(theta);
            double y = cy + r * Math.cos(phi) * 0.7;
            double z = cz + r * sinPhi * Math.sin(theta);
            double vy = 0.05 + rng.nextDouble() * 0.1;

            level.addAlwaysVisibleParticle(NcParticleTypes.FIRE_VERTICAL1.get(), true, x, y, z, 0, vy, 0);
            if ((i & 1) == 0) {
                level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, 0, vy * 0.5, 0);
            }
        }

        if (local < 80) {
            float ringR = local * (maxR / 40f);
            int ringCount = Math.min(80, 24 + local);
            for (int i = 0; i < ringCount; i++) {
                double a = (i / (double) ringCount) * Math.PI * 2.0 + rng.nextDouble() * 0.05;
                double x = cx + ringR * Math.cos(a);
                double z = cz + ringR * Math.sin(a);
                double y = cy - 0.5 + rng.nextDouble() * 1.5;
                level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, 0, 0.02, 0);
                if ((i & 3) == 0) {
                    level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x, y, z, 0, 0.05, 0);
                }
            }
        }
    }

    private static void spawnMushroom(ClientLevel level, ActiveBomb b) {
        RandomSource rng = level.random;
        int local = b.tickCounter - b.stemStart();
        float stemHeight = Math.min(100f, Math.max(40f, b.yield * 60f));
        float stemRadius = Math.max(4f, b.yield * 8f);
        float capRadius = stemRadius * 3.5f;
        float growT = Mth.clamp(local / 60f, 0f, 1f);
        double cx = b.epicenter.getX() + 0.5;
        double cy = b.epicenter.getY() + 0.5;
        double cz = b.epicenter.getZ() + 0.5;

        int stemBands = 9;
        int perBand = 9;
        for (int band = 0; band < stemBands; band++) {
            double bandT = (band + rng.nextDouble()) / stemBands;
            if (bandT > growT) continue;
            double h = bandT * stemHeight;
            double rWiden = stemRadius * (0.55 + 0.45 * bandT);
            for (int i = 0; i < perBand; i++) {
                double a = rng.nextDouble() * Math.PI * 2.0;
                double r = rWiden * Math.sqrt(rng.nextDouble());
                double x = cx + r * Math.cos(a);
                double z = cz + r * Math.sin(a);
                double y = cy + h + (rng.nextDouble() - 0.5) * 2.0;
                double vy = 0.08 + rng.nextDouble() * 0.06;
                level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, 0, vy, 0);
                if (i % 3 == 0) {
                    level.addAlwaysVisibleParticle(NcParticleTypes.FIRE_VERTICAL1.get(), true, x, y, z, 0, vy, 0);
                }
                if (i % 2 == 0) {
                    DustParticleOptions tint = (band < 3) ? DUST_EMBER : (rng.nextBoolean() ? DUST_DARK : DUST_BROWN);
                    level.addAlwaysVisibleParticle(tint, true, x, y, z, 0, vy * 0.5, 0);
                }
                if (band < 2 && i % 4 == 0) {
                    level.addAlwaysVisibleParticle(NcParticleTypes.FIRE_VERTICAL1.get(), true, x, y, z, 0, vy * 0.4, 0);
                }
                if (i % 5 == 0) {
                    level.addAlwaysVisibleParticle(ParticleTypes.SQUID_INK, true, x, y, z, 0, vy * 0.3, 0);
                }
            }
        }

        int coreCount = 6;
        for (int i = 0; i < coreCount; i++) {
            double h = rng.nextDouble() * stemHeight * growT;
            double a = rng.nextDouble() * Math.PI * 2.0;
            double r = stemRadius * 0.3 * rng.nextDouble();
            double x = cx + r * Math.cos(a);
            double z = cz + r * Math.sin(a);
            double y = cy + h;
            level.addAlwaysVisibleParticle(NcParticleTypes.EXPLOSION.get(), true, x, y, z, 0, 0.05, 0);
        }

        if (b.tickCounter >= b.capStart()) {
            int capLocal = b.tickCounter - b.capStart();
            float capProgress = Mth.clamp(capLocal / 60f, 0f, 1f);
            float curCapR = capRadius * Math.max(0.2f, capProgress);
            double capCenterY = cy + stemHeight * growT;

            int domeCount = 60;
            for (int i = 0; i < domeCount; i++) {
                double theta = rng.nextDouble() * Math.PI * 2.0;
                double phiTop = Math.acos(rng.nextDouble());
                double rr = curCapR * (0.55 + 0.45 * rng.nextDouble());
                double sp = Math.sin(phiTop);
                double x = cx + rr * sp * Math.cos(theta);
                double z = cz + rr * sp * Math.sin(theta);
                double y = capCenterY + rr * Math.cos(phiTop) * 0.55;
                double vx = Math.cos(theta) * 0.04;
                double vz = Math.sin(theta) * 0.04;
                level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, vx, 0.03, vz);
                if ((i & 1) == 0) {
                    level.addAlwaysVisibleParticle(NcParticleTypes.EXPLOSION_SEED.get(), true, x, y, z, vx * 0.5, 0.02, vz * 0.5);
                }
                if ((i & 3) == 0) {
                    level.addAlwaysVisibleParticle(NcParticleTypes.FIRE_VERTICAL1.get(), true, x, y + 2, z, vx, 0.05, vz);
                }
                DustParticleOptions tint = (i & 1) == 0 ? DUST_DARK : DUST_BROWN;
                level.addAlwaysVisibleParticle(tint, true, x, y, z, vx, 0.02, vz);
                if ((i & 7) == 0) {
                    level.addAlwaysVisibleParticle(ParticleTypes.SQUID_INK, true, x, y - 1, z, vx, -0.01, vz);
                }
            }

            int skirtCount = 50;
            for (int i = 0; i < skirtCount; i++) {
                double a = rng.nextDouble() * Math.PI * 2.0;
                double r = curCapR * (0.25 + 0.25 * rng.nextDouble());
                double x = cx + r * Math.cos(a);
                double z = cz + r * Math.sin(a);
                double y = capCenterY - stemRadius * 0.5 + (rng.nextDouble() - 0.5) * stemRadius;
                double vx = Math.cos(a) * 0.08;
                double vz = Math.sin(a) * 0.08;
                level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, vx, -0.02, vz);
                if ((i & 2) == 0) {
                    level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x, y, z, vx, 0, vz);
                }
            }

            int emitterCount = 8;
            for (int i = 0; i < emitterCount; i++) {
                double a = rng.nextDouble() * Math.PI * 2.0;
                double r = curCapR * (0.3 + 0.6 * rng.nextDouble());
                double x = cx + r * Math.cos(a);
                double z = cz + r * Math.sin(a);
                double y = capCenterY + (rng.nextDouble() - 0.2) * stemRadius;
                level.addAlwaysVisibleParticle(NcParticleTypes.EXPLOSION.get(), true, x, y, z, 0, 0.02, 0);
            }
        }
    }

    private static void spawnGroundCloud(ClientLevel level, ActiveBomb b) {
        RandomSource rng = level.random;
        int local = b.tickCounter - b.groundCloudStart();
        float stemRadius = Math.max(4f, b.yield * 8f);
        float maxRingR = stemRadius * 5.0f;
        float ringGrowT = Mth.clamp(local / 40f, 0f, 1f);
        float ringR = maxRingR * Math.max(0.35f, ringGrowT);
        float ringFade = 1f - Mth.clamp((local - 600f) / 300f, 0f, 1f);
        double cx = b.epicenter.getX() + 0.5;
        double cy = b.epicenter.getY() + 0.5;
        double cz = b.epicenter.getZ() + 0.5;

        int ringDensity = (int) (260 * ringFade);
        for (int i = 0; i < ringDensity; i++) {
            double a = rng.nextDouble() * Math.PI * 2.0;
            double r = ringR * (0.05 + 0.95 * Math.sqrt(rng.nextDouble()));
            double x = cx + r * Math.cos(a);
            double z = cz + r * Math.sin(a);
            double y = cy + rng.nextDouble() * stemRadius * 1.2;
            double vx = Math.cos(a) * (0.12 + 0.1 * rng.nextDouble());
            double vz = Math.sin(a) * (0.12 + 0.1 * rng.nextDouble());
            level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, vx, 0.03, vz);
            if ((i & 1) == 0) {
                DustParticleOptions tint = rng.nextBoolean() ? DUST_BROWN : DUST_DARK;
                level.addAlwaysVisibleParticle(tint, true, x, y, z, vx, 0.02, vz);
            }
            if ((i & 2) == 0) {
                level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x, y + 0.5, z, vx * 0.4, 0.02, vz * 0.4);
            }
            if ((i & 3) == 0) {
                level.addAlwaysVisibleParticle(NcParticleTypes.SMOKE.get(), true, x, y, z, vx * 0.5, 0.01, vz * 0.5);
            }
            if ((i & 5) == 0) {
                level.addAlwaysVisibleParticle(NcParticleTypes.FIRE_VERTICAL1.get(), true, x, y, z, vx * 1.2, 0, vz * 1.2);
            }
        }

        if (local < 360) {
            int emitterCount = (int) (10 * Mth.clamp(1f - local / 360f, 0f, 1f));
            for (int i = 0; i < emitterCount; i++) {
                double a = rng.nextDouble() * Math.PI * 2.0;
                double r = ringR * (0.4 + 0.5 * rng.nextDouble());
                double x = cx + r * Math.cos(a);
                double z = cz + r * Math.sin(a);
                double y = cy + rng.nextDouble() * 2.0;
                level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x, y, z, 0, 0.02, 0);
            }
        }

        float fireFade = Mth.clamp(1f - local / 720f, 0f, 1f);
        int baseDust = (int) (90 * fireFade);
        for (int i = 0; i < baseDust; i++) {
            double a = rng.nextDouble() * Math.PI * 2.0;
            double r = stemRadius * 2.2 * rng.nextDouble();
            double x = cx + r * Math.cos(a);
            double z = cz + r * Math.sin(a);
            double y = cy + rng.nextDouble() * 4.0;
            level.addAlwaysVisibleParticle(DUST_EMBER, true, x, y, z, 0, 0.05, 0);
            if ((i & 1) == 0) {
                level.addAlwaysVisibleParticle(ParticleTypes.LAVA, true, x, y, z, 0, 0.08, 0);
            }
            if ((i & 1) == 0) {
                double vy = 0.05 + rng.nextDouble() * 0.12;
                level.addAlwaysVisibleParticle(ParticleTypes.FLAME, true, x, y, z, 0, vy, 0);
            }
            if ((i & 3) == 0) {
                level.addAlwaysVisibleParticle(ParticleTypes.SMALL_FLAME, true, x, y + 0.5, z, (rng.nextDouble() - 0.5) * 0.05, 0.04, (rng.nextDouble() - 0.5) * 0.05);
            }
        }

        int scatterFires = (int) (70 * fireFade);
        for (int i = 0; i < scatterFires; i++) {
            double a = rng.nextDouble() * Math.PI * 2.0;
            double r = ringR * Math.sqrt(rng.nextDouble());
            double x = cx + r * Math.cos(a);
            double z = cz + r * Math.sin(a);
            double y = cy + rng.nextDouble() * 2.0;
            double vy = 0.04 + rng.nextDouble() * 0.08;
            level.addAlwaysVisibleParticle(ParticleTypes.FLAME, true, x, y, z, 0, vy, 0);
            if ((i & 1) == 0) {
                level.addAlwaysVisibleParticle(DUST_EMBER, true, x, y, z, 0, vy * 0.5, 0);
            }
            if ((i & 2) == 0) {
                level.addAlwaysVisibleParticle(ParticleTypes.LAVA, true, x, y, z, 0, 0.06, 0);
            }
        }
    }

    public static float flashAlpha(float partialTick) {
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
        RegistryObject<SoundEvent> sound = pickSound(d);
        if (sound == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound.get(), 1.0f, 1.0f));
    }

    private static RegistryObject<SoundEvent> pickSound(double d) {
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
