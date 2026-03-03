package igentuman.nc.block.kugelblitz.entity;

import igentuman.nc.block.entity.NuclearCraftBE;

import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.block.kugelblitz.BlackHoleBlock.ACTIVE;
import static igentuman.nc.client.renderer.DistortShader.blackhole;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
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
    private boolean hasEntities = false;
    public BlackHoleBE(BlockPos pPos, BlockState pBlockState) {
        super(KugelblitzRegistration.KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
    }

    public void setRemoved() {
        super.setRemoved();
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
        //Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        if (!isInitialized) {
            isInitialized = true;
            setChanged();
            initDelay = 5;
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, true));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, true), Block.UPDATE_NEIGHBORS);
        }
        int wasDelay = initDelay;
        if (initDelay > 0) {
            initDelay--;
            if (initDelay == 1) {
                setChanged();
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, false));
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, false), Block.UPDATE_NEIGHBORS);
            }
        }

        if(wasDelay != initDelay) {
            setChanged();
        }
        handleClosestEntities();
        evaporateClosestBlocks();
        evaporateIfNotInsideChamber();
    }

    private void evaporateIfNotInsideChamber() {
        for(Direction dir: Direction.values()) {
            BlockPos pos = worldPosition.relative(dir, 5);
            BlockState state = level.getBlockState(pos);
            if(!state.is(KUGELBLITZ_BLOCKS.get("photon_concentrator").get())) {
                evaporate();
                return;
            }
        }
    }

    private void evaporateClosestBlocks() {
        if((currentTick & 5) == 0) {
            for (Direction direction : Direction.values()) {
                if(!level.getBlockState(worldPosition.relative(direction)).isAir()) {
                    level.setBlock(worldPosition.relative(direction), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }


    private void handleClosestEntities() {
        if(!hasEntities && currentTick % 10 != 0) return;
        double radius = 5.0;
        double consumeRadius = 1.1;

        double centerX = worldPosition.getX() + 0.5;
        double centerY = worldPosition.getY() + 0.5;
        double centerZ = worldPosition.getZ() + 0.5;

        AABB boundingBox = new AABB(
                centerX - radius, centerY - radius, centerZ - radius,
                centerX + radius, centerY + radius, centerZ + radius
        );

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

            // Normalize direction vector
            double factor = 1.0 / distance;
            dx *= factor;
            dy *= factor;
            dz *= factor;

            double pullStrength = Math.min(3.0, 0.8 * (1.0 - Math.pow(distance / radius, 2)));
            
            if (entity instanceof ItemEntity) {
                pullStrength *= 1.5;
            }
            
            entity.setDeltaMovement(
                    entity.getDeltaMovement().x + dx * pullStrength,
                    entity.getDeltaMovement().y + dy * pullStrength,
                    entity.getDeltaMovement().z + dz * pullStrength
            );
            
            double maxSpeed = 1.0;
            double currentSpeed = entity.getDeltaMovement().length();
            if (currentSpeed > maxSpeed) {
                entity.setDeltaMovement(
                        entity.getDeltaMovement().scale(maxSpeed / currentSpeed)
                );
            }

            if (distance < consumeRadius) {
                consumeEntity(entity);
            }
        }
    }
    
    /**
     * Consumes an entity that has been pulled into the black hole
     */
    private void consumeEntity(Entity entity) {
        if (entity instanceof ItemEntity) {
            entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        } else if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof ServerPlayer) {
                PlayerEnterBlackholeEvent event = new PlayerEnterBlackholeEvent((ServerPlayer) livingEntity, getBlockPos(), getLevel());
                NeoForge.EVENT_BUS.post(event);
                // TODO: KubeJS integration removed — waiting on KubeJS to port to NeoForge 1.21.1. Re-enable NCKubeJsEvents.onPlayerEnterBlackhole(event) when available.
                if(event.isCanceled()) return;
                entity.kill();

            } else {
                entity.kill();
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


    public class PlayerEnterBlackholeEvent extends Event implements net.neoforged.bus.api.ICancellableEvent {

        public ServerPlayer getPlayer() {
            return player;
        }

        public BlockPos getBlackholePos() {
            return blackholePos;
        }

        public Level getLevel() {
            return level;
        }

        private final ServerPlayer player;
        private final BlockPos blackholePos;
        private final Level level;

        public PlayerEnterBlackholeEvent(ServerPlayer player, BlockPos blackholePos, Level level) {
            this.player = player;
            this.blackholePos = blackholePos;
            this.level = level;
        }

    }
}
