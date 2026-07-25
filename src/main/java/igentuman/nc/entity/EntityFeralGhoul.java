package igentuman.nc.entity;

import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.entries.Ghouls;
import igentuman.nc.setup.level.ModBiomes;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
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
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class EntityFeralGhoul extends Zombie {

    private static final int VAULT_COOLDOWN = 60;
    private static final double VAULT_TRIGGER_DISTANCE = 16.0D;
    private static final double VAULT_JUMP_POWER = 0.7D;
    private static final double VAULT_FORWARD_MOMENTUM = 0.8D;
    private static final double RADIATION_HIT_SV = 2.0D;

    private int vaultCooldownRemaining = 0;

    public EntityFeralGhoul(Level level) {
        this(Ghouls.FERAL_GHOUL.get(), level);
    }

    @SuppressWarnings("unchecked")
    public EntityFeralGhoul(EntityType<?> entityType, Level level) {
        super((EntityType<? extends Zombie>) entityType, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NCSounds.FERAL_GHOUL_CHARGE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return NCSounds.FERAL_GHOUL_DEATH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NCSounds.FERAL_GHOUL_DEATH.get();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @javax.annotation.Nullable SpawnGroupData spawnGroupData) {
        return new GhoulGroupData(false, false);
    }

    public static class GhoulGroupData implements SpawnGroupData {
        public final boolean isBaby;
        public final boolean canSpawnJockey;

        public GhoulGroupData(boolean isBaby, boolean canSpawnJockey) {
            this.isBaby = isBaby;
            this.canSpawnJockey = canSpawnJockey;
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
        if (vaultCooldownRemaining > 0) {
            vaultCooldownRemaining--;
        }
    }

    public void vault() {
        if (vaultCooldownRemaining > 0) {
            return;
        }
        Vec3 direction;
        if (getTarget() != null) {
            direction = new Vec3(
                    getTarget().getX() - this.getX(),
                    0.0D,
                    getTarget().getZ() - this.getZ()
            ).normalize();
        } else {
            direction = Vec3.directionFromRotation(0, this.getYRot()).normalize();
        }

        this.setDeltaMovement(
                direction.x * VAULT_FORWARD_MOMENTUM,
                VAULT_JUMP_POWER,
                direction.z * VAULT_FORWARD_MOMENTUM
        );

        this.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 0.2F, 1.2F);
        vaultCooldownRemaining = VAULT_COOLDOWN;
    }

    public boolean hasObstacleAhead() {
        Vec3 directionVec = Vec3.directionFromRotation(0, this.getYRot()).normalize();
        BlockPos currentPos = this.blockPosition();
        BlockPos forwardPos = BlockPos.containing(
                currentPos.getX() + directionVec.x * 2,
                currentPos.getY(),
                currentPos.getZ() + directionVec.z * 2
        );

        return !this.level().getBlockState(forwardPos).isAir() ||
               !this.level().getBlockState(forwardPos.above()).isAir();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof Player player) {
            EntityRadiationData data = player.getData(NRAttachments.ENTITY_RADIATION.get());
            data.addSv(RADIATION_HIT_SV);
            this.playSound(SoundEvents.GENERIC_DRINK, 0.2F, 0.8F);
        }
        return result;
    }

    public static boolean checkFeralGhoulSpawnRules(EntityType<EntityFeralGhoul> entityType,
                                                    ServerLevelAccessor level,
                                                    MobSpawnType spawnType,
                                                    BlockPos pos,
                                                    RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }
        return level.getBiome(pos).is(ModBiomes.WASTELAND_TAG);
    }

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

            if (this.ghoul.distanceTo(target) <= VAULT_TRIGGER_DISTANCE) {
                return this.ghoul.hasObstacleAhead();
            }

            if (--this.checkPathDelay > 0) {
                return false;
            }

            this.checkPathDelay = 10;
            this.path = this.ghoul.getNavigation().createPath(target, 0);

            if (this.path == null || this.path.canReach()) {
                return false;
            }

            return this.ghoul.hasObstacleAhead();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.ghoul.vault();
        }
    }
}
