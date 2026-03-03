package igentuman.nc.entity;

import igentuman.nc.setup.registration.NCAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL;
import static igentuman.nc.setup.registration.NCSounds.FERAL_GHOUL_CHARGE;
import static igentuman.nc.setup.registration.NCSounds.FERAL_GHOUL_DEATH;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;

public class EntityFeralGhoul extends Zombie {

    // Vaulting parameters
    private static final int VAULT_COOLDOWN = 60; // ticks (3 seconds)
    private static final double VAULT_TRIGGER_DISTANCE = 16.0D;
    private static final double VAULT_JUMP_POWER = 0.7D;
    private static final double VAULT_FORWARD_MOMENTUM = 0.8D;

    // Radiation parameters
    private static final int RADIATION_AMOUNT = 10000000;

    private int vaultCooldownRemaining = 0;

    public EntityFeralGhoul(Level pLevel) {
        this(FERAL_GHOUL.get(), pLevel);
    }

    @SuppressWarnings("unchecked")
    public EntityFeralGhoul(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Zombie>) entityType, level);
    }

    protected SoundEvent getAmbientSound() {
        return FERAL_GHOUL_CHARGE.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return FERAL_GHOUL_DEATH.get();
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        // Set the spawn data to null to avoid spawning with equipment
        return new GhoulGroupData(false, false);
    }

    public static class GhoulGroupData implements SpawnGroupData {
        public final boolean isBaby;
        public final boolean canSpawnJockey;

        public GhoulGroupData(boolean pIsBaby, boolean pCanSpawnJockey) {
            this.isBaby = pIsBaby;
            this.canSpawnJockey = pCanSpawnJockey;
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FeralGhoulVaultGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4F)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 45.0D);
    }

    @Override
    public void tick() {
        super.tick();

        // Decrease vault cooldown if active
        if (vaultCooldownRemaining > 0) {
            vaultCooldownRemaining--;
        }
    }

    /**
     * Initiates a vault jump over obstacles
     */
    public void vault() {
        if (vaultCooldownRemaining <= 0) {
            // Calculate direction vector toward the target
            Vec3 direction = Vec3.ZERO;
            if (getTarget() != null) {
                direction = new Vec3(
                        getTarget().getX() - this.getX(),
                        0.0D,
                        getTarget().getZ() - this.getZ()
                ).normalize();
            } else {
                direction = Vec3.directionFromRotation(0, this.getYRot()).normalize();
            }

            // Apply vertical and horizontal momentum
            this.setDeltaMovement(
                    direction.x * VAULT_FORWARD_MOMENTUM,
                    VAULT_JUMP_POWER,
                    direction.z * VAULT_FORWARD_MOMENTUM
            );

            // Play jump sound
            this.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 0.2F, 1.2F);

            // Set cooldown
            vaultCooldownRemaining = VAULT_COOLDOWN;
        }
    }

    /**
     * Checks if there's an obstacle in front of the entity
     */
    public boolean hasObstacleAhead() {
        Vec3 directionVec = Vec3.directionFromRotation(0, this.getYRot()).normalize();
        BlockPos currentPos = this.blockPosition();
        BlockPos forwardPos = new BlockPos(
                (int) (currentPos.getX() + directionVec.x * 2),
                currentPos.getY(),
                (int) (currentPos.getZ() + directionVec.z * 2)
        );

        // Check if the blocks ahead and above have collision boxes
        return !this.level().getBlockState(forwardPos).isAir() ||
               !this.level().getBlockState(forwardPos.above()).isAir();
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        boolean attackResult = super.doHurtTarget(pEntity);

        // Add radiation to player when attacked
        if (attackResult && pEntity instanceof Player player) {
            var radiation = player.getData(NCAttachments.PLAYER_RADIATION.get());
            long currentRadiation = radiation.getRadiation();
            radiation.setRadiation(currentRadiation + RADIATION_AMOUNT);

            // Play radiation effect sound
            this.playSound(SoundEvents.GENERIC_DRINK, 0.2F, 0.8F);
        }

        return attackResult;
    }

    /**
     * Custom spawn rule that only allows Feral Ghouls to spawn in wasteland biomes
     */
    public static boolean checkFeralGhoulSpawnRules(EntityType<EntityFeralGhoul> entityType,
                                              ServerLevelAccessor level,
                                              MobSpawnType spawnType,
                                              BlockPos pos,
                                              net.minecraft.util.RandomSource random) {

        if (!Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }

        return level.getBiome(pos).is(WASTELAND);
    }

    /**
     * Goal that handles vaulting behavior for Feral Ghouls
     */
    static class FeralGhoulVaultGoal extends Goal {
        private final EntityFeralGhoul ghoul;
        private Path path;
        private int checkPathDelay;
        private boolean pathBlocked;

        public FeralGhoulVaultGoal(EntityFeralGhoul ghoul) {
            this.ghoul = ghoul;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.ghoul.getTarget();
            if (target == null || !target.isAlive() || this.ghoul.vaultCooldownRemaining > 0) {
                return false;
            }

            // Check if target is within vault trigger distance
            if (this.ghoul.distanceTo(target) <= VAULT_TRIGGER_DISTANCE) {
                return this.ghoul.hasObstacleAhead();
            }

            // Path finding checks
            if (--this.checkPathDelay > 0) {
                return false;
            }

            this.checkPathDelay = 10;
            this.path = this.ghoul.getNavigation().createPath(target, 0);

            // If path is null or unreachable, consider vaulting
            if (this.path == null || this.path.canReach()) {
                return false;
            }

            return this.ghoul.hasObstacleAhead();
        }

        @Override
        public boolean canContinueToUse() {
            return false; // One-shot action, do not continue
        }

        @Override
        public void start() {
            this.ghoul.vault();
        }
    }
}
