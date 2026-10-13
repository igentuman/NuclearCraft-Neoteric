package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlackHoleBE extends GlobalBlockEntity {

    public static final long MIN_MASS = 100_000_000L;
    public static final long MAX_MASS = 10_000_000_000L;

    public float scale = 0.3f;
    @NBTField
    public boolean isInitialized = false;
    public int spawnDelay = 0;
    @NBTField(syncToClient = true)
    public long formationStartTime = Long.MIN_VALUE;
    private boolean hasEntities = false;
    private long lastTickTime = -1;

    public BlackHoleBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (isRemoved() || level == null) return;
        if (lastTickTime == level.getGameTime()) return;
        lastTickTime = level.getGameTime();
        if (formationStartTime == Long.MIN_VALUE) {
            // Existing saves start settled; newly formed holes animate for 50 ticks.
            formationStartTime = level.getGameTime() - (isInitialized ? 50 : 0);
            isInitialized = true;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        handleClosestEntities();
        evaporateClosestBlocks();
        evaporateIfNotInsideChamber();
    }

    @Override
    public void clientTick() {
        if (level == null) return;
        scale = 0.3f + spawnDelay / 100f;
        if (spawnDelay < 1) {
            playSound(NCSounds.BLACKHOLE_IDLE.get(), 0.7f);
        }
        if (spawnDelay > 0) {
            spawnDelay--;
            return;
        }
        if (level.random.nextBoolean()) {
            for (int i = 0; i < level.random.nextInt(3); i++) {
                spawnParticle();
            }
        }
        if (!isInitialized) {
            isInitialized = true;
            spawnDelay = 18;
            level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                    NCSounds.BLACKHOLE_SPAWN.get(), SoundSource.BLOCKS, 0.9F, 1.0F, false);
            for (int i = 0; i < 80; i++) {
                spawnParticle();
            }
        }
    }

    private void spawnParticle() {
        float x = worldPosition.getX() + 0.5F + randomDistance();
        float y = worldPosition.getY() + 0.5F + randomDistance();
        float z = worldPosition.getZ() + 0.5F + randomDistance();
        level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, 0, 0, 0);
    }

    private float randomDistance() {
        return level.random.nextFloat() + level.random.nextInt(8) - 4;
    }

    public float getFormationAge(float partialTick) {
        if (level == null || formationStartTime == Long.MIN_VALUE) return 2.5F;
        return Math.max(0F, (level.getGameTime() - formationStartTime + partialTick) / 20F);
    }

    private void evaporateIfNotInsideChamber() {
        for (Direction dir : Direction.values()) {
            BlockPos pos = worldPosition.relative(dir, 5);
            if (!level.getBlockState(pos).is(ModEntries.get("photon_concentrator").block().get())) {
                evaporate();
                return;
            }
        }
    }

    private void evaporateClosestBlocks() {
        if ((level.getGameTime() & 5) == 0) {
            for (Direction direction : Direction.values()) {
                if (!level.getBlockState(worldPosition.relative(direction)).isAir()) {
                    level.setBlock(worldPosition.relative(direction), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private void handleClosestEntities() {
        if (!hasEntities && level.getGameTime() % 10 != 0) return;
        double radius = 5.0;
        double consumeRadius = 1.1;
        double centerX = worldPosition.getX() + 0.5;
        double centerY = worldPosition.getY() + 0.5;
        double centerZ = worldPosition.getZ() + 0.5;

        AABB boundingBox = new AABB(centerX - radius, centerY - radius, centerZ - radius,
                centerX + radius, centerY + radius, centerZ + radius);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);
        hasEntities = !entities.isEmpty();

        for (Entity entity : entities) {
            double dx = centerX - entity.getX();
            double dy = centerY - entity.getY();
            double dz = centerZ - entity.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 1.1) {
                consumeEntity(entity);
                continue;
            }
            double factor = 1.0 / distance;
            dx *= factor;
            dy *= factor;
            dz *= factor;
            double pullStrength = Math.min(3.0, 0.8 * (1.0 - Math.pow(distance / radius, 2)));
            if (entity instanceof ItemEntity) pullStrength *= 1.5;
            entity.setDeltaMovement(
                    entity.getDeltaMovement().x + dx * pullStrength,
                    entity.getDeltaMovement().y + dy * pullStrength,
                    entity.getDeltaMovement().z + dz * pullStrength);
            double maxSpeed = 1.0;
            double currentSpeed = entity.getDeltaMovement().length();
            if (currentSpeed > maxSpeed) {
                entity.setDeltaMovement(entity.getDeltaMovement().scale(maxSpeed / currentSpeed));
            }
            if (distance < consumeRadius) consumeEntity(entity);
        }
    }

    private void consumeEntity(Entity entity) {
        if (entity instanceof ItemEntity) {
            entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        } else if (entity instanceof LivingEntity) {
            entity.kill();
        }
    }

    public float getBlackholeScale() {
        return scale;
    }

    public void meltdown() {
        double radius = Multiblocks.kugelblitzExplosionRadius;
        if (radius > 0) {
            level.explode(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                    (float) radius, Level.ExplosionInteraction.TNT);
        }
        setRemoved();
        level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
    }

    public void evaporate() {
        setRemoved();
        level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
    }
}
