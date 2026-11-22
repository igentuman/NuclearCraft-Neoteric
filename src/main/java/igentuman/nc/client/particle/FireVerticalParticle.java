package igentuman.nc.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class FireVerticalParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    protected FireVerticalParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(world, x, y, z, vx, vy, vz, sprites.first());
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy <= 0 ? 0.08D : vy;
        this.zd = vz;
        this.lifetime = 64 + this.random.nextInt(12);
        this.quadSize = 1.5F + this.random.nextFloat() * 1.5F;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        setSpriteFromAge(sprites);
    }

    @Override
    protected SingleQuadParticle.@NotNull Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return 240;
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
            yd += 0.004D;
            xd *= 0.92D;
            zd *= 0.92D;
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double vx, double vy, double vz, @NotNull RandomSource random) {
            return new FireVerticalParticle(world, x, y, z, vx, vy, vz, this.spriteSet);
        }
    }
}
