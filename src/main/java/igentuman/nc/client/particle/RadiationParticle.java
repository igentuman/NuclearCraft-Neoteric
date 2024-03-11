package igentuman.nc.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import org.antlr.v4.runtime.misc.NotNull;;

public class RadiationParticle extends SmokeParticle {

    private RadiationParticle(ClientWorld world, double posX, double posY, double posZ, double velX, double velY, double velZ, IAnimatedSprite sprite) {
        super(world, posX, posY, posZ, velX, velY, velZ, 1.0F, sprite);
        setColor(0.2F, 0.66F, 0.32F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 190 + (int) (20F * (1.0F - Minecraft.getInstance().options.gamma));
    }

    @Override
    public void render(@NotNull IVertexBuilder vertexBuilder, @NotNull ActiveRenderInfo renderInfo, float partialTicks) {
        if (age > 0) {
            super.render(vertexBuilder, renderInfo, partialTicks);
        }
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull BasicParticleType type, @NotNull ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RadiationParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
