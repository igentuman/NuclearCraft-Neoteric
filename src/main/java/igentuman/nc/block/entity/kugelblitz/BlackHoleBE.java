package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.setup.registration.NCSounds;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlackHoleBE extends ChamberBE {
    public static String NAME = "black_hole";

    @NBTField
    public boolean isInitialized = false;
    private int spawnSoundCooldown = 0;

    public BlackHoleBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public void tickClient() {
        if (spawnSoundCooldown > 1) {
            stopSound();
        }
        if (spawnSoundCooldown > 0) {
            spawnSoundCooldown--;
            return;
        }
        if (!isInitialized) {
            isInitialized = true;
            spawnSoundCooldown = 85;
            playSpawnSound();
        }
        playIdleSound();
    }

    protected void stopSound() {
        if (currentSound == null) return;
        SoundHandler.stopTileSound(getBlockPos());
        currentSound = null;
        playSoundCooldown = 0;
    }

    protected void playSpawnSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(NCSounds.BLACKHOLE_SPAWN.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(NCSounds.BLACKHOLE_SPAWN.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(NCSounds.BLACKHOLE_SPAWN.get(), SoundSource.BLOCKS, 0.6f, level.getRandom(), getBlockPos());
        }
    }

    protected void playIdleSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(NCSounds.BLACKHOLE_IDLE.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(NCSounds.BLACKHOLE_IDLE.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(NCSounds.BLACKHOLE_IDLE.get(), SoundSource.BLOCKS, 0.7f, level.getRandom(), getBlockPos());
        }
    }

    public void tickServer() {
        if (!isInitialized) {
            isInitialized = true;
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
}
