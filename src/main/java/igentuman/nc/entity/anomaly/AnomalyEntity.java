package igentuman.nc.entity.anomaly;

import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.NCSounds;
import igentuman.nc.world.anomaly.AnomalySavedData;
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

import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Abstract base for all STALKER-style anomalies. Anomalies are stationary, deterministic environmental
 * hazards confined to the Wasteland dimension. They extend {@link Mob} purely for the free
 * {@link SynchedEntityData}, NBT hooks and attribute supplier; all vanilla AI / damage / despawn paths
 * are neutralised below. Removal happens only through the variant-specific counterplay
 * ({@link #checkCounterplay()} or {@link #onQ36Hit(Entity)}), never through {@link #hurt}.
 */
public abstract class AnomalyEntity extends Mob {

    /** Sentinel for "no placement cell assigned yet". */
    public static final long UNSET_CELL = Long.MIN_VALUE;

    /** Entity event byte broadcast to clients when an anomaly is resolved, to play the dissipation FX. */
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

    /** Deterministic placement cell this anomaly belongs to (identity / dedup / destroyed-set key). */
    protected long cellId = UNSET_CELL;

    /** Client-only looping ambient sound handle; never touched server-side. */
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, (byte) State.IDLE.ordinal());
        this.entityData.define(DATA_INTENSITY, 1.0F);
    }

    // region identity / synced state

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

    // endregion

    // region invulnerability / persistence — "cannot be destroyed by attacks or explosions"

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
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

    // endregion

    // region tick

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            clientTick();
        } else {
            serverTick();
        }
    }

    protected void serverTick() {
        if (cellId != UNSET_CELL && onInterval(40)
                && AnomalySavedData.get((ServerLevel) level).isDestroyed(cellId)) {
            discard();
            return;
        }
        setDeltaMovement(getDeltaMovement().x, 0.0D, getDeltaMovement().z);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, getBlockX(), getBlockZ());
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

    /** Lazily (re)starts the variant's looping ambient sound while the entity is alive and in range. */
    private void tickAmbientSound() {
        if (SoundHandler.isActive(ambientSound)) {
            return;
        }
        if (!onInterval(20)) {
            return;
        }
        SoundEvent sound = NCSounds.getAnomalySound(getAnomalyType());
        if (sound != null) {
            ambientSound = SoundHandler.startAnomalySound(this, sound, ambientSoundVolume());
        }
    }

    protected float ambientSoundVolume() {
        return 1.2F;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (level.isClientSide && ambientSound != null) {
            SoundHandler.stopSound(ambientSound);
            ambientSound = null;
        }
        super.remove(reason);
    }

    /** Server-side, variant-specific environmental effect. Subclasses throttle via {@link #onInterval(int)}. */
    protected abstract void applyEffects();

    /** Client-side ambient FX. Default emits a slow swirl of the variant's ambient particle. */
    protected void spawnAmbientParticles() {
        double radius = ambientParticleRadius();
        for (int i = 0; i < 6; i++) {
            double a = random.nextDouble() * Math.PI * 2.0D;
            double r = random.nextDouble() * radius;
            double ox = Math.cos(a) * r;
            double oz = Math.sin(a) * r;
            double oy = (random.nextDouble() - 0.3D) * radius;
            level.addParticle(getAnomalyType().ambientParticle(),
                    getX() + ox, getY() + oy, getZ() + oz,
                    -ox * 0.02D, 0.01D, -oz * 0.02D);
        }
    }

    protected double ambientParticleRadius() {
        return 2.0D;
    }

    // endregion

    // region counterplay

    /** Server-side world scan polled on {@link #counterplayInterval()}; {@code true} => resolved. */
    protected abstract boolean checkCounterplay();

    protected int counterplayInterval() {
        return 10;
    }

    /** Default resolution: record the destroyed cell, play FX, and remove the entity. */
    protected void onCounterplayResolved() {
        if (level.isClientSide) {
            return;
        }
        if (cellId != UNSET_CELL) {
            AnomalySavedData.get((ServerLevel) level).markDestroyed(cellId);
        }
        dropShards();
        playResolveFx();
        discard();
    }

    /**
     * Spawns resonite shards as the anomaly resolves. Invoked only from {@link #onCounterplayResolved()},
     * never from the destroyed-cell cleanup discard in {@link #serverTick()}, so a deduped reload never
     * spits out loot. The item is made invulnerable so a gravitational collapse blast or burning fire
     * can't consume the reward.
     */
    protected void dropShards() {
        if (!ANOMALY_CONFIG.SHARD_DROPS_ENABLED.get()
                || random.nextDouble() > ANOMALY_CONFIG.SHARD_DROP_CHANCE.get()) {
            return;
        }
        int min = ANOMALY_CONFIG.SHARD_DROP_MIN.get();
        int max = Math.max(min, ANOMALY_CONFIG.SHARD_DROP_MAX.get());
        int count = min + (max > min ? random.nextInt(max - min + 1) : 0);
        if (count <= 0) {
            return;
        }
        ItemEntity item = new ItemEntity(level, getX(), getY() + 0.5D, getZ(),
                new ItemStack(NCItems.RESONITE_SHARD.get(), count));
        item.setInvulnerable(true);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    /** Called directly from the Q36 beam / pulse hit path (bypasses the {@code mobProjectile} ambiguity). */
    public void onQ36Hit(Entity source) {
        if (!level.isClientSide && acceptsQ36Counterplay()) {
            onCounterplayResolved();
        }
    }

    protected boolean acceptsQ36Counterplay() {
        return false;
    }

    protected void playResolveFx() {
        level.broadcastEntityEvent(this, EVENT_RESOLVE);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_RESOLVE) {
            for (int i = 0; i < 40; i++) {
                double ox = (random.nextDouble() - 0.5D) * 3.0D;
                double oy = (random.nextDouble() - 0.5D) * 3.0D;
                double oz = (random.nextDouble() - 0.5D) * 3.0D;
                level.addParticle(getAnomalyType().ambientParticle(),
                        getX() + ox, getY() + oy, getZ() + oz,
                        ox * 0.1D, oy * 0.1D, oz * 0.1D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // endregion

    // region helpers

    /** Staggered interval check: spreads scan load across loaded anomalies using the entity id. */
    protected boolean onInterval(int period) {
        if (period <= 1) {
            return true;
        }
        return Math.floorMod(tickCount + getId(), period) == 0;
    }

    protected AABB effectBox(double radius) {
        return AABB.ofSize(position(), radius * 2.0D, radius * 2.0D, radius * 2.0D);
    }

    /** Living entities within {@code radius}, excluding this anomaly and any other anomaly. */
    protected List<LivingEntity> livingInRadius(double radius) {
        double r2 = radius * radius;
        Vec3 center = position();
        return level.getEntitiesOfClass(LivingEntity.class, effectBox(radius),
                e -> e != this && !(e instanceof AnomalyEntity) && e.isAlive()
                        && e.position().distanceToSqr(center) <= r2);
    }

    // endregion
}
