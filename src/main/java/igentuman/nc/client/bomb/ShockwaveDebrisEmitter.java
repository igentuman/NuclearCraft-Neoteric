package igentuman.nc.client.bomb;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.config.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ShockwaveDebrisEmitter {

    public static final double SPAWN_DISTANCE = 128.0;
    public static final double RENDER_DISTANCE = 384.0;
    public static final int SPAWNS_PER_BOMB = 6;
    public static final int SPAWNS_GLOBAL = 10;
    public static final int MAX_AGE = 300;
    private static final float SPAWN_DENSITY = 0.008f;
    private static final float MIN_WAVE_SPEED = 3f;
    private static final float MIN_LAG = 0.08f;
    private static final float MAX_LAG = 0.16f;
    private static final float MIN_PITCH = 35f * Mth.DEG_TO_RAD;
    private static final float MAX_PITCH = 55f * Mth.DEG_TO_RAD;
    private static final float DUST_CLEARANCE = 0.6f;
    private static final float MIN_APEX = 0.7f;
    private static final float MAX_APEX = 1.2f;
    private static final float GRAVITY = 0.04f;
    private static final float MIN_SIZE = 0.6f;
    private static final float MAX_SIZE = 1.5f;
    private static final float MIN_SPIN = 0.12f;
    private static final float MAX_SPIN = 0.45f;

    private static final List<Piece> PIECES = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static final class Piece {
        final ActiveBomb bomb;
        final BlockState state;
        final float cos;
        final float sin;
        final float lag;
        final float size;
        final float sx;
        final float sy;
        final float sz;
        final Vector3f axis;
        final float spin;
        float radius;
        float prevRadius;
        double y;
        double prevY;
        double ground;
        float vy;
        float rot;
        float prevRot;
        int age;

        Piece(ActiveBomb bomb, BlockState state, float angle, float radius, double ground, float lag, float size,
              float vy, RandomSource rng) {
            this.bomb = bomb;
            this.state = state;
            this.cos = Mth.cos(angle);
            this.sin = Mth.sin(angle);
            this.radius = this.prevRadius = radius;
            this.ground = ground;
            this.y = this.prevY = ground + size * 0.5;
            this.lag = lag;
            this.size = size;
            this.sx = size * (0.65f + 0.35f * rng.nextFloat());
            this.sy = size * (0.65f + 0.35f * rng.nextFloat());
            this.sz = size * (0.65f + 0.35f * rng.nextFloat());
            this.vy = vy;
            this.axis = new Vector3f(rng.nextFloat() - 0.5f, rng.nextFloat() - 0.5f, rng.nextFloat() - 0.5f);
            if (axis.lengthSquared() < 1.0e-4f) axis.set(0f, 1f, 0f);
            axis.normalize();
            float smallness = 1f - (size - MIN_SIZE) / (MAX_SIZE - MIN_SIZE);
            this.spin = Mth.lerp(rng.nextFloat(), MIN_SPIN, MAX_SPIN) * (0.6f + 0.8f * smallness);
            this.rot = this.prevRot = rng.nextFloat() * Mth.TWO_PI;
        }

        boolean step() {
            if (++age > MAX_AGE) return false;
            float waveSpeed = NukeTimeline.groundSpeed(bomb.ageSeconds(0f), bomb.visual.r) / 20f;
            prevRadius = radius;
            prevY = y;
            prevRot = rot;
            radius += waveSpeed * (1f - lag);
            vy -= GRAVITY;
            y += vy;
            rot += spin;
            float h = bomb.surface.heightOffset(cos * radius, sin * radius);
            if (!Float.isNaN(h)) ground = bomb.epicenter.getY() + 0.5 + h;
            return vy >= 0f || y - size * 0.5 > ground;
        }

        double x(float partialTick) {
            return bomb.epicenter.getX() + 0.5 + cos * Mth.lerp(partialTick, prevRadius, radius);
        }

        double z(float partialTick) {
            return bomb.epicenter.getZ() + 0.5 + sin * Mth.lerp(partialTick, prevRadius, radius);
        }
    }

    private ShockwaveDebrisEmitter() {}

    public static void tick(List<ActiveBomb> bombs, ClientLevel level, Vec3 camera) {
        Iterator<Piece> it = PIECES.iterator();
        while (it.hasNext()) {
            Piece p = it.next();
            if (!bombs.contains(p.bomb) || !p.step()) it.remove();
        }

        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        int budget = Math.min(SPAWNS_GLOBAL, Client.effectiveNukeQuality().shockDebrisLiveCap - PIECES.size());
        if (status == ParticleStatus.DECREASED) budget /= 2;
        if (budget <= 0) return;

        int eligible = 0;
        for (ActiveBomb bomb : bombs) {
            if (halfArc(bomb, camera) > 0f) eligible++;
        }
        if (eligible == 0) return;

        int share = Math.min(SPAWNS_PER_BOMB, Mth.positiveCeilDiv(budget, eligible));
        for (ActiveBomb bomb : bombs) {
            if (budget <= 0) break;
            float halfArc = halfArc(bomb, camera);
            if (halfArc <= 0f) continue;
            budget -= emit(level, bomb, halfArc, Math.min(share, budget), camera);
        }
    }

    private static float strength(ActiveBomb bomb) {
        NukeVisualState v = bomb.visual;
        if (v.groundIntensity <= 0f || v.groundRadius <= 0f) return 0f;
        float speed = NukeTimeline.groundSpeed(bomb.ageSeconds(0f), v.r);
        return v.groundIntensity * Mth.clamp((speed - MIN_WAVE_SPEED) / MIN_WAVE_SPEED, 0f, 1f);
    }

    private static float halfArc(ActiveBomb bomb, Vec3 camera) {
        if (strength(bomb) <= 0f) return 0f;
        double dy = camera.y - bomb.epicenter.getY();
        double window = SPAWN_DISTANCE * SPAWN_DISTANCE - dy * dy;
        if (window <= 0.0) return 0f;
        double dx = camera.x - (bomb.epicenter.getX() + 0.5);
        double dz = camera.z - (bomb.epicenter.getZ() + 0.5);
        double d = Math.sqrt(dx * dx + dz * dz);
        double r = bomb.visual.groundRadius;
        if (d < 1.0e-3) return r * r <= window ? Mth.PI : 0f;
        double c = (r * r + d * d - window) / (2.0 * r * d);
        if (c >= 1.0) return 0f;
        return (float) Math.acos(Math.max(c, -1.0));
    }

    private static int emit(ClientLevel level, ActiveBomb bomb, float halfArc, int max, Vec3 camera) {
        NukeVisualState v = bomb.visual;
        float radius = v.groundRadius;
        float expected = 2f * halfArc * radius * SPAWN_DENSITY * strength(bomb);
        int count = Math.min(max, Mth.floor(expected + RANDOM.nextFloat()));
        if (count <= 0) return 0;

        double cx = bomb.epicenter.getX() + 0.5;
        double cz = bomb.epicenter.getZ() + 0.5;
        float facing = (float) Mth.atan2(camera.z - cz, camera.x - cx);
        float waveSpeed = NukeTimeline.groundSpeed(bomb.ageSeconds(0f), v.r) / 20f;
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            float angle = facing + (RANDOM.nextFloat() * 2f - 1f) * halfArc;
            float r = radius * (0.96f + 0.04f * RANDOM.nextFloat());
            float h = bomb.surface.heightOffset(Mth.cos(angle) * r, Mth.sin(angle) * r);
            if (Float.isNaN(h)) continue;
            double ground = bomb.epicenter.getY() + 0.5 + h;
            BlockPos below = BlockPos.containing(cx + Mth.cos(angle) * r, ground - 0.5, cz + Mth.sin(angle) * r);
            if (!level.getFluidState(below).isEmpty()) continue;
            BlockState state = DebrisPalette.pick(level.getBiome(below), RANDOM);

            float sizeT = RANDOM.nextFloat();
            sizeT *= sizeT;
            float size = Mth.lerp(sizeT, MIN_SIZE, MAX_SIZE);
            float lag = Mth.lerp(RANDOM.nextFloat(), MIN_LAG, MAX_LAG);
            float pitch = Mth.lerp(RANDOM.nextFloat(), MIN_PITCH, MAX_PITCH);
            float apex = Mth.lerp(RANDOM.nextFloat(), MIN_APEX, MAX_APEX) * v.r;
            float clear = Math.min(DUST_CLEARANCE * v.r, apex);
            float vy = Mth.clamp(waveSpeed * (1f - lag) * (float) Math.tan(pitch),
                    Mth.sqrt(2f * GRAVITY * clear), Mth.sqrt(2f * GRAVITY * apex));
            PIECES.add(new Piece(bomb, state, angle, r, ground, lag, size, vy, RANDOM));
            spawned++;
        }
        return spawned;
    }

    public static void render(RenderLevelStageEvent event) {
        if (PIECES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Frustum frustum = event.getFrustum();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        Quaternionf rotation = new Quaternionf();
        for (Piece p : PIECES) {
            double x = p.x(partialTick);
            double y = Mth.lerp(partialTick, p.prevY, p.y);
            double z = p.z(partialTick);
            if (camera.distanceToSqr(x, y, z) > RENDER_DISTANCE * RENDER_DISTANCE) continue;
            double half = p.size;
            if (frustum != null && !frustum.isVisible(new AABB(x - half, y - half, z - half, x + half, y + half, z + half))) {
                continue;
            }
            pose.pushPose();
            pose.translate(x - camera.x, y - camera.y, z - camera.z);
            pose.mulPose(rotation.identity().rotateAxis(Mth.lerp(partialTick, p.prevRot, p.rot), p.axis));
            pose.scale(p.sx, p.sy, p.sz);
            pose.translate(-0.5f, -0.5f, -0.5f);
            int light = LevelRenderer.getLightColor(level, BlockPos.containing(x, y, z));
            dispatcher.renderSingleBlock(p.state, pose, buffers, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            pose.popPose();
        }
    }

    public static int liveCount() {
        return PIECES.size();
    }

    public static void clear() {
        PIECES.clear();
    }
}
