package igentuman.nc.block_entity.bomb;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.entity.PrimedFissionBombEntity;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.setup.entries.Bomb.PRIMED_FISSION_BOMB;
import static igentuman.nc.setup.entries.Bomb.TICKET_CONTROLLER;


public class Pu239BombBE extends GlobalBlockEntity {

    @NBTField
    public boolean armed = false;

    @NBTField
    public int fuseTicks = igentuman.nc.config.Bomb.FUSE_TICKS.get();

    @NBTField
    public String placerUuid = "";

    @NBTField(syncToClient = true)
    public boolean placedChecked = false;

    private boolean chunkForced = false;

    public Pu239BombBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public void arm() {
        if (armed) return;
        armed = true;
        fuseTicks = igentuman.nc.config.Bomb.FUSE_TICKS.get();
        forceChunk(true);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide) return;

        if (!placedChecked) {
            placedChecked = true;
            if (placerUuid.isEmpty()) {
                Block.popResource(level, getBlockPos(), new net.minecraft.world.item.ItemStack(getBlockState().getBlock()));
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlock(getBlockPos(), false, null, 512);
                }
                return;
            }
            setChanged();
        }

        if (!armed) return;
        if (!chunkForced) forceChunk(true);

        fuseTicks--;
        setChanged();
        if (fuseTicks <= 0) {
            detonate(level, getBlockPos());
            return;
        }

        super.serverTick();
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

    public void clientTick() {
    }

    private void forceChunk(boolean force) {
        if (!(level instanceof ServerLevel server) || isRemoved()) return;
        ChunkPos cp = new ChunkPos(getBlockPos());
        try {
            boolean result = TICKET_CONTROLLER.forceChunk(server, getBlockPos(), cp.x, cp.z, force, true);
            chunkForced = force;
            NuclearCraft.LOGGER.info("[Bomb] forceChunk({}, {}, {}) = {} pos={}", cp.x, cp.z, force, result, getBlockPos());
        } catch (IllegalArgumentException e) {
            NuclearCraft.LOGGER.warn("[Bomb] ticket controller not registered; skipping forceChunk for {}", getBlockPos());
        }
    }

    private void detonate(Level lvl, BlockPos epicenter) {
        if (!(lvl instanceof ServerLevel server)) {
            lvl.setBlock(epicenter, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            return;
        }
        EntityType<PrimedFissionBombEntity> type = PRIMED_FISSION_BOMB.get();
        PrimedFissionBombEntity primed = type.create(server);
        if (primed != null) {
            primed.setPos(epicenter.getX() + 0.5, epicenter.getY() + 0.5, epicenter.getZ() + 0.5);
            int hR = Math.max(16, igentuman.nc.config.Bomb.RADIUS_HORIZONTAL.get());
            int vR = Math.max(8, igentuman.nc.config.Bomb.RADIUS_VERTICAL.get());
            primed.configure(1.0f, hR, vR);
            primed.setPlacerUuid(this.placerUuid);
            primed.preForceEpicenter();
            server.addFreshEntity(primed);
        }
        lvl.setBlock(epicenter, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
}
