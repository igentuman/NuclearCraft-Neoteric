package igentuman.nc.entity;

import igentuman.nc.item.Q36Item;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.setup.registration.Entities.Q36_PULSE_PROJECTILE;

public class Q36PulseProjectile extends ThrowableProjectile {

    public Q36PulseProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public Q36PulseProjectile(Level level, LivingEntity owner) {
        super(Q36_PULSE_PROJECTILE.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 m = getDeltaMovement();
            for (int i = 0; i < 3; i++) {
                double t = i / 3.0D;
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() - m.x * t, getY() - m.y * t, getZ() - m.z * t,
                        0, 0, 0);
            }
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
            for (int i = 0; i < 2; i++) {
                double t = 0.2D + i * 0.35D;
                double ox = (random.nextDouble() - 0.5D) * 0.15D;
                double oy = (random.nextDouble() - 0.5D) * 0.15D;
                double oz = (random.nextDouble() - 0.5D) * 0.15D;
                level().addParticle(ParticleTypes.DRAGON_BREATH,
                        getX() - m.x * t + ox, getY() - m.y * t + oy, getZ() - m.z * t + oz,
                        -m.x * 0.05D, -m.y * 0.05D, -m.z * 0.05D);
            }
        }
        if (this.tickCount > 60) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, (byte) 3);
            playSound(SoundEvents.GENERIC_EXPLODE, 0.4F, 1.8F);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        Entity target = result.getEntity();
        if (target instanceof igentuman.nc.entity.anomaly.AnomalyEntity anomaly) {
            anomaly.onQ36Hit(getOwner());
        }
        LivingEntity ownerLiving = getOwner() instanceof LivingEntity le ? le : null;
        DamageSource src = damageSources().mobProjectile(this, ownerLiving);
        target.hurt(src, Q36Item.FireMode.PULSE.damage());
        if (target instanceof LivingEntity living) {
            living.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(rad ->
                    rad.setRadiation(rad.getRadiation() + Q36Item.FireMode.PULSE.radiation()));
            Vec3 dir = living.position().subtract(this.position()).normalize();
            living.setDeltaMovement(living.getDeltaMovement().add(dir.x * 0.3D, 0.1D, dir.z * 0.3D));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 10; i++) {
                double ox = (random.nextDouble() - 0.5) * 0.6;
                double oy = (random.nextDouble() - 0.5) * 0.6;
                double oz = (random.nextDouble() - 0.5) * 0.6;
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + ox, getY() + oy, getZ() + oz,
                        ox * 0.2, oy * 0.2, oz * 0.2);
            }
            level().addParticle(ParticleTypes.FLASH, getX(), getY(), getZ(), 0, 0, 0);
            for (int i = 0; i < 20; i++) {
                double ox = (random.nextDouble() - 0.5) * 1.2;
                double oy = (random.nextDouble() - 0.5) * 1.2;
                double oz = (random.nextDouble() - 0.5) * 1.2;
                level().addParticle(ParticleTypes.DRAGON_BREATH,
                        getX() + ox, getY() + oy, getZ() + oz,
                        ox * 0.08, oy * 0.08, oz * 0.08);
            }
            for (int i = 0; i < 8; i++) {
                double ox = (random.nextDouble() - 0.5) * 0.8;
                double oy = (random.nextDouble() - 0.5) * 0.8;
                double oz = (random.nextDouble() - 0.5) * 0.8;
                level().addParticle(ParticleTypes.SMOKE,
                        getX() + ox, getY() + oy, getZ() + oz,
                        ox * 0.05, 0.05, oz * 0.05);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
