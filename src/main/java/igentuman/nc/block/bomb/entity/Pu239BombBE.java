package igentuman.nc.block.bomb.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;

import static igentuman.nc.setup.registration.Entities.PRIMED_FISSION_BOMB;
import static igentuman.nc.setup.registration.NCBlocks.PU_239_BOMB_BE;

public class Pu239BombBE extends NuclearCraftBE {

    @NBTField
    public boolean armed = false;

    @NBTField
    public int fuseTicks = 60;

    @NBTField
    public String placerUuid = "";

    @NBTField
    public boolean placedChecked = false;

    private boolean chunkForced = false;

    public Pu239BombBE(BlockPos pos, BlockState state) {
        super(PU_239_BOMB_BE.get(), pos, state);
    }

    public void arm() {
        if (armed) return;
        armed = true;
        fuseTicks = CommonConfig.BOMB_CONFIG.FUSE_TICKS.get();
        forceChunk(true);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void tickServer() {
        if (level == null) return;
        if (!placedChecked) {
            placedChecked = true;
            if (placerUuid == null || placerUuid.isEmpty()) {
                Block.popResource(level, getBlockPos(), new net.minecraft.world.item.ItemStack(getBlockState().getBlock()));
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlock(getBlockPos(), false);
                }
                return;
            }
            setChanged();
        }
        if (!armed) return;
        if (!chunkForced) forceChunk(true);
        fuseTicks--;
        if (fuseTicks <= 0) {
            detonate(level, getBlockPos());
        }
    }

    private void forceChunk(boolean force) {
        if (!(level instanceof ServerLevel server)) return;
        ChunkPos cp = new ChunkPos(getBlockPos());
        boolean result = ForgeChunkManager.forceChunk(server, NuclearCraft.MODID, getBlockPos(), cp.x, cp.z, force, true);
        chunkForced = force;
        NuclearCraft.LOGGER.info("[Bomb] forceChunk({}, {}, {}) = {} pos={}", cp.x, cp.z, force, result, getBlockPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (armed && !chunkForced) forceChunk(true);
    }

    @Override
    public void setRemoved() {
        if (chunkForced) forceChunk(false);
        super.setRemoved();
    }

    private void detonate(Level lvl, BlockPos epicenter) {
        if (lvl instanceof ServerLevel server) {
            PrimedFissionBombEntity primed = PRIMED_FISSION_BOMB.get().create(server);
            if (primed != null) {
                primed.setPos(epicenter.getX() + 0.5, epicenter.getY() + 0.5, epicenter.getZ() + 0.5);
                int hR = (int) (double)CommonConfig.BOMB_CONFIG.RADIUS_HORIZONTAL.get();
                int vR = (int) (double)CommonConfig.BOMB_CONFIG.RADIUS_VERTICAL.get();
                primed.configure(1.0f, hR, vR);
                primed.setPlacerUuid(this.placerUuid);
                primed.preForceEpicenter();
                server.addFreshEntity(primed);
            }
        }
        lvl.setBlock(epicenter, Blocks.AIR.defaultBlockState(), 3);
    }
}
