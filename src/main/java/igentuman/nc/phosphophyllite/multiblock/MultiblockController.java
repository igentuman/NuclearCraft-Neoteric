package igentuman.nc.phosphophyllite.multiblock;

import igentuman.nc.NuclearCraft;
import igentuman.nc.phosphophyllite.threading.Queues;
import igentuman.nc.phosphophyllite.util.AStarList;
import igentuman.nc.phosphophyllite.util.ModuleMap;
import igentuman.nc.phosphophyllite.util.Util;
import igentuman.nc.util.joml.Vector2i;
import igentuman.nc.util.joml.Vector3i;
import igentuman.nc.util.joml.Vector3ic;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MultiblockController<
        TileType extends BlockEntity & IMultiblockTile<TileType, ControllerType>,
        ControllerType extends MultiblockController<TileType, ControllerType>
        > {
    
    private MultiblockController<?, ?> mergedInto = null;
    
    protected final Level world;
    
    private boolean hasSaveDelegate = false;
    private boolean isMergingControllers = false;
    protected final ModuleMap<MultiblockTileModule<TileType, ControllerType>, TileType> blocks = new ModuleMap<MultiblockTileModule<TileType, ControllerType>, TileType>(new MultiblockTileModule[0]);
    protected final Set<ITickableMultiblockTile> toTick = new LinkedHashSet<>();
    protected final Set<IAssemblyAttemptedTile> assemblyAttemptedTiles = new LinkedHashSet<>();
    protected final Set<IOnAssemblyTile> onAssemblyTiles = new LinkedHashSet<>();
    protected final Set<IOnDisassemblyTile> onDisassemblyTiles = new LinkedHashSet<>();
    private boolean updateExtremes = true;
    private long updateAssemblyAtTick = Long.MAX_VALUE;
    private long checkForDetachmentsAtTick = Long.MAX_VALUE;
    protected final Set<MultiblockController<?, ?>> controllersToMerge = new LinkedHashSet<>();
    protected final List<BlockPos> removedBlocks = new LinkedList<>();
    
    private final Vector3i minCoord = new Vector3i();
    private final Vector3i maxCoord = new Vector3i();
    private final Vector3i minExtremeBlocks = new Vector3i();
    private final Vector3i maxExtremeBlocks = new Vector3i();
    
    protected final boolean pauseOnUnload;
    
    public enum AssemblyState {
        ASSEMBLED,
        DISASSEMBLED,
        PAUSED,
    }
    
    protected AssemblyState state = AssemblyState.DISASSEMBLED;
    
    private boolean shouldUpdateNBT = false;
    private CompoundTag cachedNBT = null;
    
    protected final Validator<IMultiblockTile<?, ?>> tileTypeValidator;
    private Validator<ControllerType> assemblyValidator = c -> true;
    
    protected boolean isUpdatingState = false;
    protected ValidationError lastValidationError = null;
    
    long lastTick = -1;
    
    public MultiblockController(@Nonnull Level world, @Nonnull Validator<IMultiblockTile<?, ?>> tileTypeValidator, boolean pauseOnUnload) {
        this.tileTypeValidator = tileTypeValidator;
        this.world = world;
        NuclearCraft.addController(this);
        this.pauseOnUnload = pauseOnUnload;
    }
    
    ControllerType self() {
        //noinspection unchecked
        return (ControllerType) this;
    }
    
    public Level getWorld() {
        return world;
    }
    
    public Vector3ic minCoord() {
        return minCoord;
    }
    
    public Vector3ic maxCoord() {
        return maxCoord;
    }
    
    @Nullable
    public TileType getTile(Vector3i position) {
        return blocks.getTile(position);
    }
    
    @Nullable
    public TileType getTile(BlockPos position) {
        return blocks.getTile(position);
    }
    
    public boolean containsTile(TileType tile) {
        return blocks.containsTile(tile);
    }
    
    private void updateMinMaxCoordinates() {
        if (blocks.isEmpty() || !updateExtremes) {
            return;
        }
        updateExtremes = false;
        minCoord.set(Integer.MAX_VALUE);
        maxCoord.set(Integer.MIN_VALUE);
        blocks.forEachPos(pos -> {
            if (pos.getX() < minCoord.x) {
                minCoord.x = pos.getX();
                minExtremeBlocks.x = 1;
            } else if (pos.getX() == minCoord.x) {
                minExtremeBlocks.x++;
            }
            if (pos.getY() < minCoord.y) {
                minCoord.y = pos.getY();
                minExtremeBlocks.y = 1;
            } else if (pos.getY() == minCoord.y) {
                minExtremeBlocks.y++;
            }
            if (pos.getZ() < minCoord.z) {
                minCoord.z = pos.getZ();
                minExtremeBlocks.z = 1;
            } else if (pos.getZ() == minCoord.z) {
                minExtremeBlocks.z++;
            }
            if (pos.getX() > maxCoord.x) {
                maxCoord.x = pos.getX();
                maxExtremeBlocks.x = 1;
            } else if (pos.getX() == maxCoord.x) {
                maxExtremeBlocks.x++;
            }
            if (pos.getY() > maxCoord.y) {
                maxCoord.y = pos.getY();
                maxExtremeBlocks.y = 1;
            } else if (pos.getY() == maxCoord.y) {
                maxExtremeBlocks.y++;
                
            }
            if (pos.getZ() > maxCoord.z) {
                maxCoord.z = pos.getZ();
                maxExtremeBlocks.z = 1;
            } else if (pos.getZ() == maxCoord.z) {
                maxExtremeBlocks.z++;
            }
        });
    }
    
    final void attemptAttach(@Nonnull MultiblockTileModule<?, ?> toAttachGeneric) {
        
        if (!tileTypeValidator.validate(toAttachGeneric.iface)) {
            return;
        }
        
        if (isUpdatingState) {
            throw new IllegalStateException("Attempt to add block while updating multiblock state");
        }
        
        //noinspection unchecked
        MultiblockTileModule<TileType, ControllerType> toAttachModule = (MultiblockTileModule<TileType, ControllerType>) toAttachGeneric;
        TileType toAttachTile = toAttachModule.iface;
        
        if (toAttachModule.controller != null && toAttachModule.controller != this) {
            if (toAttachModule.controller.blocks.size() > blocks.size()) {
                toAttachModule.controller.controllersToMerge.add(self());
            } else {
                controllersToMerge.add(toAttachModule.controller);
            }
            return;
        }
        
        // check if already attached
        if (toAttachModule.controller == this) {
            return;
        }
        
        // ok, its a valid tile to attach, so ima attach it
        
        // prevent double adding, just in case
        if (!blocks.addModule(toAttachModule)) {
            // weird edge case that happened, clearing merged controller's lists of blocks fixed this, but just in case
            toAttachModule.controller = self();
            return;
        }
        
        BlockPos toAttachPos = toAttachTile.getBlockPos();
        // update minmax
        if (toAttachPos.getX() < minCoord.x) {
            minCoord.x = toAttachPos.getX();
            minExtremeBlocks.x = 1;
        } else if (toAttachPos.getX() == minCoord.x) {
            minExtremeBlocks.x++;
        }
        if (toAttachPos.getY() < minCoord.y) {
            minCoord.y = toAttachPos.getY();
            minExtremeBlocks.y = 1;
        } else if (toAttachPos.getY() == minCoord.y) {
            minExtremeBlocks.y++;
        }
        if (toAttachPos.getZ() < minCoord.z) {
            minCoord.z = toAttachPos.getZ();
            minExtremeBlocks.z = 1;
        } else if (toAttachPos.getZ() == minCoord.z) {
            minExtremeBlocks.z++;
        }
        if (toAttachPos.getX() > maxCoord.x) {
            maxCoord.x = toAttachPos.getX();
            maxExtremeBlocks.x = 1;
        } else if (toAttachPos.getX() == maxCoord.x) {
            maxExtremeBlocks.x++;
        }
        if (toAttachPos.getY() > maxCoord.y) {
            maxCoord.y = toAttachPos.getY();
            maxExtremeBlocks.y = 1;
        } else if (toAttachPos.getY() == maxCoord.y) {
            maxExtremeBlocks.y++;
        }
        if (toAttachPos.getZ() > maxCoord.z) {
            maxCoord.z = toAttachPos.getZ();
            maxExtremeBlocks.z = 1;
        } else if (toAttachPos.getZ() == maxCoord.z) {
            maxExtremeBlocks.z++;
        }
        
        if (toAttachTile instanceof ITickableMultiblockTile tickableMultiblockTile) {
            toTick.add(tickableMultiblockTile);
        }
        if (toAttachTile instanceof IAssemblyAttemptedTile assemblyAttemptedTile) {
            assemblyAttemptedTiles.add(assemblyAttemptedTile);
        }
        if (toAttachTile instanceof IOnAssemblyTile onAssemblyTile) {
            onAssemblyTiles.add(onAssemblyTile);
        }
        if (toAttachTile instanceof IOnDisassemblyTile onDisassemblyTile) {
            onDisassemblyTiles.add(onDisassemblyTile);
        }
        
        if (toAttachModule.isSaveDelegate) {
            if (hasSaveDelegate) {
                toAttachModule.isSaveDelegate = false;
            } else {
                hasSaveDelegate = true;
            }
        }
        
        toAttachModule.controller = self();
        if (toAttachModule.preExistingBlock) {
            if (toAttachModule.cachedNBT != null && toAttachModule.cachedNBT.contains("controllerData")) {
                onBlockWithNBTAttached(toAttachModule.cachedNBT.getCompound("controllerData"));
                toAttachModule.cachedNBT = null;
            }
            if (state == AssemblyState.DISASSEMBLED && !isMergingControllers) {
                state = AssemblyState.PAUSED;
            }
            onPartAttached(toAttachTile);
        } else {
            state = AssemblyState.DISASSEMBLED;
            onPartPlaced(toAttachTile);
        }
        toAttachModule.updateNeighbors();
        updateAssemblyAtTick = NuclearCraft.tickNumber() + 1;
    }
    
    final void detach(@Nonnull MultiblockTileModule<TileType, ControllerType> toDetach) {
        detach(toDetach, false);
    }
    
    final void detach(@Nonnull MultiblockTileModule<TileType, ControllerType> toDetach, boolean onChunkUnload) {
        detach(toDetach, onChunkUnload, true);
    }
    
    final void detach(@Nonnull MultiblockTileModule<TileType, ControllerType> toDetach, boolean onChunkUnload, boolean attemptReattach) {
        detach(toDetach, onChunkUnload, attemptReattach, true);
    }
    
    final void detach(@Nonnull MultiblockTileModule<TileType, ControllerType> toDetachModule, boolean onChunkUnload, boolean attemptReattach, boolean checkForDetachments) {
        if (!blocks.removeModule(toDetachModule)) {
            return;
        }
        TileType toDetachTile = toDetachModule.iface;
        
        if (isUpdatingState) {
            throw new IllegalStateException("Attempt to remove block while updating multiblock state");
        }
        
        if (toDetachTile instanceof ITickableMultiblockTile) {
            toTick.remove(toDetachTile);
        }
        if (toDetachTile instanceof IAssemblyAttemptedTile) {
            assemblyAttemptedTiles.remove(toDetachTile);
        }
        if (toDetachTile instanceof IOnAssemblyTile) {
            onAssemblyTiles.remove(toDetachTile);
        }
        if (toDetachTile instanceof IOnDisassemblyTile) {
            onDisassemblyTiles.remove(toDetachTile);
        }
        
        if (onChunkUnload) {
            onPartDetached(toDetachTile);
            if (pauseOnUnload) {
                state = AssemblyState.PAUSED;
                updateCachedNBT();
            }
        } else {
            onPartBroken(toDetachTile);
        }
        
        if (attemptReattach) {
            Queues.serverThread.enqueue(toDetachModule::attachToNeighbors);
        }
        
        toDetachModule.nullNeighbors();
        toDetachModule.controller = null;
        
        if (toDetachModule.isSaveDelegate) {
            hasSaveDelegate = false;
        }
        
        if (blocks.isEmpty()) {
            NuclearCraft.removeController(this);
        }
        
        BlockPos toDetachPos = toDetachTile.getBlockPos();
        
        if (checkForDetachments) {
            this.checkForDetachmentsAtTick = NuclearCraft.tickNumber() + 2;
            if (!onChunkUnload) {
                this.checkForDetachmentsAtTick = Long.MIN_VALUE;
            }
            removedBlocks.add(toDetachPos);
        }
        
        if (toDetachPos.getX() == minCoord.x) {
            minExtremeBlocks.x--;
            if (minExtremeBlocks.x == 0) {
                updateExtremes = true;
            }
        }
        if (toDetachPos.getY() == minCoord.y) {
            minExtremeBlocks.y--;
            if (minExtremeBlocks.y == 0) {
                updateExtremes = true;
            }
        }
        if (toDetachPos.getZ() == minCoord.z) {
            minExtremeBlocks.z--;
            if (minExtremeBlocks.z == 0) {
                updateExtremes = true;
            }
        }
        if (toDetachPos.getX() == maxCoord.x) {
            maxExtremeBlocks.x--;
            if (maxExtremeBlocks.x == 0) {
                updateExtremes = true;
            }
        }
        if (toDetachPos.getY() == maxCoord.y) {
            maxExtremeBlocks.y--;
            if (maxExtremeBlocks.y == 0) {
                updateExtremes = true;
            }
        }
        if (toDetachPos.getZ() == maxCoord.z) {
            maxExtremeBlocks.z--;
            if (maxExtremeBlocks.z == 0) {
                updateExtremes = true;
            }
        }
        
        updateAssemblyAtTick = NuclearCraft.tickNumber() + 1;
    }
    
    public final void update() {
        if (lastTick >= NuclearCraft.tickNumber()) {
            return;
        }
        lastTick = NuclearCraft.tickNumber();
        
        if (blocks.isEmpty()) {
            // why are we being ticked?
            NuclearCraft.removeController(this);
            checkForDetachmentsAtTick = Long.MAX_VALUE;
        }
        
        if (checkForDetachmentsAtTick <= NuclearCraft.tickNumber()) {
            AStarList<MultiblockTileModule<?, ?>> aStarList = new AStarList<>(module -> module.iface.getBlockPos());
            
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (BlockPos removedBlock : removedBlocks) {
                for (Direction value : Direction.values()) {
                    mutableBlockPos.set(removedBlock);
                    mutableBlockPos.move(value);
                    MultiblockTileModule<TileType, ControllerType> module = blocks.getModule(mutableBlockPos);
                    if (module != null && module.controller == this) {
                        aStarList.addTarget(module);
                    }
                }
            }
            removedBlocks.clear();
            
            var directions = Direction.values();
            
            while (!aStarList.done()) {
                var node = aStarList.nextNode();
                for (int i = 0; i < 6; i++) {
                    node.lastSavedTick = this.lastTick;
                    var module = node.getNeighbor(directions[i]);
                    if (module != null && module.controller == this && module.lastSavedTick != this.lastTick) {
                        module.lastSavedTick = this.lastTick;
                        aStarList.addNode(module);
                    }
                }
            }
            
            if (!aStarList.foundAll()) {
                HashSet<MultiblockTileModule<TileType, ControllerType>> toOrphan = new LinkedHashSet<>();
                blocks.forEachModule(module -> {
                    if (module.lastSavedTick != this.lastTick) {
                        toOrphan.add(module);
                    }
                });
                if (!toOrphan.isEmpty()) {
                    for (MultiblockTileModule<TileType, ControllerType> tile : toOrphan) {
                        detach(tile, state == AssemblyState.PAUSED, true, false);
                    }
                    updateAssemblyAtTick = Long.MIN_VALUE;
                }
            }
            checkForDetachmentsAtTick = Long.MAX_VALUE;
        }
        while (!controllersToMerge.isEmpty()) {
            HashSet<MultiblockController<?, ?>> newToMerge = new HashSet<>();
            for (MultiblockController<?, ?> otherController : controllersToMerge) {
                otherController.disassembledBlockStates();
                NuclearCraft.removeController(otherController);
                otherController.controllersToMerge.remove(self());
                newToMerge.addAll(otherController.controllersToMerge);
                otherController.controllersToMerge.clear();
                if (otherController.mergedInto != null && otherController.mergedInto != this) {
                    newToMerge.add(otherController.mergedInto);
                    otherController.mergedInto.controllersToMerge.add(this);
                    continue;
                }
                if (otherController.blocks.size() == 0) {
                    continue;
                }
                //noinspection unchecked
                this.onMerge((ControllerType) otherController);
                if (this.cachedNBT == null && otherController.cachedNBT != null) {
                    this.cachedNBT = otherController.cachedNBT;
                    otherController.cachedNBT = null;
                }
                isMergingControllers = true;
                otherController.blocks.forEachModule(module -> {
                    module.controller = null;
                    module.preExistingBlock = true;
                    attemptAttach(module);
                });
                isMergingControllers = false;
                otherController.blocks.clear();
                updateAssemblyAtTick = Long.MIN_VALUE;
                otherController.mergedInto = this;
            }
            controllersToMerge.clear();
            controllersToMerge.addAll(newToMerge);
        }
        
        if (updateAssemblyAtTick < lastTick) {
            updateMinMaxCoordinates();
            updateAssemblyState();
            updateAssemblyAtTick = Long.MAX_VALUE;
        }
        
        if (state == AssemblyState.ASSEMBLED) {
            tick();
            toTick.forEach(ITickableMultiblockTile::tick);
        } else if (state == AssemblyState.DISASSEMBLED) {
            disassembledTick();
        }
    }
    
    public void revalidate() {
        updateAssemblyAtTick = NuclearCraft.tickNumber() + 1;
    }
    
    public void suicide() {
        ModuleMap<MultiblockTileModule<TileType, ControllerType>, TileType> blocks = new ModuleMap<MultiblockTileModule<TileType, ControllerType>, TileType>(new MultiblockTileModule[0]);
        blocks.addAll(this.blocks);
        blocks.forEachModule(module -> module.onRemoved(true));
        NuclearCraft.removeController(this);
    }
    
    private void updateAssemblyState() {
        AssemblyState oldState = state;
        boolean validated = false;
        lastValidationError = null;
        try {
            isUpdatingState = true;
            try {
                validated = assemblyValidator.validate(self());
            } catch (ValidationError e) {
                lastValidationError = e;
            }
            if (validated) {
                state = AssemblyState.ASSEMBLED;
                if (cachedNBT != null && oldState != AssemblyState.ASSEMBLED) {
                    read(cachedNBT.getCompound("userdata"));
                    shouldUpdateNBT = true;
                }
                onValidationPassed();
                if (oldState == AssemblyState.PAUSED) {
                    onUnpaused();
                } else if (oldState == AssemblyState.DISASSEMBLED) {
                    onAssembled();
                }
                assembledBlockStates();
                onAssemblyTiles.forEach(IOnAssemblyTile::onAssembly);
                if (!hasSaveDelegate) {
                    MultiblockTileModule<TileType, ControllerType> module = blocks.getOne();
                    assert module != null;
                    module.isSaveDelegate = true;
                    hasSaveDelegate = true;
                }
            } else {
                if (oldState == AssemblyState.ASSEMBLED) {
                    state = AssemblyState.DISASSEMBLED;
                    onDisassembled();
                    disassembledBlockStates();
                    updateCachedNBT();
                    onDisassemblyTiles.forEach(IOnDisassemblyTile::onDisassembly);
                }
            }
            assemblyAttemptedTiles.forEach(IAssemblyAttemptedTile::onAssemblyAttempted);
        } finally {
            isUpdatingState = false;
        }
    }
    
    private void onBlockWithNBTAttached(CompoundTag nbt) {
        if (cachedNBT == null) {
            readNBT(nbt);
        }
    }
    
    private final Long2ObjectLinkedOpenHashMap<BlockState> newStates = new Long2ObjectLinkedOpenHashMap<>();
    
    private void assembledBlockStates() {
        newStates.clear();
        
        final int size = blocks.size();
        final TileType[] tileElements = blocks.tileElements();
        final MultiblockTileModule<TileType, ControllerType>[] moduleElements = blocks.moduleElements();
        final var posElements = blocks.posElements();
        if (tileElements.length < size || moduleElements.length < size || posElements.length < size) {
            throw new IllegalStateException("Arrays too short");
        }
        for (int i = 0; i < size; i++) {
            final var entity = tileElements[i];
            final var module = moduleElements[i];
            final var pos = posElements[i];
            final BlockState oldState = entity.getBlockState();
            BlockState newState = module.assembledBlockState(oldState);
            if (newState != oldState) {
                newStates.put(pos, newState);
                entity.setBlockState(newState);
            }
        }
        if (!newStates.isEmpty()) {
            Util.setBlockStates(newStates, world);
        }
    }
    
    private void disassembledBlockStates() {
        newStates.clear();
        final int size = blocks.size();
        final TileType[] tileElements = blocks.tileElements();
        final MultiblockTileModule<TileType, ControllerType>[] moduleElements = blocks.moduleElements();
        final var posElements = blocks.posElements();
        if (tileElements.length < size || moduleElements.length < size || posElements.length < size) {
            throw new IllegalStateException("Arrays too short");
        }
        for (int i = 0; i < size; i++) {
            final var entity = tileElements[i];
            final var module = moduleElements[i];
            final var pos = posElements[i];
            final BlockState oldState = entity.getBlockState();
            BlockState newState = module.disassembledBlockState(oldState);
            if (newState != oldState) {
                newStates.put(pos, newState);
                entity.setBlockState(newState);
            }
        }
        if (!newStates.isEmpty()) {
            Util.setBlockStates(newStates, world);
        }
    }
    
    /**
     * Read from the NBT saved by member blocks
     *
     * @param nbt previously returned by getNBT that represents this multiblock
     */
    final void readNBT(CompoundTag nbt) {
        if (!nbt.isEmpty()) {
            cachedNBT = nbt.copy();
            CompoundTag multiblockData = cachedNBT.getCompound("multiblockData");
            if (multiblockData.contains("assemblyState")) {
                // dont just shove this into this.state
                // if you do onDisassembled will be called incorrectly
                AssemblyState nbtState = AssemblyState.valueOf(multiblockData.getString("assemblyState"));
                // because minecraft is dumb, and saves chunks before unloading them, i need to treat assembled as paused too
                if (state == AssemblyState.DISASSEMBLED && (nbtState == AssemblyState.PAUSED || nbtState == AssemblyState.ASSEMBLED) && pauseOnUnload) {
                    state = AssemblyState.PAUSED;
                }
            }
        }
    }
    
    /**
     * Called by member blocks when saving to world
     * may not directly call write() from here
     * <p>
     * DO NOT EDIT THE RETURNED NBT, weird things can happen if you do
     *
     * @return NBT that represents this multiblock
     */
    @Nonnull
    final CompoundTag getNBT() {
        if (!pauseOnUnload) {
            return new CompoundTag();
        }
        if (shouldUpdateNBT) {
            shouldUpdateNBT = false;
            updateCachedNBT();
        }
        return cachedNBT == null ? new CompoundTag() : cachedNBT;
    }
    
    private void updateCachedNBT() {
        cachedNBT = new CompoundTag();
        cachedNBT.put("userdata", write());
        CompoundTag multiblockData = new CompoundTag();
        cachedNBT.put("multiblockData", multiblockData);
        {
            // instead of storing an exhaustive list of all the blocks we had
            // just save the controller hash, and make sure we have the right number
            multiblockData.putInt("controller", hashCode());
            multiblockData.putString("assemblyState", state.toString());
        }
    }
    
    /**
     * Marks multiblocks structure as dirty to minecraft so it is saved
     */
    protected final void markDirty() {
        shouldUpdateNBT = true;
        Util.markRangeDirty(world, new Vector2i(minCoord.x, minCoord.z), new Vector2i(maxCoord.x, maxCoord.z));
    }
    
    @Nonnull
    public AssemblyState assemblyState() {
        return state;
    }
    
    
    // -- API --
    
    /**
     * Sets, or removes, the validator to be used to determine if the multiblock is assembled or not
     *
     * @param validator the new Validator, or null to remove
     */
    
    protected void setAssemblyValidator(@Nullable Validator<ControllerType> validator) {
        if (validator != null) {
            assemblyValidator = validator;
        }
    }
    
    /**
     * Called at the end of a tick for assembled multiblocks only
     * is not called if the multiblock is dissassembled or paused
     */
    public void tick() {
    }
    
    /**
     * Called at the end of a tick for dissassembled multiblocks only
     * not called if the multiblock is assembled or paused
     */
    public void disassembledTick() {
    }
    
    /**
     * Called when a part is added to the multiblock structure
     * not called in conjunction with onPartPlaced
     * <p>
     * CANNOT ALTER NBT STATE
     *
     * @param toAttach, the part that was added
     */
    protected void onPartAttached(@Nonnull TileType toAttach) {
    }
    
    /**
     * Called when a part is removed to the multiblock structure
     * not called in conjunction with onPartBroken
     * <p>
     * CANNOT ALTER NBT STATE
     *
     * @param toDetach, the part that was removed
     */
    protected void onPartDetached(@Nonnull TileType toDetach) {
    }
    
    /**
     * Called when a new part is added to the world, or a block is merged in from another multiblock
     * <p>
     * not called when a previously placed block is reloaded
     *
     * @param placed the block that was placed
     */
    protected void onPartPlaced(@Nonnull TileType placed) {
    }
    
    /**
     * Called when a part is removed from the world, or a block is detached durring separation
     * <p>
     * not called when a part is unloaded
     *
     * @param broken the block that was broken
     */
    protected void onPartBroken(@Nonnull TileType broken) {
    }
    
    /**
     * Called when two multiblock controllers are merged together
     * <p>
     * this happens with a block connecting the two is placed
     * <p>
     * only called for the controller that will reside over the blocks both controllers control
     * <p>
     * called before blocks have been moved to the primary controller
     *
     * @param otherController the controller to merge into this one
     */
    protected void onMerge(@Nonnull ControllerType otherController) {
    }
    
    /**
     * Called when a multiblock passes validation
     * <p>
     * called after @onPartPlaced, called before @onAssembled or @onUnpaused
     */
    protected void onValidationPassed() {
    }
    
    /**
     * Called when a multiblock is assembled from a disassembled state
     * <p>
     * called after @onPartPlaced
     * Not called if multiblock is already assembled
     */
    protected void onAssembled() {
    }
    
    /**
     * Called when a multiblock is disassembled by a destroyed block
     * <p>
     * called after @onPartBroken, called before @write
     */
    protected void onDisassembled() {
    }
    
    /**
     * Called when a multiblock is to be resumed from a paused state
     * <p>
     * called after @read but before first call to @tick
     */
    protected void onUnpaused() {
    
    }
    
    /**
     * Called after a multiblock has passes assembly validation, and has an NBT to read from
     * <p>
     * may not be called for new multiblocks
     *
     * @param compound the NBT that was written to in the last write call
     */
    protected void read(@Nonnull CompoundTag compound) {
    }
    
    /**
     * Create an NBT tag to be saved and re-read upon multiblock re-assembly
     * <p>
     * Can be called at any time, and multiblock must be able to resume from this NBT regardless of its current state
     *
     * @return the NBT to save
     */
    @Nonnull
    protected CompoundTag write() {
        return new CompoundTag();
    }
    
}
