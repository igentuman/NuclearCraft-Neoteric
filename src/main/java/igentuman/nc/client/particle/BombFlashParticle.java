package igentuman.nc.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class BombFlashParticle extends TextureSheetParticle {

    private final float yield;
    private final float maxR;
    private final float rotSpeed;

    protected BombFlashParticle(ClientLevel world, double x, double y, double z, double yield, int lifetime, SpriteSet sprites) {
        super(world, x, y, z, 0, 0, 0);
        this.yield = (float) yield;
        this.lifetime = lifetime;
        this.maxR = Math.max(9f, this.yield * 45f);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.roll = this.random.nextFloat() * ((float)Math.PI * 2F);
        this.oRoll = this.roll;
        this.rotSpeed = (this.random.nextBoolean() ? 1f : -1f) * (0.01F + this.random.nextFloat() * 0.02F);
        this.setSpriteFromAge(sprites);
        updateScaleAndAlpha(0.0f);
    }

    private void updateScaleAndAlpha(float partialTick) {
        float t = (this.age + partialTick) / (float) this.lifetime;
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;

        float t_peak = 0.15f;
        float sizeFactor;
        float alphaFactor;

        if (t < t_peak) {
            float progress = t / t_peak;
            sizeFactor = (float) Math.sin(progress * (Math.PI / 2.0));
            alphaFactor = progress;
        } else {
            float progress = (t - t_peak) / (1.0f - t_peak);
            sizeFactor = 1.0f + 0.1f * progress;
            alphaFactor = 1.0f - progress;
        }

        this.quadSize = this.maxR * sizeFactor;
        this.alpha = Math.max(0.0f, Math.min(0.8f, alphaFactor));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.oRoll = this.roll;
            this.roll += this.rotSpeed;
            updateScaleAndAlpha(0.0f);
        }
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        updateScaleAndAlpha(partialTicks);
        super.render(buffer, camera, partialTicks);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
            int lifetime = (int) vy;
            if (lifetime <= 0) {
                lifetime = 30;
            }
            return new BombFlashParticle(world, x, y, z, vx, lifetime, this.spriteSet);
        }
    }
}
