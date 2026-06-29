package igentuman.nc.block_entity.fission;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReaction;
import igentuman.nc.multiblock.fission.FissionReactorCache;
import igentuman.nc.multiblock.fission.HeatBuffer;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Controller block entity for the fission reactor. The structure validator (off-thread) produces
 * stats into the {@link FissionReactorCache}; this BE runs the {@link FissionReaction} on the main
 * thread each tick when formed, so all world/buffer mutation stays on the main thread.
 */
public class FissionReactorControllerBE extends MultiblockControllerBE {

    private final HeatBuffer heatBuffer = new HeatBuffer();
    private final FissionReaction reaction = new FissionReaction();

    @NBTField(syncToClient = true) public int heat;
    @NBTField(syncToClient = true) public int maxHeat;
    @NBTField(syncToClient = true) public int energyPerTick;
    @NBTField(syncToClient = true) public int reactivity;
    @NBTField(syncToClient = true) public int cooling;
    @NBTField(syncToClient = true) public int netHeat;

    public FissionReactorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public HeatBuffer heatBuffer() {
        return heatBuffer;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        MultiblockHandler.submitTick(serverLevel, worldPosition);
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(serverLevel, worldPosition);
        boolean newFormed = instance != null && instance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (formed && instance.cache instanceof FissionReactorCache fc) {
            reaction.tick(this, fc);
        } else {
            reaction.idle(this);
        }
        if (wasChanged) {
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag heatTag = new CompoundTag();
        heatBuffer.save(heatTag);
        tag.put("HeatBuffer", heatTag);
        CompoundTag reactionTag = new CompoundTag();
        reaction.save(reactionTag);
        tag.put("Reaction", reactionTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("HeatBuffer")) heatBuffer.load(tag.getCompound("HeatBuffer"));
        if (tag.contains("Reaction")) reaction.load(tag.getCompound("Reaction"));
    }
}
