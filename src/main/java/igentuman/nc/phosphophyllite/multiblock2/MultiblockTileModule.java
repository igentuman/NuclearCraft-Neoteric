package igentuman.nc.phosphophyllite.multiblock2;

import igentuman.nc.phosphophyllite.threading.Queues;
import igentuman.nc.phosphophyllite.util.Util;
import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.modular.api.IModularTile;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.phosphophyllite.modular.tile.IIsTickingTracker;
import igentuman.nc.phosphophyllite.multiblock2.modular.ICoreMultiblockTileModule;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.Objects;

import static igentuman.nc.phosphophyllite.util.Util.DIRECTIONS;


@NonnullDefault
public final class MultiblockTileModule<
        TileType extends BlockEntity & IMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType>
        > extends TileModule<TileType> implements IIsTickingTracker {
    
    @Nullable
    private ControllerType controller;
    
    boolean preExistingBlock = false;
    boolean allowAttach = false;
    
    long lastSavedTick = 0;
    
    @SuppressWarnings("unchecked") // its fine
    final MultiblockTileModule<TileType, BlockType, ControllerType>[] neighbors = new MultiblockTileModule[6];
    final BlockEntity[] neighborTiles = new BlockEntity[6];
    
    private final ObjectArrayList<ICoreMultiblockTileModule<TileType, BlockType, ControllerType>> coreMultiblockTileModules = new ObjectArrayList<>();
    
    @OnModLoad
    private static void onModLoad() {
        ModuleRegistry.registerTileModule(IMultiblockTile.class, MultiblockTileModule::new);
    }
    
    public MultiblockTileModule(IModularTile blockEntity) {
        super(blockEntity);
    }
    
    @Override
    public void postModuleConstruction() {
        for (TileModule<?> module : iface.modules()) {
            if (module instanceof ICoreMultiblockTileModule<?, ?, ?>) {
                //noinspection unchecked
                coreMultiblockTileModules.add((ICoreMultiblockTileModule<TileType, BlockType, ControllerType>) module);
            }
        }
    }
    
    /*
     * There is a potential edge case where a multiblock can have blocks half loaded be broken by non-player means (quarry)
     */
    @Override
    public void startTicking() {
        // this fires on server only
        allowAttach = true;
        attachToNeighborsLater();
    }
    
    @Override
    public void stopTicking() {
        if (controller != null) {
            allowAttach = false;
            // effectively chunk unload, if a player is going to remove it, it will be ticking again when that happens
            coreMultiblockTileModules.forEach(ICoreMultiblockTileModule::aboutToUnloadDetach);
            controller.detach(this, true, false, true);
            // in the event we start ticking again, we are a pre-existing block
            preExistingBlock = true;
        }
    }
    
    @Override
    public void onRemoved(boolean chunkUnload) {
        if (controller != null) {
            if (chunkUnload) {
                coreMultiblockTileModules.forEach(ICoreMultiblockTileModule::aboutToUnloadDetach);
            } else {
                coreMultiblockTileModules.forEach(ICoreMultiblockTileModule::aboutToRemovedDetach);
            }
            controller.detach(this, chunkUnload, false, true);
        }
    }
    
    @Nullable
    public ControllerType controller() {
        return controller;
    }
    
    @Contract
    @Nullable
    ControllerType controller(@Nullable ControllerType newController) {
        if (controller != newController) {
            controller = newController;
            coreMultiblockTileModules.forEach(ICoreMultiblockTileModule::onControllerChange);
        }
        return controller;
    }
    
    @Override
    public String saveKey() {
        return "phosphophyllite_multiblock";
    }
    
    @Override
    public void readNBT(CompoundTag nbt) {
        preExistingBlock = true;
    }
    
    @Override
    public CompoundTag writeNBT() {
        return new CompoundTag();
    }
    
    @Contract(pure = true)
    private boolean shouldConnectTo(IMultiblockTile<?, ?, ?> otherRawTile, Direction direction) {
        if (!allowAttach) {
            return false;
        }
        if (this.controller != null) {
            if (!controller.canAttachTile(otherRawTile)) {
                return false;
            }
        }
        if (otherRawTile.nullableController() != null) {
            if (!otherRawTile.controller().canAttachTile(iface)) {
                return false;
            }
        }
        
        // its safe to cast at this point because both (if existing) controllers agree that the they can be attached to each other
        //noinspection unchecked
        final var otherTile = (TileType) otherRawTile;
        //noinspection unchecked
        final var otherModule = (MultiblockTileModule<TileType, BlockType, ControllerType>) otherTile.module(IMultiblockTile.class);
        assert otherModule != null;
        final var oppositeDirection = direction.getOpposite();
        
        for (int i = 0; i < coreMultiblockTileModules.size(); i++) {
            final var extensionModule = coreMultiblockTileModules.get(i);
            if (!extensionModule.shouldConnectTo(otherTile, direction)) {
                return false;
            }
        }
        
        for (int i = 0; i < otherModule.coreMultiblockTileModules.size(); i++) {
            final var extensionModule = otherModule.coreMultiblockTileModules.get(i);
            if (!extensionModule.shouldConnectTo(iface, oppositeDirection)) {
                return false;
            }
        }
        
        return true;
    }
    
    void updateNeighbors() {
        if (controller == null) {
            return;
        }
        nullNeighbors();
        var pos = iface.getBlockPos();
        for (Direction value : DIRECTIONS) {
            var neighbor = controller.blocks.getModule(pos.getX() + value.getStepX(), pos.getY() + value.getStepY(), pos.getZ() + value.getStepZ());
            if (neighbor == null || !shouldConnectTo(neighbor.iface, value)) {
                continue;
            }
            neighbors[value.get3DDataValue()] = neighbor;
            neighborTiles[value.get3DDataValue()] = neighbor.iface;
        }
        for (int i = 0; i < neighbors.length; i++) {
            MultiblockTileModule<TileType, BlockType, ControllerType> neighbor = neighbors[i];
            if (neighbor != null) {
                neighbor.neighbors[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = this;
                neighbor.neighborTiles[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = iface;
            }
        }
    }
    
    void nullNeighbors() {
        for (int i = 0; i < neighbors.length; i++) {
            MultiblockTileModule<?, ?, ?> neighbor = neighbors[i];
            if (neighbor != null) {
                neighbor.neighbors[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = null;
                neighbor.neighborTiles[Direction.from3DDataValue(i).getOpposite().get3DDataValue()] = null;
            }
            neighbors[i] = null;
            neighborTiles[i] = null;
        }
    }
    
    @Nullable
    public MultiblockTileModule<TileType, BlockType, ControllerType> getNeighbor(Direction direction) {
        return neighbors[direction.get3DDataValue()];
    }
    
    public void attachToNeighborsLater() {
        if (Objects.requireNonNull(iface.getLevel()).isClientSide) {
            return;
        }
        Queues.serverThread.enqueueUntracked(this::attachToNeighborsNow);
    }
    
    public void attachToNeighborsNow() {
        final var level = iface.getLevel();
        assert level != null;
        if (level.isClientSide) {
            return;
        }
        if (iface.isRemoved()) {
            return;
        }
        final var pos = iface.getBlockPos();
        if (iface.getLevel().getBlockEntity(pos) != iface) {
            return;
        }
        coreMultiblockTileModules.forEach(ICoreMultiblockTileModule::aboutToAttemptAttach);
        BlockPos.MutableBlockPos possibleTilePos = new BlockPos.MutableBlockPos();
        for (Direction direction : DIRECTIONS) {
            possibleTilePos.set(pos);
            possibleTilePos.move(direction);
            final var tile = Util.getTile(level, possibleTilePos);
            if (tile instanceof IMultiblockTile<?, ?, ?> multiblockTile) {
                final MultiblockTileModule<?, ?, ?> multiblockModule = multiblockTile.multiblockModule();
                if (multiblockModule.controller == null) {
                    continue;
                }
                if (!shouldConnectTo(multiblockTile, direction)) {
                    continue;
                }
                multiblockModule.controller.attemptAttach(this);
            }
        }
        if (controller == null) {
            iface.createController().attemptAttach(this);
        }
    }
}
