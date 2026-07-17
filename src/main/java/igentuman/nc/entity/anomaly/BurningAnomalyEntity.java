package igentuman.nc.entity.anomaly;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Sets nearby living entities (and optionally flammable blocks) alight and periodically detonates a
 * small explosion. Counterplay: flood it — enough water source blocks within 3 blocks extinguish it.
 */
public class BurningAnomalyEntity extends AnomalyEntity {

    private int explosionTimer = -1;

    public BurningAnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    public AnomalyType getAnomalyType() {
        return AnomalyType.BURNING;
    }

    @Override
    protected double ambientParticleRadius() {
        return 2.0D;
    }

    @Override
    protected void applyEffects() {
        if (explosionTimer < 0) {
            explosionTimer = nextExplosionDelay();
        }
        if (onInterval(20)) {
            double radius = ANOMALY_CONFIG.BURN_RADIUS.get();
            Vec3 center = position();
            for (LivingEntity living : livingInRadius(radius)) {
                living.setSecondsOnFire(5);
                double dist = Math.sqrt(living.position().distanceToSqr(center));
                float frac = (float) Mth.clamp(dist / Math.max(radius, 0.001D), 0.0D, 1.0D);
                living.hurt(damageSources().inFire(), 6.0F - 4.0F * frac);
            }
            if (ANOMALY_CONFIG.BURN_BLOCK_IGNITE.get()) {
                igniteSomeBlocks(radius);
            }
        }
        if (ANOMALY_CONFIG.BURN_EXPLOSION.get()) {
            if (--explosionTimer <= 0) {
                explosionTimer = nextExplosionDelay();
                double power = ANOMALY_CONFIG.BURN_EXPLOSION_RADIUS.get();
                if (power > 0.0D) {
                    double angle = random.nextDouble() * Math.PI * 2.0;
                    double dist = random.nextDouble() * 5.0;
                    double ex = getX() + Math.cos(angle) * dist;
                    double ey = getY() + (random.nextDouble() * 4.0);
                    double ez = getZ() + Math.sin(angle) * dist;
                    level().explode(this, ex, ey, ez, (float) power, Level.ExplosionInteraction.MOB);
                }
            }
        }
    }

    private int nextExplosionDelay() {
        int min = ANOMALY_CONFIG.BURN_EXPLOSION_MIN_TICKS.get();
        int max = Math.max(min, ANOMALY_CONFIG.BURN_EXPLOSION_MAX_TICKS.get());
        return min + random.nextInt(max - min + 1);
    }

    private void igniteSomeBlocks(double radius) {
        int r = (int) radius;
        for (int attempt = 0; attempt < 3; attempt++) {
            int ox = random.nextInt(r * 2 + 1) - r;
            int oy = random.nextInt(5) - 2;
            int oz = random.nextInt(r * 2 + 1) - r;
            BlockPos pos = blockPosition().offset(ox, oy, oz);
            if (!level().isLoaded(pos)) {
                continue;
            }
            if (level().getBlockState(pos).isAir() && level().getBlockState(pos.below()).isSolidRender(level(), pos.below())) {
                level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            }
        }
    }

    @Override
    protected boolean checkCounterplay() {
        int required = ANOMALY_CONFIG.BURN_WATER_BLOCKS_TO_NEUTRALIZE.get();
        int found = 0;
        BlockPos center = blockPosition();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    FluidState fluid = level().getFluidState(pos);
                    if (fluid.isSource() && fluid.is(FluidTags.WATER)) {
                        if (++found >= required) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ExplosionTimer", explosionTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ExplosionTimer")) {
            explosionTimer = tag.getInt("ExplosionTimer");
        }
    }
}
