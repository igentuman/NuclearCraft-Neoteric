package igentuman.nc.entity;

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.setup.registration.Entities.WASTELAND_PROJECTILE;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class EntityWastelandProjectile extends ThrowableProjectile {

    private static final float PROJECTILE_DAMAGE = 8.0F;
    private static final float RADIATION_AMOUNT = 1000000; // Amount of radiation to apply on hit
    private static final int PARTICLE_COUNT = 8; // Number of particles to spawn on impact

    public EntityWastelandProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public EntityWastelandProjectile(Level level, LivingEntity owner) {
        super(WASTELAND_PROJECTILE.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
        // No additional data needed
    }

    @Override
    public void tick() {
        super.tick();

        // Leave a trail of particles
        if (level().isClientSide) {
            Vec3 motion = getDeltaMovement().normalize().scale(0.1);
            for (int i = 0; i < 2; i++) {
                level().addParticle(
                    ParticleTypes.SMOKE,
                    getX() - motion.x + random.nextDouble() * 0.2 - 0.1,
                    getY() - motion.y + random.nextDouble() * 0.2 - 0.1,
                    getZ() - motion.z + random.nextDouble() * 0.2 - 0.1,
                    0, 0, 0
                );
            }
        }

        // Kill entity if it's been alive too long (10 seconds)
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            // Create impact effect
            this.level().broadcastEntityEvent(this, (byte)3);

            // Play sound
            this.playSound(SoundEvents.GENERIC_EXPLODE, 0.8F, 0.6F / (this.random.nextFloat() * 0.2F + 0.9F));

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity target = result.getEntity();
        if (!level().isClientSide && target instanceof LivingEntity livingTarget) {
            // Apply damage
            DamageSource damageSource = this.damageSources().mobProjectile(this,
                this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null);
            target.hurt(damageSource, PROJECTILE_DAMAGE);

            // Apply radiation if it's a player
            if (livingTarget.equals(this.level().getNearestPlayer(this, 64))) {
                livingTarget.getCapability(igentuman.nc.radiation.data.PlayerRadiationProvider.PLAYER_RADIATION)
                    .ifPresent(radiation -> {
                        long currentRadiation = radiation.getRadiation();
                        radiation.setRadiation((long) (currentRadiation + RADIATION_AMOUNT));
                    });
            }

            // Apply knockback
            double knockbackStrength = 0.5;
            Vec3 knockbackDir = livingTarget.position().subtract(this.position()).normalize();
            livingTarget.setDeltaMovement(
                livingTarget.getDeltaMovement().add(
                    knockbackDir.x * knockbackStrength,
                    0.1,
                    knockbackDir.z * knockbackStrength
                )
            );
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        // We could place a temporary wasteland block here if desired
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) { // Impact effect
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.8;
                double offsetY = (random.nextDouble() - 0.5) * 0.8;
                double offsetZ = (random.nextDouble() - 0.5) * 0.8;

                this.level().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    this.getX() + offsetX,
                    this.getY() + 0.2 + offsetY,
                    this.getZ() + offsetZ,
                    0, 0.07, 0
                );
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
