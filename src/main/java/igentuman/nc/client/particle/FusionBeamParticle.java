package igentuman.nc.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.mojang.math.Axis.YP;

public class FusionBeamParticle extends Particle {

    private static final float RADIAN_45 = (float) Math.toRadians(45);
    private static final float RADIAN_90 = (float) Math.toRadians(90);
    private static final float RED = 1.0F;
    private static final float GREEN = 0.0F;
    private static final float BLUE = 0.5F;
    private static final float ALPHA = 0.11F;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final Direction direction;
    private final float halfLength;
    private final float quadSize;
    private final TextureAtlasSprite sprite;

    private FusionBeamParticle(ClientLevel world, Vec3 start, Vec3 end, Direction dir, float energyScale, TextureAtlasSprite sprite) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        lifetime = 5;
        quadSize = energyScale;
        halfLength = (float) (end.distanceTo(start) / 2);
        direction = dir;
        this.sprite = sprite;
        updateBoundingBox();
    }

    @Override
    public @NotNull ParticleRenderType getGroup() {
        return BeamParticleGroup.TYPE;
    }

    BeamParticleGroup.Beam extract(Camera camera, float partialTicks) {
        Vec3 view = camera.position();
        float newX = (float) (Mth.lerp(partialTicks, xo, x) - view.x());
        float newY = (float) (Mth.lerp(partialTicks, yo, y) - view.y());
        float newZ = (float) (Mth.lerp(partialTicks, zo, z) - view.z());
        Quaternionf quaternion = direction.getRotation();
        quaternion.mul(YP.rotation(RADIAN_45));
        Vector3f[] first = getResultVector(quaternion, newX, newY, newZ);
        Quaternionf quaternion2 = new Quaternionf(quaternion);
        quaternion2.mul(YP.rotation(RADIAN_90));
        Vector3f[] second = getResultVector(quaternion2, newX, newY, newZ);
        return new BeamParticleGroup.Beam(first, second,
                sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
                RED, GREEN, BLUE, ALPHA, FULL_BRIGHT);
    }

    boolean isVisible(net.minecraft.client.renderer.culling.Frustum frustum) {
        return frustum.isVisible(getBoundingBox());
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
        public Particle createParticle(FusionBeamParticleData data, @NotNull ClientLevel world, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, @NotNull RandomSource random) {
            Direction dir = data.direction();
            Vec3 start = new Vec3(x, y, z);
            Vec3 end = start.add(dir.getStepX() * data.distance(), dir.getStepY() * data.distance(), dir.getStepZ() * data.distance());
            return new FusionBeamParticle(world, start, end, dir, data.energyScale(), this.spriteSet.get(random));
        }
    }
}
