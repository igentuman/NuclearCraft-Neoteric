package igentuman.nc.client.particle;

import igentuman.nc.setup.NcParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class ExplosionSeedParticle extends NoRenderParticle {

    private int life;
    private final int lifeTime = 8;

    private ExplosionSeedParticle(ClientLevel world, double posX, double posY, double posZ) {
        super(world, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void tick() {
        for (int i = 0; i < 6; ++i) {
            double d0 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            double d1 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            double d2 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            this.level.addParticle(NcParticles.EXPLOSION.get(), d0, d1, d2, (float) this.life / (float) this.lifeTime, 0.0D, 0.0D);
        }
        ++this.life;
        if (this.life == this.lifeTime) {
            this.remove();
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        public Factory(SpriteSet sprites) {
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
            return new ExplosionSeedParticle(world, x, y, z);
        }
    }
}
