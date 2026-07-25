package igentuman.nc.entity.anomaly;

import igentuman.nc.client.sound.AnomalyAmbientSound;
import igentuman.nc.config.Common;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.world.anomaly.AnomalySavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public abstract class AnomalyEntity extends Mob {

    public static final long UNSET_CELL = Long.MIN_VALUE;
    public static final byte EVENT_RESOLVE = 60;

    protected static final EntityDataAccessor<Byte> DATA_STATE =
            SynchedEntityData.defineId(AnomalyEntity.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Float> DATA_INTENSITY =
            SynchedEntityData.defineId(AnomalyEntity.class, EntityDataSerializers.FLOAT);

    public enum State {
        IDLE, ACTIVE, CHARGING, RESOLVING;

        private static final State[] VALUES = values();

        public static State byByte(byte b) {
            return (b < 0 || b >= VALUES.length) ? IDLE : VALUES[b];
        }
    }

    protected long cellId = UNSET_CELL;

    @OnlyIn(Dist.CLIENT)
    private SoundInstance ambientSound;

    protected AnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setPersistenceRequired();
        setNoGravity(true);
        setInvulnerable(true);
        this.xpReward = 0;
    }

    public abstract AnomalyType getAnomalyType();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1024.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, (byte) State.IDLE.ordinal());
        builder.define(DATA_INTENSITY, 1.0F);
    }

    public long getCellId() {
        return cellId;
    }

    public void setCellId(long cellId) {
        this.cellId = cellId;
    }

    public State getState() {
        return State.byByte(this.entityData.get(DATA_STATE));
    }

    public void setState(State state) {
        this.entityData.set(DATA_STATE, (byte) state.ordinal());
    }

    public float getIntensity() {
        return this.entityData.get(DATA_INTENSITY);
    }

    public void setIntensity(float intensity) {
        this.entityData.set(DATA_INTENSITY, intensity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean ignoreExplosion(net.minecraft.world.level.Explosion explosion) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("CellId", cellId);
        tag.putByte("AnomalyState", (byte) getState().ordinal());
        tag.putFloat("AnomalyIntensity", getIntensity());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CellId")) {
            cellId = tag.getLong("CellId");
        }
        if (tag.contains("AnomalyState")) {
            setState(State.byByte(tag.getByte("AnomalyState")));
        }
        if (tag.contains("AnomalyIntensity")) {
            setIntensity(tag.getFloat("AnomalyIntensity"));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            clientTick();
        } else {
            serverTick();
        }
    }

    protected void serverTick() {
        if (cellId != UNSET_CELL && onInterval(40)
                && AnomalySavedData.get((ServerLevel) level()).isDestroyed(cellId)) {
            discard();
            return;
        }
        setDeltaMovement(getDeltaMovement().x, 0.0D, getDeltaMovement().z);
        int surface = level().getHeight(Heightmap.Types.WORLD_SURFACE, getBlockX(), getBlockZ());
        double targetY = surface + 2.0D;
        if (Math.abs(getY() - targetY) > 0.01D) {
            setPos(getX(), targetY, getZ());
        }
        applyEffects();
        if (onInterval(counterplayInterval()) && checkCounterplay()) {
            onCounterplayResolved();
        }
    }

    protected void clientTick() {
        spawnAmbientParticles();
        tickAmbientSound();
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAmbientSound() {
        if (ambientSound != null && Minecraft.getInstance().getSoundManager().isActive(ambientSound)) {
            return;
        }
        if (!onInterval(20)) {
            return;
        }
        SoundEvent sound = NCSounds.getAnomalySound(getAnomalyType());
        if (sound != null) {
            ambientSound = new AnomalyAmbientSound(this, sound, ambientSoundVolume());
            Minecraft.getInstance().getSoundManager().play(ambientSound);
        }
    }

    protected float ambientSoundVolume() {
        return 0.4F;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (level().isClientSide && ambientSound != null) {
            Minecraft.getInstance().getSoundManager().stop(ambientSound);
            ambientSound = null;
        }
        super.remove(reason);
    }

    protected abstract void applyEffects();

    protected void spawnAmbientParticles() {
        double radius = ambientParticleRadius();
        for (int i = 0; i < 6; i++) {
            double a = random.nextDouble() * Math.PI * 2.0D;
            double r = random.nextDouble() * radius;
            double ox = Math.cos(a) * r;
            double oz = Math.sin(a) * r;
            double oy = (random.nextDouble() - 0.3D) * radius;
            level().addParticle(getAnomalyType().ambientParticle(),
                    getX() + ox, getY() + oy, getZ() + oz,
                    -ox * 0.02D, 0.01D, -oz * 0.02D);
        }
    }

    protected double ambientParticleRadius() {
        return 2.0D;
    }

    protected abstract boolean checkCounterplay();

    protected int counterplayInterval() {
        return 10;
    }

    protected void onCounterplayResolved() {
        if (level().isClientSide) {
            return;
        }
        if (cellId != UNSET_CELL) {
            AnomalySavedData.get((ServerLevel) level()).markDestroyed(cellId);
        }
        dropShards();
        playResolveFx();
        discard();
    }

    protected void dropShards() {
        if (!Common.ANOMALY_CONFIG.SHARD_DROPS_ENABLED.get()
                || random.nextDouble() > Common.ANOMALY_CONFIG.SHARD_DROP_CHANCE.get()) {
            return;
        }
        int min = Common.ANOMALY_CONFIG.SHARD_DROP_MIN.get();
        int max = Math.max(min, Common.ANOMALY_CONFIG.SHARD_DROP_MAX.get());
        int count = min + (max > min ? random.nextInt(max - min + 1) : 0);
        if (count <= 0) {
            return;
        }
        try {
            ItemStack shardStack = new ItemStack(ModEntries.get("resonite_shard").item().get(), count);
            ItemEntity item = new ItemEntity(level(), getX(), getY() + 0.5D, getZ(), shardStack);
            item.setInvulnerable(true);
            item.setDefaultPickUpDelay();
            level().addFreshEntity(item);
        } catch (Exception ignored) {
        }
    }

    public void onQ36Hit(Entity source) {
        if (!level().isClientSide && acceptsQ36Counterplay()) {
            onCounterplayResolved();
        }
    }

    protected boolean acceptsQ36Counterplay() {
        return false;
    }

    protected void playResolveFx() {
        level().broadcastEntityEvent(this, EVENT_RESOLVE);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_RESOLVE) {
            for (int i = 0; i < 40; i++) {
                double ox = (random.nextDouble() - 0.5D) * 3.0D;
                double oy = (random.nextDouble() - 0.5D) * 3.0D;
                double oz = (random.nextDouble() - 0.5D) * 3.0D;
                level().addParticle(getAnomalyType().ambientParticle(),
                        getX() + ox, getY() + oy, getZ() + oz,
                        ox * 0.1D, oy * 0.1D, oz * 0.1D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    protected boolean onInterval(int period) {
        if (period <= 1) {
            return true;
        }
        return Math.floorMod(tickCount + getId(), period) == 0;
    }

    protected AABB effectBox(double radius) {
        return AABB.ofSize(position(), radius * 2.0D, radius * 2.0D, radius * 2.0D);
    }

    protected List<LivingEntity> livingInRadius(double radius) {
        double r2 = radius * radius;
        Vec3 center = position();
        return level().getEntitiesOfClass(LivingEntity.class, effectBox(radius),
                e -> e != this && !(e instanceof AnomalyEntity) && e.isAlive()
                        && e.position().distanceToSqr(center) <= r2);
    }
}
