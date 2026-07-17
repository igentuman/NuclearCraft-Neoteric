package igentuman.nc.entity.anomaly;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Flings living entities that wander too close to a random safe spot far away, and blinks itself
 * around within a small radius (hovering above the surface). Counterplay is the Q36 beam.
 */
public class TeleportingAnomalyEntity extends AnomalyEntity {

    private int blinkTimer = -1;
    private double anchorX;
    private double anchorZ;
    private boolean hasAnchor;

    public TeleportingAnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    /** Home position the self-blink stays within {@code TP_SELF_RADIUS} of. Set at spawn by the manager. */
    public void setAnchor(double x, double z) {
        this.anchorX = x;
        this.anchorZ = z;
        this.hasAnchor = true;
    }

    @Override
    public AnomalyType getAnomalyType() {
        return AnomalyType.TELEPORTING;
    }

    @Override
    protected double ambientParticleRadius() {
        return 2.0D;
    }

    @Override
    protected void applyEffects() {
        ServerLevel serverLevel = (ServerLevel) level();
        if (!hasAnchor) {
            setAnchor(getX(), getZ());
        }
        if (onInterval(10)) {
            for (LivingEntity victim : livingInRadius(ANOMALY_CONFIG.TP_VICTIM_RADIUS.get())) {
                teleportVictim(serverLevel, victim);
            }
        }
        if (blinkTimer < 0) {
            blinkTimer = nextBlinkDelay();
        }
        if (--blinkTimer <= 0) {
            blinkTimer = nextBlinkDelay();
            selfBlink(serverLevel);
        }
    }

    private int nextBlinkDelay() {
        int min = ANOMALY_CONFIG.TP_SELF_MIN_TICKS.get();
        int max = Math.max(min, ANOMALY_CONFIG.TP_SELF_MAX_TICKS.get());
        return min + random.nextInt(max - min + 1);
    }

    private void teleportVictim(ServerLevel serverLevel, LivingEntity victim) {
        int maxDist = ANOMALY_CONFIG.TP_MAX_DISTANCE.get();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = 32.0D + random.nextDouble() * (maxDist - 32.0D);
            int tx = (int) (getX() + Math.cos(angle) * dist);
            int tz = (int) (getZ() + Math.sin(angle) * dist);
            if (!serverLevel.hasChunkAt(new BlockPos(tx, 0, tz))) {
                continue;
            }
            int ty = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz)+serverLevel.random.nextInt(14);
            BlockPos dest = new BlockPos(tx, ty, tz);
            if (isSafeDestination(serverLevel, dest)) {
                victim.teleportTo(tx + 0.5D, ty, tz + 0.5D);
                serverLevel.playSound(null, dest, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
                return;
            }
        }
    }

    private boolean isSafeDestination(ServerLevel serverLevel, BlockPos dest) {
        if (dest.getY() <= serverLevel.getMinBuildHeight() + 1 || dest.getY() >= serverLevel.getMaxBuildHeight() - 2) {
            return false;
        }
        if (!serverLevel.getFluidState(dest).is(FluidTags.LAVA)
                && !serverLevel.getFluidState(dest.below()).is(FluidTags.LAVA)) {
            return serverLevel.getBlockState(dest).getCollisionShape(serverLevel, dest).isEmpty()
                    && serverLevel.getBlockState(dest.above()).getCollisionShape(serverLevel, dest.above()).isEmpty();
        }
        return false;
    }

    private void selfBlink(ServerLevel serverLevel) {
        double radius = ANOMALY_CONFIG.TP_SELF_RADIUS.get();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double dist = radius * (0.5D + random.nextDouble() * 0.5D);
        int nx = (int) (anchorX + Math.cos(angle) * dist);
        int nz = (int) (anchorZ + Math.sin(angle) * dist);
        if (!serverLevel.hasChunkAt(new BlockPos(nx, 0, nz))) {
            return;
        }
        int surface = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, nx, nz);
        int ny = surface + ANOMALY_CONFIG.TP_HOVER_OFFSET.get();
        serverLevel.broadcastEntityEvent(this, EVENT_RESOLVE);
        teleportTo(nx + 0.5D, ny, nz + 0.5D);
    }

    @Override
    protected boolean checkCounterplay() {
        return false;
    }

    @Override
    protected boolean acceptsQ36Counterplay() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BlinkTimer", blinkTimer);
        if (hasAnchor) {
            tag.putDouble("AnchorX", anchorX);
            tag.putDouble("AnchorZ", anchorZ);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BlinkTimer")) {
            blinkTimer = tag.getInt("BlinkTimer");
        }
        if (tag.contains("AnchorX") && tag.contains("AnchorZ")) {
            setAnchor(tag.getDouble("AnchorX"), tag.getDouble("AnchorZ"));
        }
    }
}
