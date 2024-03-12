package igentuman.nc.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import igentuman.nc.util.math.Pos3D;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.antlr.v4.runtime.misc.NotNull;
import org.lwjgl.opengl.GL11;;import javax.annotation.Nonnull;

public class FusionBeamParticle extends SpriteTexturedParticle {

    private static final float RADIAN_45 = (float) Math.toRadians(45);
    private static final float RADIAN_90 = (float) Math.toRadians(90);

    private final Direction direction;
    private final float halfLength;

    private FusionBeamParticle(ClientWorld world, Vector3d start, Vector3d end, Direction dir, float energyScale) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        lifetime = 5;
        rCol = 1;
        gCol = 0;
        bCol = 0.5f;
        alpha = 0.11F;
        quadSize = energyScale;
        halfLength = (float) (end.distanceTo(start) / 2);
        direction = dir;
        updateBoundingBox();
    }

    @Override
    public void render(@Nonnull IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d view = renderInfo.getPosition();
        float newX = (float) (MathHelper.lerp(partialTicks, xo, x) - view.x());
        float newY = (float) (MathHelper.lerp(partialTicks, yo, y) - view.y());
        float newZ = (float) (MathHelper.lerp(partialTicks, zo, z) - view.z());
        float uMin = getU0();
        float uMax = getU1();
        float vMin = getV0();
        float vMax = getV1();
        Quaternion quaternion = direction.getRotation();
        quaternion.mul(Vector3f.YP.rotation(RADIAN_45));
        drawComponent(vertexBuilder, getResultVector(quaternion, newX, newY, newZ), uMin, uMax, vMin, vMax);
        Quaternion quaternion2 = new Quaternion(quaternion);
        quaternion2.mul(Vector3f.YP.rotation(RADIAN_90));
        drawComponent(vertexBuilder, getResultVector(quaternion2, newX, newY, newZ), uMin, uMax, vMin, vMax);
    }

    private Vector3f[] getResultVector(Quaternion quaternion, float newX, float newY, float newZ) {
        Vector3f[] resultVector = {
              new Vector3f(-quadSize, -halfLength, 0),
              new Vector3f(-quadSize, halfLength, 0),
              new Vector3f(quadSize, halfLength, 0),
              new Vector3f(quadSize, -halfLength, 0)
        };
        for (Vector3f vec : resultVector) {
            vec.transform(quaternion);
            vec.add(newX, newY, newZ);
        }
        return resultVector;
    }

    private void drawComponent(IVertexBuilder vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax) {
        vertexBuilder.vertex(resultVector[0].x(), resultVector[0].y(), resultVector[0].z()).uv(uMax, vMax).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
        vertexBuilder.vertex(resultVector[1].x(), resultVector[1].y(), resultVector[1].z()).uv(uMax, vMin).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
        vertexBuilder.vertex(resultVector[2].x(), resultVector[2].y(), resultVector[2].z()).uv(uMin, vMin).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
        vertexBuilder.vertex(resultVector[3].x(), resultVector[3].y(), resultVector[3].z()).uv(uMin, vMax).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
    }

    private static final IParticleRenderType FUSION_BEAM_TYPE = new IParticleRenderType() {
        @Override
        public void begin(BufferBuilder buffer, TextureManager manager) {
            //Copy of PARTICLE_SHEET_TRANSLUCENT but with cull disabled
            RenderSystem.depthMask(true);
            manager.bind(AtlasTexture.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            RenderSystem.disableCull();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
        }

        @Override
        public void end(Tessellator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "MEK_LASER_PARTICLE_TYPE";
        }
    };
    @NotNull
    @Override
    public IParticleRenderType getRenderType() {
        return FUSION_BEAM_TYPE;
    }

    @Override
    protected void setSize(float particleWidth, float particleHeight) {
        if (particleWidth != this.bbWidth || particleHeight != this.bbHeight) {
            this.bbWidth = particleWidth;
            this.bbHeight = particleHeight;
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (direction != null) {
            updateBoundingBox();
        }
    }

    private void updateBoundingBox() {
        float halfDiameter = quadSize / 2;
        switch (direction) {
            case DOWN:
            case UP:
                setBoundingBox(new AxisAlignedBB(x - halfDiameter, y - halfLength, z - halfDiameter, x + halfDiameter, y + halfLength, z + halfDiameter));
                break;
            case NORTH:
            case SOUTH:
                setBoundingBox(new AxisAlignedBB(x - halfDiameter, y - halfDiameter, z - halfLength, x + halfDiameter, y + halfDiameter, z + halfLength));
                break;
            case WEST:
            case EAST:
                setBoundingBox(new AxisAlignedBB(x - halfLength, y - halfDiameter, z - halfDiameter, x + halfLength, y + halfDiameter, z + halfDiameter));
                break;
        }
    }

    public static class Factory implements IParticleFactory<FusionBeamParticleData> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public FusionBeamParticle createParticle(FusionBeamParticleData data, @NotNull ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Pos3D start = new Pos3D(x, y, z);
            Pos3D end = start.translate(data.direction, data.distance);
            FusionBeamParticle beam = new FusionBeamParticle(world, start, end, data.direction, data.energyScale);
            beam.pickSprite(this.spriteSet);
            return beam;
        }
    }
}