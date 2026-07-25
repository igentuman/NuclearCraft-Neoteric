package igentuman.nc.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Q36EnergyFlash extends Entity {

    public static final int LIFETIME_TICKS = 16;

    public Q36EnergyFlash(EntityType<? extends Q36EnergyFlash> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= LIFETIME_TICKS) {
            discard();
        }
        if (level().isClientSide) {
            spawnSparkles();
        }
    }

    private void spawnSparkles() {
        float decay = 1.0F - (float) tickCount / LIFETIME_TICKS;
        int count = (int) (10 * decay) + 1;
        float speed = 1.0F + decay * 2.5F;

        for (int i = 0; i < count; i++) {
            double vx = (random.nextDouble() - 0.5) * speed;
            double vy = (random.nextDouble() - 0.5) * speed;
            double vz = (random.nextDouble() - 0.5) * speed;
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + (random.nextDouble() - 0.5D)*2D, getY() + (random.nextDouble() - 0.5D)*2D, getZ() + (random.nextDouble() - 0.5D)*2D, vx, vy, vz);
        }
        if (tickCount == 0) {
            level().addParticle(ParticleTypes.FLASH, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        return distanceSq < 4096.0D;
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }
}
