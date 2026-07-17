package igentuman.nc.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import igentuman.nc.setup.registration.NcParticleTypes;

@OnlyIn(Dist.CLIENT)
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
            this.level.addParticle(NcParticleTypes.EXPLOSION.get(), d0, d1, d2, (double)((float)this.life / (float)this.lifeTime), 0.0D, 0.0D);
        }
        ++this.life;
        if (this.life == this.lifeTime) {
            this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Factory(SpriteSet pSprites) {

        }
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ExplosionSeedParticle(world, x, y, z);
        }
    }
}
