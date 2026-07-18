package igentuman.nc.entity;

import igentuman.nc.client.renderer.WastelandBossRenderer;
import igentuman.nc.entity.goal.RadiationBurstGoal;
import igentuman.nc.entity.goal.RangedAttackGoal;
import igentuman.nc.entity.goal.SlamAttackGoal;
import igentuman.nc.entity.goal.SummonGhoulsGoal;
import igentuman.nc.entity.goal.ThrowSpamGoal;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
import static igentuman.nc.setup.registration.NCSounds.*;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class EntityWastelandBoss extends EntityFeralGhoul {

    // Boss parameters
    public static final int BOSS_RADIATION_AMOUNT = 20000000;
    public static final float SLAM_ATTACK_RANGE = 8.0F;
    public static final int SLAM_ATTACK_COOLDOWN = 60; // 4 seconds
    public static final int RADIATION_BURST_COOLDOWN = 60; // 8 seconds
    public static final float RADIATION_BURST_RANGE = 8.0F;
    public static final int RADIATION_BURST_AMOUNT = 10000000;
    public static final int SUMMON_COOLDOWN = 120; // 24 seconds
    public static final float SUMMON_RANGE = 32.0F;
    public static final int MAX_SUMMONS = 8;
    // Ranged attack parameters
    public static final int RANGED_ATTACK_COOLDOWN = 20;
    public static final float MIN_RANGED_ATTACK_DISTANCE = 8.0F;
    public static final float MAX_RANGED_ATTACK_DISTANCE = 64.0F;
    // Throw spam attack parameters
    public static final int THROW_SPAM_COOLDOWN = 300; // 15 seconds
    public static final int THROW_SPAM_DURATION = 200; // 10 seconds
    public static final int THROW_SPAM_INTERVAL = 8; // Throw every 8 ticks (0.4 seconds)
    public static final float THROW_SPAM_RANGE = 32.0F;

    public int slamAttackCooldownRemaining = 0;
    public int radiationBurstCooldownRemaining = 0;
    public int summonCooldownRemaining = 0;
    public int rangedAttackCooldownRemaining = 0;
    public int throwSpamCooldownRemaining = 0;
    public int throwSpamDurationRemaining = 0;
    public int throwSpamIntervalRemaining = 0;
    public boolean hasEnteredEnragedState = false;
    public final Random random = new Random();
    public boolean isExecutingAttack = false;

    // Boss bar
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.nuclearcraft.feral_ghoul_boss"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public EntityWastelandBoss(Level pLevel) {
        this(FERAL_GHOUL_BOSS.get(), pLevel);
    }

    @SuppressWarnings("unchecked")
    public EntityWastelandBoss(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 250;
        this.refreshDimensions();
        this.setPersistenceRequired();
        this.setHealth(getMaxHealth());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return BOSS_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return BOSS_HIT.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FeralGhoulVaultGoal(this));
        this.goalSelector.addGoal(7, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(5, new SlamAttackGoal(this));
        this.goalSelector.addGoal(3, new RadiationBurstGoal(this));
        this.goalSelector.addGoal(4, new SummonGhoulsGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this));
        this.goalSelector.addGoal(1, new ThrowSpamGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.40F)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 70.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 2.8D)
                .add(Attributes.ARMOR, 20.0D);
    }

    public static boolean checkFeralGhoulBossSpawnRules(EntityType<EntityWastelandBoss> entityType,
                                                    ServerLevelAccessor level,
                                                    MobSpawnType spawnType,
                                                    BlockPos pos,
                                                    net.minecraft.util.RandomSource random) {

        if (!Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }
        //todo implement specific conditions for spawning the boss

        return level.getBiome(pos).is(WASTELAND);
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        this.level.broadcastEntityEvent(this, (byte) 8);
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

        if (throwSpamCooldownRemaining > 0) {
            throwSpamCooldownRemaining--;
        }

        if (throwSpamDurationRemaining > 0) {
            throwSpamDurationRemaining--;
            if (throwSpamIntervalRemaining > 0) {
                throwSpamIntervalRemaining--;
            }
        }

        // Check for enraged state transition
        if (!hasEnteredEnragedState && isEnraged()) {
            hasEnteredEnragedState = true;
            // Play dramatic enrage sound
            this.playSound(BOSS_ANGRY.get(), 1.5F, 0.6F);
            this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 0.8F);
            // Broadcast entity event for potential client-side effects
            this.level.broadcastEntityEvent(this, (byte) 10); // Custom enrage event
        }
    }

    /**
     * Execute a radiation burst attack that affects players in an area
     */
    public void executeRadiationBurst() {
        if (radiationBurstCooldownRemaining <= 0 && !isExecutingAttack) {
            isExecutingAttack = true;
            this.playSound(BOSS_ANGRY.get(), 0.7F, 1.0F);
            this.playSound(GEIGER_SOUNDS.get(2).get(), 0.7F, 1F);

            AABB radiationBox = this.getBoundingBox().inflate(RADIATION_BURST_RANGE);
            List<Player> nearbyPlayers = this.level.getEntitiesOfClass(Player.class, radiationBox);

            if (this.level instanceof ServerLevel serverLevel) {
                double entityY = this.getY() + 0.5;
                Vec3 center = this.position();

                for (int i = 0; i < 50; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = random.nextDouble() * RADIATION_BURST_RANGE;
                    double height = random.nextDouble() * 3;

                    double xPos = center.x + radius * Math.cos(angle);
                    double yPos = entityY + height+1;
                    double zPos = center.z + radius * Math.sin(angle);

                    double xVel = (xPos - center.x) * 0.05;
                    double yVel = random.nextDouble() * 0.05;
                    double zVel = (zPos - center.z) * 0.05;

                    serverLevel.sendParticles(
                            NcParticleTypes.RADIATION.get(),
                            xPos, yPos, zPos,
                            1,
                            xVel, yVel, zVel,
                            0.02
                    );
                }

                for (int ring = 0; ring < 3; ring++) {
                    double ringRadius = (ring + 1) * (RADIATION_BURST_RANGE / 3);
                    int particlesInRing = 20 + (ring * 10);

                    for (int i = 0; i < particlesInRing; i++) {
                        double angle = (Math.PI * 2 * i) / particlesInRing;
                        double xPos = center.x + ringRadius * Math.cos(angle);
                        double yPos = center.y + 0.2;
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

                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 10, 0));
            }

            radiationBurstCooldownRemaining = RADIATION_BURST_COOLDOWN;
            isExecutingAttack = false;
        }
    }

    /**
     * Execute summon attack that spawns feral ghouls near the target
     */
    public void executeSummonAttack() {
        if (summonCooldownRemaining <= 0 && !isExecutingAttack && !this.level.isClientSide()) {
            isExecutingAttack = true;
            this.playSound(BOSS_ANGRY.get(), 0.4F, 0.4F);
            this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 0.7F);

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                int existingGhouls = 0;
                List<EntityFeralGhoul> nearbyGhouls = this.level.getEntitiesOfClass(
                        EntityFeralGhoul.class,
                        this.getBoundingBox().inflate(20.0),
                        ghoul -> !(ghoul instanceof EntityWastelandBoss)
                );
                existingGhouls = nearbyGhouls.size();

                int spawnCount = Math.min(MAX_SUMMONS, MAX_SUMMONS - existingGhouls);

                if (spawnCount > 0) {
                    for (int i = 0; i < spawnCount; i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double distance = 2.0 + random.nextDouble() * (SUMMON_RANGE - 2.0);
                        double xOffset = Math.sin(angle) * distance;
                        double zOffset = Math.cos(angle) * distance;

                        BlockPos spawnPos = new BlockPos(
                                (int) (target.getX() + xOffset),
                                (int) target.getY(),
                                (int) (target.getZ() + zOffset)
                        );

                        if (this.level.getBlockState(spawnPos).isAir() &&
                                this.level.getBlockState(spawnPos.below()).getMaterial().isSolid()) {

                            EntityFeralGhoul ghoul = new EntityFeralGhoul(this.level);
                            ghoul.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                            ghoul.setTarget(target);

                            this.level.addParticle(
                                    ParticleTypes.ANGRY_VILLAGER,
                                    spawnPos.getX() + 0.5,
                                    spawnPos.getY() + 1.5,
                                    spawnPos.getZ() + 0.5,
                                    0, 0.1, 0
                            );

                            this.level.addFreshEntity(ghoul);
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

            this.playSound(BOSS_ACTION.get(), 1.4F, 0.9F);
            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.1F);

            double entityX = this.getX();
            double entityY = this.getY();
            double entityZ = this.getZ();
            ServerLevel serverLevel = (ServerLevel) this.level;
            RandomSource random = this.getRandom();

            for (int i = 0; i < 40; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = random.nextDouble() * SLAM_ATTACK_RANGE;
                double xPos = entityX + Math.cos(angle) * distance;
                double yPos = entityY + 0.1;
                double zPos = entityZ + Math.sin(angle) * distance;

                double xVel = Math.cos(angle) * 0.15;
                double zVel = Math.sin(angle) * 0.15;

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
                            ParticleTypes.CAMPFIRE_COSY_SMOKE, // or CLOUD
                            xPos, yPos, zPos,
                            1, // count
                            xVel, 0.1, zVel,
                            0.05 // speed
                    );
                }
            }

            AABB affectedArea = this.getBoundingBox().inflate(SLAM_ATTACK_RANGE);
            List<LivingEntity> nearbyEntities = this.level.getEntitiesOfClass(
                    LivingEntity.class,
                    affectedArea,
                    entity -> entity != this
            );

            for (LivingEntity entity : nearbyEntities) {
                double distance = entity.distanceTo(this);
                float damageAmount = 10.0f * (1.0f - (float)(distance / SLAM_ATTACK_RANGE));

                entity.hurt(DamageSource.mobAttack(this), damageAmount);

                Vec3 knockbackDirection = entity.position().subtract(this.position()).normalize();
                double distanceMult = Math.log(SLAM_ATTACK_RANGE/distance)+0.6;
                entity.setDeltaMovement(
                    knockbackDirection.x * distanceMult,
                    1 * distanceMult,
                    knockbackDirection.z * distanceMult
                );
            }

            isExecutingAttack = false;
            slamAttackCooldownRemaining = SLAM_ATTACK_COOLDOWN;
        }
    }

    /**
     * Execute a ranged attack by throwing a wasteland block at the target
     */
    public void executeRangedAttack() {
        if (rangedAttackCooldownRemaining <= 0 && !isExecutingAttack && !this.level.isClientSide()) {
            isExecutingAttack = true;
            this.playSound(BOSS_ACTION.get(), 1.4F, 0.9F);
            this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 0.2F);
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                Vec3 eyePos = new Vec3(this.getX(), this.getEyeY(), this.getZ());
                Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                Vec3 direction = targetPos.subtract(eyePos).normalize();

                Vec3 targetVelocity = target.getDeltaMovement();
                double distance = target.distanceTo(this);
                double velocityScale = 3;
                double timeToHit = distance / velocityScale;
                Vec3 predictedPos = targetPos.add(targetVelocity.x * timeToHit, 0, targetVelocity.z * timeToHit);
                Vec3 adjustedDirection = predictedPos.subtract(eyePos).normalize();
                double inaccuracy = 0.1;
                double verticalAdjustment = Math.log10(distance) * 0.05;

                Vec3 rightVector = new Vec3(direction.z, 0, -direction.x).normalize().scale(0.750);
                Vec3 leftVector = rightVector.scale(-0.750);

                EntityBlockProjectile leftProjectile = new EntityBlockProjectile(this.level, this);
                leftProjectile.setPos(
                        this.getX() + leftVector.x * 1.0 + direction.x * 0.5,
                        this.getEyeY() - 0.3,
                        this.getZ() + leftVector.z * 1.0 + direction.z * 0.5
                );

                leftProjectile.shoot(
                        adjustedDirection.x,
                        adjustedDirection.y + verticalAdjustment,
                        adjustedDirection.z,
                        (float) velocityScale,
                        (float) inaccuracy
                );

                this.level.addFreshEntity(leftProjectile);

                EntityBlockProjectile rightProjectile = new EntityBlockProjectile(this.level, this);
                rightProjectile.setPos(
                        this.getX() + rightVector.x * 1.0 + direction.x * 0.5,
                        this.getEyeY() - 0.3,
                        this.getZ() + rightVector.z * 1.0 + direction.z * 0.5
                );

                rightProjectile.shoot(
                        adjustedDirection.x,
                        adjustedDirection.y + verticalAdjustment,
                        adjustedDirection.z,
                        (float) velocityScale,
                        (float) inaccuracy
                );

                this.level.addFreshEntity(rightProjectile);
            }

            rangedAttackCooldownRemaining = RANGED_ATTACK_COOLDOWN;
            isExecutingAttack = false;
        }
    }

    /**
     * Execute throw spam attack - rapidly throws blocks at the target for 10 seconds
     * Only available when boss health is below 50% (enraged state)
     */
    public void executeThrowSpamAttack() {
        if (throwSpamCooldownRemaining <= 0 && !isExecutingAttack && !this.level.isClientSide()) {
            // Start the throw spam attack
            isExecutingAttack = true;
            throwSpamDurationRemaining = THROW_SPAM_DURATION;
            throwSpamIntervalRemaining = 0; // Start throwing immediately
            
            this.playSound(BOSS_ANGRY.get(), 1.2F, 0.7F);
            this.playSound(SoundEvents.WITHER_AMBIENT, 1.2F, 0.4F);
            this.playSound(SoundEvents.RAVAGER_ROAR, 0.8F, 0.6F);
            
            throwSpamCooldownRemaining = THROW_SPAM_COOLDOWN;
            isExecutingAttack = false;
        }
    }

    /**
     * Throw a single projectile during spam attack
     */
    public void throwSpamProjectile() {
        if (throwSpamDurationRemaining > 0 && throwSpamIntervalRemaining <= 0 && !this.level.isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && target.distanceTo(this) <= THROW_SPAM_RANGE) {
                
                // Play throwing sound
                this.playSound(SoundEvents.WITHER_SHOOT, 0.5F, 1.2F + random.nextFloat() * 0.4F);
                
                Vec3 eyePos = new Vec3(this.getX(), this.getEyeY(), this.getZ());
                Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
                Vec3 direction = targetPos.subtract(eyePos).normalize();

                // Add some randomness to make it less predictable
                double inaccuracy = 0.3;
                double velocityScale = 2.5 + random.nextDouble() * 1.0;
                
                // Predict target movement for better accuracy
                Vec3 targetVelocity = target.getDeltaMovement();
                double distance = target.distanceTo(this);
                double timeToHit = distance / velocityScale;
                Vec3 predictedPos = targetPos.add(targetVelocity.x * timeToHit, 0, targetVelocity.z * timeToHit);
                Vec3 adjustedDirection = predictedPos.subtract(eyePos).normalize();
                
                // Add vertical adjustment for arc
                double verticalAdjustment = Math.log10(distance) * 0.08;

                EntityBlockProjectile projectile = new EntityBlockProjectile(this.level, this);
                
                // Randomize spawn position slightly
                double offsetX = (random.nextDouble() - 0.5) * 1.5;
                double offsetZ = (random.nextDouble() - 0.5) * 1.5;
                
                projectile.setPos(
                        this.getX() + offsetX + direction.x * 0.5,
                        this.getEyeY() + 0.5,
                        this.getZ() + offsetZ + direction.z * 0.5
                );

                projectile.shoot(
                        adjustedDirection.x,
                        adjustedDirection.y + verticalAdjustment,
                        adjustedDirection.z,
                        (float) velocityScale,
                        (float) inaccuracy
                );

                this.level.addFreshEntity(projectile);
                
                // Reset interval timer
                throwSpamIntervalRemaining = THROW_SPAM_INTERVAL;
            }
        }
    }

    /**
     * Check if the boss is currently executing the throw spam attack
     */
    public boolean isThrowSpamActive() {
        return throwSpamDurationRemaining > 0;
    }

    /**
     * Check if the boss is in enraged state (below 50% health)
     * This unlocks the throw spam attack
     */
    public boolean isEnraged() {
        return getHealth() / getMaxHealth() < 0.5f;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    /**
     * Prevents this boss entity from despawning naturally
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Makes this entity persistent even when chunks unload
     */
    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 4 || pId == 5 || pId == 6 || pId == 7 || pId == 8) {
            if (this.level.isClientSide()) {
                if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this) instanceof WastelandBossRenderer renderer) {
                    renderer.getModel().handleEntityEvent(pId);
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.isFire() || source == DamageSource.WITHER || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getDirectEntity() instanceof AbstractArrow) {
            pAmount = pAmount * 0.25F;
        }
        if (pSource.isExplosion()) {
            pAmount = pAmount * 0.25F;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(1.5f, 1.5f);
    }
}
