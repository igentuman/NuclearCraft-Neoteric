package igentuman.nc.entity;

import igentuman.nc.setup.entries.Ghouls;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityBlockProjectile extends ThrowableProjectile {

    private static final float DAMAGE = 8.0F;
    private static final double RADIATION_HIT_SV = 0.1D;
    private static final int MAX_LIFETIME = 200;

    public EntityBlockProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityBlockProjectile(Level level, LivingEntity owner) {
        super(Ghouls.WASTELAND_PROJECTILE.get(), owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 movement = getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                double trail = 0.3D + i * 0.35D;
                level().addParticle(ParticleTypes.SMOKE,
                        getX() - movement.x * trail + random.nextDouble() * 0.2D - 0.1D,
                        getY() - movement.y * trail + random.nextDouble() * 0.2D - 0.1D,
                        getZ() - movement.z * trail + random.nextDouble() * 0.2D - 0.1D,
                        0.0D, 0.0D, 0.0D);
            }
        }
        if (tickCount > MAX_LIFETIME) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide || result.getEntity() == getOwner()) {
            return;
        }

        Entity target = result.getEntity();
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        if (target.hurt(damageSources().mobProjectile(this, owner), DAMAGE) && target instanceof LivingEntity livingTarget) {
            Vec3 knockback = livingTarget.position().subtract(position()).normalize().scale(0.5D);
            livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(knockback.x, 0.1D, knockback.z));
            if (livingTarget instanceof Player player) {
                EntityRadiationData radiation = player.getData(NRAttachments.ENTITY_RADIATION.get());
                radiation.addSv(RADIATION_HIT_SV);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (tickCount < 3 || level().isClientSide) {
            return;
        }
        level().broadcastEntityEvent(this, (byte) 3);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 0.55F + random.nextFloat() * 0.15F);
        discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != 3) {
            super.handleEntityEvent(id);
            return;
        }
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5D) * 0.8D;
            double offsetY = (random.nextDouble() - 0.5D) * 0.8D;
            double offsetZ = (random.nextDouble() - 0.5D) * 0.8D;
            level().addParticle(ParticleTypes.LARGE_SMOKE,
                    getX() + offsetX, getY() + 0.2D + offsetY, getZ() + offsetZ,
                    offsetX * 0.05D, 0.07D, offsetZ * 0.05D);
        }
    }
}
