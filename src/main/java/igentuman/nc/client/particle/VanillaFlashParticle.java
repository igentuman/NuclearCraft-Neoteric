package igentuman.nc.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class VanillaFlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected VanillaFlashParticle(ClientLevel world, double x, double y, double z, double scale, double transparency, SpriteSet sprites) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.lifetime = 4;
        this.quadSize = (float) scale;
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
        this.alpha = (float) (1.0 - Math.abs(transparency));
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
            this.setSpriteFromAge(this.sprites);
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
            double scale = vx;
            if (scale <= 0.0) {
                scale = 1.0;
            }
            double transparency = vy;
            return new VanillaFlashParticle(world, x, y, z, scale, transparency, this.spriteSet);
        }
    }
}
