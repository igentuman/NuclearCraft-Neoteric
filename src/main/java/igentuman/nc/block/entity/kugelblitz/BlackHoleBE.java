package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

import static igentuman.nc.block.kugelblitz.BlackHoleBlock.ACTIVE;
import static igentuman.nc.client.renderer.DistortShader.blackhole;
import static igentuman.nc.setup.registration.NCSounds.BLACKHOLE_IDLE;
import static igentuman.nc.setup.registration.NCSounds.BLACKHOLE_SPAWN;
import static net.minecraft.world.level.block.Blocks.AIR;

public class BlackHoleBE extends NuclearCraftBE {

    public static String NAME = "black_hole";
    public float scale = 0.3f;
    public static long MIN_MASS =    100_000_000L;
    public static long MAX_MASS = 10_000_000_000L;
    @NBTField
    public boolean isInitialized = false;
    @NBTField
    public int initDelay = 0;
    public int spawnDelay = 0;

    public BlackHoleBE(BlockPos pPos, BlockState pBlockState) {
        super(KugelblitzRegistration.KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
    }

    public void tickClient() {
        if(isRemoved()) {
            blackhole.remove(getBlockPos());
        }
        if (!blackhole.contains(getBlockPos())) {
            blackhole.add(getBlockPos());
        }
        scale = 0.3f + spawnDelay/100f;
        if (spawnDelay < 1) {
            playSound(BLACKHOLE_IDLE, 0.7f);
        }
        if (spawnDelay > 0) {
            spawnDelay--;
            return;
        }
        if(getLevel().random.nextBoolean()) {
            for (int i = 0; i < getLevel().random.nextInt(3); i++) {
                float x = getBlockPos().getX() + 0.5F + randomDistance();
                float y = getBlockPos().getY() + 0.5F + randomDistance();
                float z = getBlockPos().getZ() + 0.5F + randomDistance();
                level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, 0, 0, 0);
            }
        }
        if (!isInitialized) {
            isInitialized = true;
            spawnDelay = 18;
            getLevel().playLocalSound(
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    BLACKHOLE_SPAWN.get(),
                    SoundSource.BLOCKS,
                    0.9F,
                    1.0F,
                    false
            );
            //burst dragon breath particles in a sphere
            for (int i = 0; i < 80; i++) {
                float x = getBlockPos().getX() + 0.5F + randomDistance();
                float y = getBlockPos().getY() + 0.5F + randomDistance();
                float z = getBlockPos().getZ() + 0.5F + randomDistance();
                level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, 0, 0, 0);
            }
        }
    }

    private float randomDistance() {
        return level.random.nextFloat() + level.random.nextInt(8) - 4;
    }

    public void tickServer() {
        if(isRemoved()) return;
        if (!isInitialized) {
            isInitialized = true;
            setChanged();
            initDelay = 5;
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, true));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, true), Block.UPDATE_ALL);
        }
        int wasDelay = initDelay;
        if (initDelay > 0) {
            initDelay--;
            if (initDelay == 1) {
                setChanged();
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, false));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, false), Block.UPDATE_ALL);
            }
        }

        if(wasDelay != initDelay) {
            setChanged();
        }
        handleClosestEntities();
    }

    private void handleClosestEntities() {
        if (getLevel().getGameTime() % 2 != 0) return;
        double radius = 5.0;

        double centerX = worldPosition.getX() + 0.5;
        double centerY = worldPosition.getY() + 0.5;
        double centerZ = worldPosition.getZ() + 0.5;

        AABB boundingBox = new AABB(
                centerX - radius, centerY - radius, centerZ - radius,
                centerX + radius, centerY + radius, centerZ + radius
        );

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);

        for (Entity entity : entities) {
            double dx = centerX - entity.getX();
            double dy = centerY - entity.getY();
            double dz = centerZ - entity.getZ();

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance < 0.1) continue;

            double factor = 1.0 / distance;
            dx *= factor;
            dy *= factor;
            dz *= factor;

            double pullStrength = (1.0 - distance / radius) * 10f;

            entity.setDeltaMovement(
                    entity.getDeltaMovement().x + dx * pullStrength,
                    entity.getDeltaMovement().y + dy * pullStrength,
                    entity.getDeltaMovement().z + dz * pullStrength
            );

            if (entity instanceof ItemEntity) {
                if (distance < 2) {
                    entity.discard();
                }
            } else if (entity instanceof LivingEntity livingEntity && distance < 2) {
                if(livingEntity.getHealth() < 1024) {
                    livingEntity.discard();
                } else {
                    livingEntity.hurt(level.damageSources().magic(), 1024.0F);
                }
            }
        }
    }

    public float getBlackholeScale() {
        return scale;
    }

    public void meltdown() {
        getLevel().explode(null,  getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 10, Level.ExplosionInteraction.TNT);
        setRemoved();
        getLevel().setBlockAndUpdate(getBlockPos(), AIR.defaultBlockState());
    }

    public void evaporate() {
        setRemoved();
        getLevel().setBlockAndUpdate(getBlockPos(), AIR.defaultBlockState());
    }
}
