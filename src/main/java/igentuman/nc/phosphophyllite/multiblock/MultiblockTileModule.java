package igentuman.nc.phosphophyllite.multiblock;

import igentuman.nc.phosphophyllite.modular.api.IModularTile;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.phosphophyllite.modular.tile.IIsTickingTracker;
import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static igentuman.nc.phosphophyllite.multiblock2.IAssemblyStateBlock.ASSEMBLED;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockTileModule<
        TileType extends BlockEntity & IMultiblockTile<TileType, ControllerType>,
        ControllerType extends MultiblockController<TileType, ControllerType>
        > extends TileModule<TileType> implements IIsTickingTracker {
    
    private final boolean ASSEMBLY_STATE = iface.getBlockState().hasProperty(ASSEMBLED);
    
    protected ControllerType controller;
    @SuppressWarnings("unchecked") // its fine
    protected final MultiblockTileModule<TileType, ControllerType>[] neighbors = new MultiblockTileModule[6];
    protected final BlockEntity[] neighborTiles = new BlockEntity[6];
    
    public ControllerType controller() {
        return controller;
    }
    
    @Nullable
    public MultiblockTileModule<TileType, ControllerType> getNeighbor(Direction direction) {
        return neighbors[direction.get3DDataValue()];
    }
    
    void updateNeighbors() {
        if (controller == null) {
            return;
        }
        var pos = iface.getBlockPos();
        for (Direction value : Direction.values()) {
            neighbors[value.get3DDataValue()] = controller.blocks.getModule(pos.getX() + value.getStepX(), pos.getY() + value.getStepY(), pos.getZ() + value.getStepZ());
        }
        for (int i = 0; i < neighbors.length; i++) {
            MultiblockTileModule<TileType, ControllerType> neighbor = neighbors[i];
            if (neighbor != null) {
                neighbor.neighbors[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = this;
                neighbor.neighborTiles[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = iface;
            }
        }
    }
    
    void nullNeighbors() {
        for (int i = 0; i < neighbors.length; i++) {
            MultiblockTileModule<?, ?> neighbor = neighbors[i];
            if (neighbor != null) {
                neighbor.neighbors[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = null;
                neighbor.neighborTiles[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = null;
            }
        }
        
        for (Direction value : Direction.values()) {
            neighbors[value.get3DDataValue()] = null;
        }
    }
    
    @OnModLoad
    static void onModLoad() {
        ModuleRegistry.registerTileModule(IMultiblockTile.class, IMultiblockTile::createMultiblockModule);
    }
    
    public MultiblockTileModule(IModularTile blockEntity) {
        super(blockEntity);
    }
    
    @Override
    public String saveKey() {
        return "phosphophyllite_multiblock";
    }
    
    long lastSavedTick = 0;
    
    private boolean allowAttach = false;
    boolean isSaveDelegate = false;
    
    protected BlockState assembledBlockState(BlockState state) {
        if (ASSEMBLY_STATE) {
            state = state.setValue(ASSEMBLED, true);
        }
        return state;
    }
    
    protected BlockState disassembledBlockState(BlockState state) {
        if (ASSEMBLY_STATE) {
            state = state.setValue(ASSEMBLED, false);
        }
        return state;
    }
    
    private static Level lastLevel;
    private static ChunkAccess lastChunk;
    private static long lastChunkPos;
    
    public void attachToNeighbors() {
        assert iface.getLevel() != null;
        if (iface.getLevel().isClientSide) {
            return;
        }
        if (!allowAttach) {
            return;
        }
        final var pos = iface.getBlockPos();
        if (iface.getLevel().getBlockEntity(pos) != iface) {
            return;
        }
        if (ASSEMBLY_STATE) {
            final var level = iface.getLevel();
            final var chunkPos = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
            if (lastLevel != level || chunkPos != lastChunkPos) {
                lastLevel = iface.getLevel();
                lastChunkPos = chunkPos;
                lastChunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            }
            lastChunk.setBlockState(pos, iface.getBlockState().setValue(ASSEMBLED, false), false);
//            Util.setBlockStateWithoutUpdate(iface.getBlockPos(), iface.getBlockState().setValue(ASSEMBLED, false));
        }
        if (controller != null) {
            controller.detach(this, false, false, true);
            controller = null;
        }
        // at this point, i need to get or create a controller
        BlockPos.MutableBlockPos possibleTilePos = new BlockPos.MutableBlockPos();
        ChunkAccess lastChunk = null;
        long lastChunkPos = 0;
        for (Direction value : Direction.values()) {
            possibleTilePos.set(pos);
            possibleTilePos.move(value);
            long currentChunkPos = ChunkPos.asLong(possibleTilePos.getX() >> 4, possibleTilePos.getZ() >> 4);
            if (lastChunk == null || currentChunkPos != lastChunkPos) {
                lastChunkPos = currentChunkPos;
                lastChunk = iface.getLevel().getChunk(possibleTilePos.getX() >> 4, possibleTilePos.getZ() >> 4, ChunkStatus.FULL, false);
            }
            if (lastChunk != null) {
                BlockEntity possibleTile = lastChunk.getBlockEntity(possibleTilePos);
                if (possibleTile instanceof IMultiblockTile) {
                    if (((IMultiblockTile<?, ?>) possibleTile).multiblockModule().controller != null) {
                        ((IMultiblockTile<?, ?>) possibleTile).multiblockModule().controller.attemptAttach(this);
                    }
                }
            }
        }
        if (controller == null) {
            iface.createController().attemptAttach(this);
        }
    }
    
    boolean preExistingBlock = false;
    CompoundTag cachedNBT = null;
    
    @Override
    public void readNBT(CompoundTag compound) {
        cachedNBT = compound;
        isSaveDelegate = compound.getBoolean("isSaveDelegate");
        preExistingBlock = true;
    }
    
    @Nullable
    @Override
    public CompoundTag writeNBT() {
        if (cachedNBT != null) {
            return cachedNBT;
        }
        CompoundTag compound = new CompoundTag();
        if (isSaveDelegate && controller != null && controller.blocks.containsModule(this)) {
            compound.put("controllerData", controller.getNBT());
            compound.putBoolean("isSaveDelegate", isSaveDelegate);
        }
        return compound;
    }
    
    @Override
    public void onAdded() {
        assert iface.getLevel() != null;
        if (iface.getLevel().isClientSide) {
            cachedNBT = null;
        }
    }
    
    @Override
    public void startTicking() {
        allowAttach = true;
        if (cachedNBT != null) {
            preExistingBlock = true;
        }
        attachToNeighbors();
    }
    
    @Override
    public void stopTicking() {
        if (controller != null) {
            allowAttach = false;
            cachedNBT = writeNBT();
            controller.detach(this, true, false);
        }
    }
    
    @Override
    public void onRemoved(boolean chunkUnload) {
        if (controller != null) {
            controller.detach(this, chunkUnload, !chunkUnload);
        }
        allowAttach = false;
    }

}
