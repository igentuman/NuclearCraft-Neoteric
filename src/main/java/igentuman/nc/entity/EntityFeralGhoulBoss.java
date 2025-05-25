package igentuman.nc.entity;

import igentuman.nc.client.model.ModelFeralGhoulBoss;
import igentuman.nc.client.renderer.FeralGhoulBossRenderer;
import igentuman.nc.entity.goal.RadiationBurstGoal;
import igentuman.nc.entity.goal.RangedAttackGoal;
import igentuman.nc.entity.goal.SlamAttackGoal;
import igentuman.nc.entity.goal.SummonGhoulsGoal;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL_BOSS;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class EntityFeralGhoulBoss extends EntityFeralGhoul {

    // Boss parameters
    public static final int BOSS_RADIATION_AMOUNT = 20000000;
    public static final float SLAM_ATTACK_RANGE = 8.0F;
    public static final int SLAM_ATTACK_COOLDOWN = 80; // 4 seconds
    public static final int RADIATION_BURST_COOLDOWN = 160; // 8 seconds
    public static final float RADIATION_BURST_RANGE = 8.0F;
    public static final int RADIATION_BURST_AMOUNT = 10000000;
    public static final int SUMMON_COOLDOWN = 480; // 24 seconds
    public static final float SUMMON_RANGE = 8.0F;
    public static final int MAX_SUMMONS = 3;
    // Ranged attack parameters
    public static final int RANGED_ATTACK_COOLDOWN = 120; // 6 seconds
    public static final float MIN_RANGED_ATTACK_DISTANCE = 16.0F;
    public static final float MAX_RANGED_ATTACK_DISTANCE = 48.0F;

    public int slamAttackCooldownRemaining = 0;
    public int radiationBurstCooldownRemaining = 0;
    public int summonCooldownRemaining = 0;
    public int rangedAttackCooldownRemaining = 0;
    public final Random random = new Random();
    public boolean isExecutingAttack = false;

    // Boss bar
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.nuclearcraft.feral_ghoul_boss"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public EntityFeralGhoulBoss(Level pLevel) {
        this(FERAL_GHOUL_BOSS.get(), pLevel);
    }

    @SuppressWarnings("unchecked")
    public EntityFeralGhoulBoss(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
        this.refreshDimensions(); // Make sure dimensions are updated when the entity is created
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new SlamAttackGoal(this));
        this.goalSelector.addGoal(2, new RadiationBurstGoal(this));
        this.goalSelector.addGoal(3, new SummonGhoulsGoal(this));
        this.goalSelector.addGoal(4, new RangedAttackGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.40F)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.FOLLOW_RANGE, 70.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 7.0D);
    }

    public static boolean checkFeralGhoulBossSpawnRules(EntityType<EntityFeralGhoulBoss> entityType,
                                                    ServerLevelAccessor level,
                                                    MobSpawnType spawnType,
                                                    BlockPos pos,
                                                    net.minecraft.util.RandomSource random) {

        if (!Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }

        return level.getBiome(pos).is(WASTELAND);
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        boolean attackResult = super.doHurtTarget(pEntity);

        if (attackResult && pEntity instanceof Player player) {
            player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                long currentRadiation = radiation.getRadiation();
                radiation.setRadiation(currentRadiation + BOSS_RADIATION_AMOUNT);
            });

            Vec3 knockbackDirection = pEntity.position().subtract(this.position()).normalize().scale(2.0);
            pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(knockbackDirection));

            ((LivingEntity) pEntity).addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }

        return attackResult;
    }

    @Override
    public void tick() {
        super.tick();

        // Update boss bar
        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // Decrease cooldowns
        if (slamAttackCooldownRemaining > 0) {
            slamAttackCooldownRemaining--;
        }

        if (radiationBurstCooldownRemaining > 0) {
            radiationBurstCooldownRemaining--;
        }

        if (summonCooldownRemaining > 0) {
            summonCooldownRemaining--;
        }

        if (rangedAttackCooldownRemaining > 0) {
            rangedAttackCooldownRemaining--;
        }
    }

    /**
     * Execute a radiation burst attack that affects players in an area
     */
    public void executeRadiationBurst() {
        if (radiationBurstCooldownRemaining <= 0 && !isExecutingAttack) {
            isExecutingAttack = true;

            // Visual and sound effects
            this.level().broadcastEntityEvent(this, (byte) 5); // Custom entity event for clients to show animation
            this.playSound(SoundEvents.WITHER_SHOOT, 0.7F, 0.6F);

            // Apply radiation to nearby players
            AABB radiationBox = this.getBoundingBox().inflate(RADIATION_BURST_RANGE);
            List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, radiationBox);

            // Spawn radiation particles throughout the affected area
            if (this.level() instanceof ServerLevel serverLevel) {
                // Create particles in a circular pattern around the boss
                double entityY = this.getY() + 0.5;
                Vec3 center = this.position();

                // Create a dome of particles
                for (int i = 0; i < 100; i++) { // Number of particles to spawn
                    double angle = random.nextDouble() * Math.PI * 2; // Random angle
                    double radius = random.nextDouble() * RADIATION_BURST_RANGE; // Random radius within burst range
                    double height = random.nextDouble() * 2.5; // Random height up to 2.5 blocks

                    // Calculate position for this particle
                    double xPos = center.x + radius * Math.cos(angle);
                    double yPos = entityY + height;
                    double zPos = center.z + radius * Math.sin(angle);

                    // Calculate velocity - particles should move outward and upward
                    double xVel = (xPos - center.x) * 0.05;
                    double yVel = random.nextDouble() * 0.05;
                    double zVel = (zPos - center.z) * 0.05;

                    // Spawn the particle
                    serverLevel.sendParticles(
                            NcParticleTypes.RADIATION.get(),
                            xPos, yPos, zPos,
                            1, // count - just one particle per position
                            xVel, yVel, zVel,
                            0.02 // speed
                    );
                }

                // Create an expanding ring of particles at ground level
                for (int ring = 0; ring < 3; ring++) {
                    double ringRadius = (ring + 1) * (RADIATION_BURST_RANGE / 3);
                    int particlesInRing = 20 + (ring * 10);

                    for (int i = 0; i < particlesInRing; i++) {
                        double angle = (Math.PI * 2 * i) / particlesInRing;
                        double xPos = center.x + ringRadius * Math.cos(angle);
                        double yPos = center.y + 0.1; // Just above ground level
                        double zPos = center.z + ringRadius * Math.sin(angle);

                        serverLevel.sendParticles(
                                NcParticleTypes.RADIATION.get(),
                                xPos, yPos, zPos,
                                1,
                                0, 0.05, 0,
                                0.01
                        );
                    }
                }
            }

            for (Player player : nearbyPlayers) {
                player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                    long currentRadiation = radiation.getRadiation();
                    radiation.setRadiation(currentRadiation + RADIATION_BURST_AMOUNT);
                });

                // Apply negative effects
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 140, 0));
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            }

            // Set cooldown
            radiationBurstCooldownRemaining = RADIATION_BURST_COOLDOWN;
            isExecutingAttack = false;
        }
    }

    /**
     * Execute summon attack that spawns feral ghouls near the target
     */
    public void executeSummonAttack() {
        if (summonCooldownRemaining <= 0 && !isExecutingAttack && !this.level().isClientSide()) {
            isExecutingAttack = true;

            // Visual and sound effects
            this.level().broadcastEntityEvent(this, (byte) 6); // Custom entity event for clients to show animation
            this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 1.2F);

            // Find potential target near which to spawn ghouls
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                // Count existing feral ghouls
                int existingGhouls = 0;
                List<EntityFeralGhoul> nearbyGhouls = this.level().getEntitiesOfClass(
                        EntityFeralGhoul.class,
                        this.getBoundingBox().inflate(20.0),
                        ghoul -> !(ghoul instanceof EntityFeralGhoulBoss)
                );
                existingGhouls = nearbyGhouls.size();

                // Calculate number of ghouls to spawn
                int spawnCount = Math.min(MAX_SUMMONS, MAX_SUMMONS - existingGhouls);

                if (spawnCount > 0) {
                    // Find positions around the target
                    for (int i = 0; i < spawnCount; i++) {
                        // Calculate a random position within range of the target
                        double angle = random.nextDouble() * Math.PI * 2;
                        double distance = 2.0 + random.nextDouble() * (SUMMON_RANGE - 2.0);
                        double xOffset = Math.sin(angle) * distance;
                        double zOffset = Math.cos(angle) * distance;

                        BlockPos spawnPos = new BlockPos(
                                (int) (target.getX() + xOffset),
                                (int) target.getY(),
                                (int) (target.getZ() + zOffset)
                        );

                        // Check if the position is valid (has space and solid ground)
                        if (this.level().getBlockState(spawnPos).isAir() &&
                                this.level().getBlockState(spawnPos.below()).isSolid()) {

                            // Spawn the ghoul
                            EntityFeralGhoul ghoul = new EntityFeralGhoul(this.level());
                            ghoul.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                            ghoul.setTarget(target);

                            // Add visual effect for spawning
                            this.level().addParticle(
                                    net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                                    spawnPos.getX() + 0.5,
                                    spawnPos.getY() + 0.5,
                                    spawnPos.getZ() + 0.5,
                                    0, 0.1, 0
                            );

                            this.level().addFreshEntity(ghoul);
                        }
                    }
                }
            }

            // Set cooldown
            summonCooldownRemaining = SUMMON_COOLDOWN;
            isExecutingAttack = false;
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    /**
     * Execute a ground slam attack that affects players in an area
     */
    public void executeSlamAttack() {
        if (slamAttackCooldownRemaining <= 0 && !isExecutingAttack) {
            isExecutingAttack = true;

            // Visual and sound effects
            this.level().broadcastEntityEvent(this, (byte) 4); // Custom entity event for clients to show animation
            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.8F);

            // Create position for particle effects
            double entityX = this.getX();
            double entityY = this.getY();
            double entityZ = this.getZ();
            ServerLevel serverLevel = (ServerLevel) this.level();
            RandomSource random = this.getRandom();

            // Create ground impact particles - dust and smoke in a circular pattern
            for (int i = 0; i < 80; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = random.nextDouble() * SLAM_ATTACK_RANGE;
                double xPos = entityX + Math.cos(angle) * distance;
                double yPos = entityY + 0.1;
                double zPos = entityZ + Math.sin(angle) * distance;

                // Velocity for the particles - moving outward from impact
                double xVel = Math.cos(angle) * 0.15;
                double zVel = Math.sin(angle) * 0.15;

                // Mix of particles for better effect
                if (i % 3 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            xPos, yPos, zPos,
                            1, // count
                            xVel * 0.2, 0.05, zVel * 0.2,
                            0.01 // speed
                    );
                } else {
                    serverLevel.sendParticles(
                            ParticleTypes.ASH, // or CLOUD
                            xPos, yPos, zPos,
                            1, // count
                            xVel, 0.1, zVel,
                            0.05 // speed
                    );
                }
            }

        }
    }

    /**
     * Execute a ranged attack by throwing a wasteland block at the target
     */
    public void executeRangedAttack() {
        if (rangedAttackCooldownRemaining <= 0 && !isExecutingAttack && !this.level().isClientSide()) {
            isExecutingAttack = true;

            // Visual and sound effects
            this.level().broadcastEntityEvent(this, (byte) 7); // Custom entity event for clients to show animation
            this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 0.6F);

            // Find target
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                // Calculate direction and velocity
                Vec3 eyePos = new Vec3(this.getX(), this.getEyeY(), this.getZ());
                Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                Vec3 direction = targetPos.subtract(eyePos).normalize();

                // Lead the target based on its movement
                Vec3 targetVelocity = target.getDeltaMovement();
                double distance = target.distanceTo(this);
                double timeToHit = distance / 1.5; // Assuming projectile speed of 1.5 blocks per tick
                Vec3 predictedPos = targetPos.add(targetVelocity.x * timeToHit, 0, targetVelocity.z * timeToHit);
                Vec3 adjustedDirection = predictedPos.subtract(eyePos).normalize();

                // Create and shoot projectile
                EntityWastelandProjectile projectile = new EntityWastelandProjectile(this.level(), this);
                projectile.setPos(
                        this.getX() + direction.x * 0.5,
                        this.getEyeY() - 0.1,
                        this.getZ() + direction.z * 0.5
                );

                // Add some randomness to the aim
                double inaccuracy = 0.05;
                double velocityScale = 1.5; // Speed of projectile
                projectile.shoot(
                        adjustedDirection.x,
                        adjustedDirection.y + 0.1, // Aim slightly upward to account for gravity
                        adjustedDirection.z,
                        (float) velocityScale,
                        (float) inaccuracy
                );

                this.level().addFreshEntity(projectile);
            }

            // Set cooldown
            rangedAttackCooldownRemaining = RANGED_ATTACK_COOLDOWN;
            isExecutingAttack = false;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 4 || pId == 5 || pId == 6 || pId == 7) {
            // Special attack events will be handled by the client renderer
            // 4 = slam attack, 5 = radiation burst, 6 = summon ghouls, 7 = ranged attack
            if (this.level().isClientSide()) {
                // On client side, trigger the animation in the model
                if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this) instanceof FeralGhoulBossRenderer renderer) {
                    ((ModelFeralGhoulBoss)renderer.getModel()).handleEntityEvent(pId);
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Boss is immune to radiation and wither damage
        return source.is(DamageTypes.WITHER) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        // Reduced damage from projectiles
        if (pSource.getDirectEntity() instanceof AbstractArrow) {
            pAmount = pAmount * 0.5F;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // Make the boss 1.5x wider and taller than a regular zombie/ghoul
        // Default zombie dimensions are typically around 0.6f wide and 1.95f tall
        return super.getDimensions(pose).scale(1.5f, 1.5f);
    }
}
