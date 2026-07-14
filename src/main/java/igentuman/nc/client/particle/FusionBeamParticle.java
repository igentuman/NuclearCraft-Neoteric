package igentuman.nc.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.mojang.math.Axis.YP;

/** Renders a fusion reactor energy beam as two crossed, directional translucent quads between two points. */
public class FusionBeamParticle extends TextureSheetParticle {

    private static final float RADIAN_45 = (float) Math.toRadians(45);
    private static final float RADIAN_90 = (float) Math.toRadians(90);

    private final Direction direction;
    private final float halfLength;

    private FusionBeamParticle(ClientLevel world, Vec3 start, Vec3 end, Direction dir, float energyScale) {
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
    public void render(@NotNull VertexConsumer vertexBuilder, Camera renderInfo, float partialTicks) {
        Vec3 view = renderInfo.getPosition();
        float newX = (float) (Mth.lerp(partialTicks, xo, x) - view.x());
        float newY = (float) (Mth.lerp(partialTicks, yo, y) - view.y());
        float newZ = (float) (Mth.lerp(partialTicks, zo, z) - view.z());
        float uMin = getU0();
        float uMax = getU1();
        float vMin = getV0();
        float vMax = getV1();
        Quaternionf quaternion = direction.getRotation();
        quaternion.mul(YP.rotation(RADIAN_45));
        drawComponent(vertexBuilder, getResultVector(quaternion, newX, newY, newZ), uMin, uMax, vMin, vMax);
        Quaternionf quaternion2 = new Quaternionf(quaternion);
        quaternion2.mul(YP.rotation(RADIAN_90));
        drawComponent(vertexBuilder, getResultVector(quaternion2, newX, newY, newZ), uMin, uMax, vMin, vMax);
    }

    private Vector3f[] getResultVector(Quaternionf quaternion, float newX, float newY, float newZ) {
        Vector3f[] resultVector = {
                new Vector3f(-quadSize, -halfLength, 0),
                new Vector3f(-quadSize, halfLength, 0),
                new Vector3f(quadSize, halfLength, 0),
                new Vector3f(quadSize, -halfLength, 0)
        };
        for (Vector3f vec : resultVector) {
            quaternion.transform(vec);
            vec.add(newX, newY, newZ);
        }
        return resultVector;
    }

    private void drawComponent(VertexConsumer vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax) {
        addVertex(vertexBuilder, resultVector[0], uMax, vMax);
        addVertex(vertexBuilder, resultVector[1], uMax, vMin);
        addVertex(vertexBuilder, resultVector[2], uMin, vMin);
        addVertex(vertexBuilder, resultVector[3], uMin, vMax);
        // Back faces
        addVertex(vertexBuilder, resultVector[1], uMax, vMin);
        addVertex(vertexBuilder, resultVector[0], uMax, vMax);
        addVertex(vertexBuilder, resultVector[3], uMin, vMax);
        addVertex(vertexBuilder, resultVector[2], uMin, vMin);
    }

    private void addVertex(VertexConsumer vertexBuilder, Vector3f pos, float u, float v) {
        vertexBuilder.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setUv2(240, 240);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
        setBoundingBox(switch (direction) {
            case DOWN, UP -> new AABB(x - halfDiameter, y - halfLength, z - halfDiameter, x + halfDiameter, y + halfLength, z + halfDiameter);
            case NORTH, SOUTH -> new AABB(x - halfDiameter, y - halfDiameter, z - halfLength, x + halfDiameter, y + halfDiameter, z + halfLength);
            case WEST, EAST -> new AABB(x - halfLength, y - halfDiameter, z - halfDiameter, x + halfLength, y + halfDiameter, z + halfDiameter);
        });
    }

    public static class Provider implements ParticleProvider<FusionBeamParticleData> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(FusionBeamParticleData data, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Direction dir = data.direction();
            Vec3 start = new Vec3(x, y, z);
            Vec3 end = start.add(dir.getStepX() * data.distance(), dir.getStepY() * data.distance(), dir.getStepZ() * data.distance());
            FusionBeamParticle beam = new FusionBeamParticle(world, start, end, dir, data.energyScale());
            beam.pickSprite(this.spriteSet);
            return beam;
        }
    }
}
