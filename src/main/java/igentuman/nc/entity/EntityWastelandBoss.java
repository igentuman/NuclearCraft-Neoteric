package igentuman.nc.entity;

import igentuman.nc.entity.goal.RadiationBurstGoal;
import igentuman.nc.entity.goal.RangedAttackGoal;
import igentuman.nc.entity.goal.SlamAttackGoal;
import igentuman.nc.entity.goal.SummonGhoulsGoal;
import igentuman.nc.entity.goal.ThrowSpamGoal;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.entries.Ghouls;
import igentuman.nc.setup.level.ModBiomes;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EntityWastelandBoss extends EntityFeralGhoul {

    public static final float SLAM_ATTACK_RANGE = 8.0F;
    public static final int SLAM_ATTACK_COOLDOWN = 60;
    public static final float RADIATION_BURST_RANGE = 8.0F;
    public static final int RADIATION_BURST_COOLDOWN = 60;
    public static final float SUMMON_RANGE = 32.0F;
    public static final int SUMMON_COOLDOWN = 120;
    public static final int MAX_SUMMONS = 8;
    public static final float MIN_RANGED_ATTACK_DISTANCE = 8.0F;
    public static final float MAX_RANGED_ATTACK_DISTANCE = 64.0F;
    public static final int RANGED_ATTACK_COOLDOWN = 20;
    public static final float THROW_SPAM_RANGE = 32.0F;
    public static final int THROW_SPAM_COOLDOWN = 300;
    public static final int THROW_SPAM_DURATION = 200;
    public static final int THROW_SPAM_INTERVAL = 8;

    public static final byte ANIMATION_SLAM = 4;
    public static final byte ANIMATION_RADIATION_BURST = 5;
    public static final byte ANIMATION_SUMMON = 6;
    public static final byte ANIMATION_RANGED = 7;
    public static final byte ANIMATION_MELEE = 8;
    public static final byte ANIMATION_THROW_SPAM = 9;
    public static final byte EVENT_ENRAGED = 10;

    private static final double MELEE_RADIATION_SV = 2.0D;
    private static final double BURST_RADIATION_SV = 1.0D;

    public int slamAttackCooldownRemaining;
    public int radiationBurstCooldownRemaining;
    public int summonCooldownRemaining;
    public int rangedAttackCooldownRemaining;
    public int throwSpamCooldownRemaining;
    public int throwSpamDurationRemaining;
    public int throwSpamIntervalRemaining;

    private boolean hasEnteredEnragedState;
    private byte animationEvent;
    private int animationStartTick;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.nuclearcraft.feral_ghoul_boss"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS);

    public EntityWastelandBoss(Level level) {
        this(Ghouls.FERAL_GHOUL_BOSS.get(), level);
    }

    public EntityWastelandBoss(EntityType<? extends EntityWastelandBoss> type, Level level) {
        super(type, level);
        xpReward = 250;
        setPersistenceRequired();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NCSounds.BOSS_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return NCSounds.BOSS_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NCSounds.BOSS_ANGRY.get();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new ThrowSpamGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this));
        goalSelector.addGoal(3, new RadiationBurstGoal(this));
        goalSelector.addGoal(4, new SummonGhoulsGoal(this));
        goalSelector.addGoal(5, new SlamAttackGoal(this));
        goalSelector.addGoal(6, new FeralGhoulVaultGoal(this));
        goalSelector.addGoal(7, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 64.0F));
        goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 70.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 20.0D);
    }

    public static boolean checkSpawnRules(EntityType<EntityWastelandBoss> type, ServerLevelAccessor level,
                                          MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)
                && level.getBiome(pos).is(ModBiomes.WASTELAND_TAG);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        level().broadcastEntityEvent(this, ANIMATION_MELEE);
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof Player player) {
            EntityRadiationData radiation = player.getData(NRAttachments.ENTITY_RADIATION.get());
            radiation.addSv(MELEE_RADIATION_SV);
            Vec3 knockback = player.position().subtract(position()).normalize().scale(2.0D);
            player.setDeltaMovement(player.getDeltaMovement().add(knockback.x, 0.25D, knockback.z));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
            bossEvent.setName(getDisplayName());
        }
        slamAttackCooldownRemaining = decrement(slamAttackCooldownRemaining);
        radiationBurstCooldownRemaining = decrement(radiationBurstCooldownRemaining);
        summonCooldownRemaining = decrement(summonCooldownRemaining);
        rangedAttackCooldownRemaining = decrement(rangedAttackCooldownRemaining);
        throwSpamCooldownRemaining = decrement(throwSpamCooldownRemaining);
        throwSpamDurationRemaining = decrement(throwSpamDurationRemaining);
        throwSpamIntervalRemaining = decrement(throwSpamIntervalRemaining);

        if (!level().isClientSide && !hasEnteredEnragedState && isEnraged()) {
            hasEnteredEnragedState = true;
            playSound(NCSounds.BOSS_ANGRY.get(), 1.5F, 0.6F);
            playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 0.8F);
            level().broadcastEntityEvent(this, EVENT_ENRAGED);
        }
    }

    private static int decrement(int value) {
        return Math.max(0, value - 1);
    }

    public void executeRadiationBurst() {
        if (radiationBurstCooldownRemaining > 0 || level().isClientSide) {
            return;
        }
        playSound(NCSounds.BOSS_ANGRY.get(), 0.7F, 1.0F);
        playSound(SoundEvents.BEACON_POWER_SELECT, 0.8F, 0.5F);

        ServerLevel server = (ServerLevel) level();
        Vec3 center = position();
        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * RADIATION_BURST_RANGE / 3.0D;
            int count = 20 + ring * 10;
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2.0D * i / count;
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        center.x + Math.cos(angle) * radius,
                        center.y + 0.2D + random.nextDouble() * 2.5D,
                        center.z + Math.sin(angle) * radius,
                        1, Math.cos(angle) * 0.04D, 0.03D, Math.sin(angle) * 0.04D, 0.01D);
            }
        }

        AABB area = getBoundingBox().inflate(RADIATION_BURST_RANGE);
        for (Player player : level().getEntitiesOfClass(Player.class, area, Player::isAlive)) {
            player.getData(NRAttachments.ENTITY_RADIATION.get()).addSv(BURST_RADIATION_SV);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
        }
        radiationBurstCooldownRemaining = RADIATION_BURST_COOLDOWN;
    }

    public void executeSummonAttack() {
        if (summonCooldownRemaining > 0 || !(level() instanceof ServerLevel server)) {
            return;
        }
        playSound(NCSounds.BOSS_ANGRY.get(), 0.7F, 0.6F);
        playSound(SoundEvents.VEX_CHARGE, 1.0F, 0.7F);

        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            int nearby = level().getEntitiesOfClass(EntityFeralGhoul.class,
                    getBoundingBox().inflate(SUMMON_RANGE), ghoul -> ghoul != this).size();
            int attempts = Math.max(0, MAX_SUMMONS - nearby);
            for (int i = 0; i < attempts; i++) {
                BlockPos spawnPos = findSummonPosition(target.blockPosition());
                if (spawnPos == null) {
                    continue;
                }
                EntityFeralGhoul ghoul = Ghouls.FERAL_GHOUL.get().create(server);
                if (ghoul == null) {
                    continue;
                }
                ghoul.moveTo(spawnPos, random.nextFloat() * 360.0F, 0.0F);
                ghoul.finalizeSpawn(server, server.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
                ghoul.setTarget(target);
                server.addFreshEntity(ghoul);
                server.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        spawnPos.getX() + 0.5D, spawnPos.getY() + 1.2D, spawnPos.getZ() + 0.5D,
                        8, 0.35D, 0.6D, 0.35D, 0.02D);
            }
        }
        summonCooldownRemaining = SUMMON_COOLDOWN;
    }

    private BlockPos findSummonPosition(BlockPos targetPos) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = 3.0D + random.nextDouble() * (SUMMON_RANGE - 3.0D);
        int x = Mth.floor(targetPos.getX() + Math.sin(angle) * distance);
        int z = Mth.floor(targetPos.getZ() + Math.cos(angle) * distance);
        for (int y = targetPos.getY() + 4; y >= targetPos.getY() - 4; y--) {
            BlockPos candidate = new BlockPos(x, y, z);
            BlockState floor = level().getBlockState(candidate.below());
            if (floor.isSolidRender(level(), candidate.below())
                    && level().getBlockState(candidate).getCollisionShape(level(), candidate).isEmpty()
                    && level().getBlockState(candidate.above()).getCollisionShape(level(), candidate.above()).isEmpty()) {
                return candidate;
            }
        }
        return null;
    }

    public void executeSlamAttack() {
        if (slamAttackCooldownRemaining > 0 || !(level() instanceof ServerLevel server)) {
            return;
        }
        playSound(NCSounds.BOSS_ACTION.get(), 1.4F, 0.9F);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.1F);

        for (int i = 0; i < 50; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = random.nextDouble() * SLAM_ATTACK_RANGE;
            server.sendParticles(i % 3 == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    getX() + Math.cos(angle) * distance, getY() + 0.1D, getZ() + Math.sin(angle) * distance,
                    1, Math.cos(angle) * 0.1D, 0.08D, Math.sin(angle) * 0.1D, 0.03D);
        }

        AABB area = getBoundingBox().inflate(SLAM_ATTACK_RANGE);
        List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive());
        for (LivingEntity victim : victims) {
            double distance = Math.max(0.5D, victim.distanceTo(this));
            if (distance > SLAM_ATTACK_RANGE) {
                continue;
            }
            float damage = 10.0F * (1.0F - (float) (distance / SLAM_ATTACK_RANGE));
            victim.hurt(damageSources().mobAttack(this), damage);
            Vec3 direction = victim.position().subtract(position()).normalize();
            double strength = Mth.clamp(Math.log(SLAM_ATTACK_RANGE / distance) + 0.6D, 0.6D, 2.5D);
            victim.setDeltaMovement(direction.x * strength, strength, direction.z * strength);
        }
        slamAttackCooldownRemaining = SLAM_ATTACK_COOLDOWN;
    }

    public void executeRangedAttack() {
        if (rangedAttackCooldownRemaining > 0 || level().isClientSide) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        playSound(NCSounds.BOSS_ACTION.get(), 1.4F, 0.9F);
        playSound(SoundEvents.WITHER_SHOOT, 1.0F, 0.2F);

        Vec3 origin = new Vec3(getX(), getEyeY(), getZ());
        Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        Vec3 forward = targetPos.subtract(origin).normalize();
        Vec3 side = new Vec3(forward.z, 0.0D, -forward.x).normalize().scale(0.75D);
        throwProjectile(target, side);
        throwProjectile(target, side.scale(-1.0D));
        rangedAttackCooldownRemaining = RANGED_ATTACK_COOLDOWN;
    }

    private void throwProjectile(LivingEntity target, Vec3 sideOffset) {
        double speed = 3.0D;
        Vec3 origin = new Vec3(getX(), getEyeY() - 0.3D, getZ());
        Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        double flightTime = target.distanceTo(this) / speed;
        Vec3 predicted = targetPos.add(target.getDeltaMovement().multiply(flightTime, 0.0D, flightTime));
        Vec3 direction = predicted.subtract(origin).normalize();
        double lift = Math.log10(Math.max(1.0D, target.distanceTo(this))) * 0.05D;

        EntityBlockProjectile projectile = new EntityBlockProjectile(level(), this);
        projectile.setPos(origin.x + sideOffset.x + direction.x * 0.5D,
                origin.y, origin.z + sideOffset.z + direction.z * 0.5D);
        projectile.shoot(direction.x, direction.y + lift, direction.z, (float) speed, 0.1F);
        level().addFreshEntity(projectile);
    }

    public void startThrowSpamAttack() {
        if (throwSpamCooldownRemaining > 0 || level().isClientSide) {
            return;
        }
        throwSpamDurationRemaining = THROW_SPAM_DURATION;
        throwSpamIntervalRemaining = 0;
        throwSpamCooldownRemaining = THROW_SPAM_COOLDOWN;
        playSound(NCSounds.BOSS_ANGRY.get(), 1.2F, 0.7F);
        playSound(SoundEvents.RAVAGER_ROAR, 0.8F, 0.6F);
    }

    public void throwSpamProjectile() {
        if (throwSpamDurationRemaining <= 0 || throwSpamIntervalRemaining > 0 || level().isClientSide) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || distanceTo(target) > THROW_SPAM_RANGE) {
            return;
        }
        playSound(SoundEvents.WITHER_SHOOT, 0.5F, 1.2F + random.nextFloat() * 0.4F);
        double speed = 2.5D + random.nextDouble();
        Vec3 origin = new Vec3(getX() + (random.nextDouble() - 0.5D) * 1.5D,
                getEyeY() + 0.5D, getZ() + (random.nextDouble() - 0.5D) * 1.5D);
        Vec3 targetPos = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        double flightTime = target.distanceTo(this) / speed;
        Vec3 predicted = targetPos.add(target.getDeltaMovement().multiply(flightTime, 0.0D, flightTime));
        Vec3 direction = predicted.subtract(origin).normalize();
        double lift = Math.log10(Math.max(1.0D, target.distanceTo(this))) * 0.08D;

        EntityBlockProjectile projectile = new EntityBlockProjectile(level(), this);
        projectile.setPos(origin);
        projectile.shoot(direction.x, direction.y + lift, direction.z, (float) speed, 0.3F);
        level().addFreshEntity(projectile);
        throwSpamIntervalRemaining = THROW_SPAM_INTERVAL;
    }

    public boolean isEnraged() {
        return getHealth() < getMaxHealth() * 0.5F;
    }

    public byte getAnimationEvent() {
        return animationEvent;
    }

    public int getAnimationStartTick() {
        return animationStartTick;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id >= ANIMATION_SLAM && id <= ANIMATION_THROW_SPAM) {
            animationEvent = id;
            animationStartTick = tickCount;
            return;
        }
        if (id == EVENT_ENRAGED) {
            for (int i = 0; i < 40; i++) {
                level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                        getRandomX(1.5D), getRandomY(), getRandomZ(1.5D),
                        0.0D, 0.08D, 0.0D);
            }
            return;
        }
        super.handleEntityEvent(id);
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

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.WITHER) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= 0.25F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SlamCooldown", slamAttackCooldownRemaining);
        tag.putInt("RadiationBurstCooldown", radiationBurstCooldownRemaining);
        tag.putInt("SummonCooldown", summonCooldownRemaining);
        tag.putInt("RangedCooldown", rangedAttackCooldownRemaining);
        tag.putInt("ThrowSpamCooldown", throwSpamCooldownRemaining);
        tag.putBoolean("HasEnraged", hasEnteredEnragedState);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        slamAttackCooldownRemaining = tag.getInt("SlamCooldown");
        radiationBurstCooldownRemaining = tag.getInt("RadiationBurstCooldown");
        summonCooldownRemaining = tag.getInt("SummonCooldown");
        rangedAttackCooldownRemaining = tag.getInt("RangedCooldown");
        throwSpamCooldownRemaining = tag.getInt("ThrowSpamCooldown");
        hasEnteredEnragedState = tag.getBoolean("HasEnraged");
        if (hasCustomName()) {
            bossEvent.setName(getDisplayName());
        }
    }
}
