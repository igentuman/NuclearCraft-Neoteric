package igentuman.nc.entity.anomaly;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Pulls entities toward its centre, occasionally launches caught creatures upward, and slowly absorbs
 * surrounding blocks. When enough blocks have been absorbed it collapses in an explosion — the only
 * way it is removed (its "counterplay" is to feed it until it self-destructs, or simply leave the area).
 */
public class GravitationalAnomalyEntity extends AnomalyEntity {

    protected static final EntityDataAccessor<Integer> DATA_ABSORBED =
            SynchedEntityData.defineId(GravitationalAnomalyEntity.class, EntityDataSerializers.INT);

    public GravitationalAnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    public AnomalyType getAnomalyType() {
        return AnomalyType.GRAVITATIONAL;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ABSORBED, 0);
    }

    public int getAbsorbedBlocks() {
        return this.entityData.get(DATA_ABSORBED);
    }

    public void setAbsorbedBlocks(int count) {
        this.entityData.set(DATA_ABSORBED, count);
    }

    /**
     * Growth multiplier from absorbed mass: 1.0 when empty, up to {@code 1 + GRAV_MASS_SCALE} at the
     * collapse threshold. Synced via {@link #DATA_ABSORBED}, so client render/shader can read it too.
     */
    public double massFactor() {
        int threshold = ANOMALY_CONFIG.GRAV_ABSORB_THRESHOLD.get();
        double frac = threshold <= 0 ? 0.0D : Math.min(1.0D, (double) getAbsorbedBlocks() / threshold);
        return 1.0D + ANOMALY_CONFIG.GRAV_MASS_SCALE.get() * frac;
    }

    @Override
    protected double ambientParticleRadius() {
        return ANOMALY_CONFIG.GRAV_RADIUS.get() * massFactor();
    }

    @Override
    protected void applyEffects() {
        double mass = massFactor();
        double radius = ANOMALY_CONFIG.GRAV_RADIUS.get() * mass;
        double pullStrength = ANOMALY_CONFIG.GRAV_PULL.get() * mass;
        double launchChance = ANOMALY_CONFIG.GRAV_LAUNCH_CHANCE.get();
        double launchPower = ANOMALY_CONFIG.GRAV_LAUNCH_POWER.get();
        Vec3 center = position();
        double r2 = radius * radius;

        for (Entity entity : level.getEntities(this, effectBox(radius), e -> !(e instanceof AnomalyEntity) && e.isAlive())) {
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().getItem() instanceof BlockItem) {
                    setAbsorbedBlocks(getAbsorbedBlocks() + 1);
                }
                itemEntity.discard();
                continue;
            }
            Vec3 delta = center.subtract(entity.position());
            double distSqr = delta.lengthSqr();
            if (distSqr > r2 || distSqr < 1.0E-2D) {
                continue;
            }
            double dist = Math.sqrt(distSqr);
            double scale = pullStrength * (1.0D - Math.min(1.0D, dist / radius));
            Vec3 pull = delta.scale(1.0D / dist).scale(scale);
            entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
            if (entity instanceof LivingEntity living) {
                if (random.nextDouble() < launchChance) {
                    Vec3 dm = entity.getDeltaMovement();
                    entity.setDeltaMovement(dm.x, launchPower, dm.z);
                    entity.fallDistance = 0.0F;
                    entity.hurtMarked = true;
                }
                if (dist <= 2.0D) {
                    living.hurt(DamageSource.MAGIC, 5.0F);
                }
            }
            // Player movement is client-authoritative; flag so the server pushes a velocity packet.
            if (entity instanceof Player) {
                entity.hurtMarked = true;
            }
        }

        if (ANOMALY_CONFIG.GRAV_BLOCK_ABSORB.get()
                && getAbsorbedBlocks() < ANOMALY_CONFIG.GRAV_ABSORB_THRESHOLD.get()
                && onInterval(ANOMALY_CONFIG.GRAV_ABSORB_INTERVAL.get())) {
            absorbBlock(radius/2);
        }
    }

    private void absorbBlock(double radius) {
        int r = Math.max(1, (int) radius);
        for (int attempt = 0; attempt < 6; attempt++) {
            int ox = random.nextInt(r * 2 + 1) - r;
            int oy = random.nextInt(r * 2 + 1) - r;
            int oz = random.nextInt(r * 2 + 1) - r;
            BlockPos pos = blockPosition().offset(ox, oy, oz);
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(Blocks.BEDROCK)) {
                continue;
            }
            if (state.getDestroySpeed(level, pos) < 0.0F) {
                continue;
            }
            if (level.getBlockEntity(pos) != null) {
                continue;
            }
            level.removeBlock(pos, false);
            setAbsorbedBlocks(getAbsorbedBlocks() + 1);
            return;
        }
    }

    @Override
    protected boolean checkCounterplay() {
        return getAbsorbedBlocks() >= ANOMALY_CONFIG.GRAV_ABSORB_THRESHOLD.get();
    }

    @Override
    protected void onCounterplayResolved() {
        if (level.isClientSide) {
            return;
        }
        if (ANOMALY_CONFIG.GRAV_EXPLOSION.get()) {
            double power = ANOMALY_CONFIG.GRAV_EXPLOSION_POWER.get();
            if (power > 0.0D) {
                level.explode(this, getX(), getY(), getZ(), (float) power, Explosion.BlockInteraction.DESTROY);
            }
        }
        super.onCounterplayResolved();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AbsorbedBlocks", getAbsorbedBlocks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AbsorbedBlocks")) {
            setAbsorbedBlocks(tag.getInt("AbsorbedBlocks"));
        }
    }
}
