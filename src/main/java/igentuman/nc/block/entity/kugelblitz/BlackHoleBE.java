package igentuman.nc.block.entity.kugelblitz;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.setup.registration.NCSounds.BLACKHOLE_IDLE;
import static igentuman.nc.setup.registration.NCSounds.BLACKHOLE_SPAWN;

public class BlackHoleBE extends NuclearCraftBE {

    public static String NAME = "black_hole";
    public float scale = 0.3f;

    @NBTField
    public boolean isInitialized = false;
    private int spawnSoundCooldown = 0;

    public BlackHoleBE(BlockPos pPos, BlockState pBlockState) {
        super(KugelblitzRegistration.KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
    }

    public void tickClient() {
        if (spawnSoundCooldown > 1) {
            stopSound();
            scale = 0.3f;
        }
        if (spawnSoundCooldown > 0) {
            spawnSoundCooldown--;
            scale = Math.max(0.29f, Math.min(0.31f, getLevel().getRandom().nextFloat()));
            return;
        }
        if (!isInitialized) {
            isInitialized = true;
            spawnSoundCooldown = 28;
            playSound(BLACKHOLE_SPAWN, 0.8f);
        }
        playSound(BLACKHOLE_IDLE, 0.7f);
    }

    public void tickServer() {
        if (!isInitialized) {
            isInitialized = true;
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public float getBlackholeScale() {
        return scale;
    }
}
