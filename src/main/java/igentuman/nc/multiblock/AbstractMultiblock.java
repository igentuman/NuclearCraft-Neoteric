package igentuman.nc.multiblock;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.api.nc.multiblock.Multiblock;
import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.CommonConfig.MISC_CONFIG;
import static net.minecraft.world.level.block.Blocks.AIR;

public abstract class AbstractMultiblock implements Multiblock {

    public boolean hasToRefresh = true;
    public BlockPos errorBlockPos = BlockPos.ZERO;
    public int connectedPorts = 0;
    protected int height;
    protected int width;
    protected int depth;
    protected MultiblockController controller;
    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public String id;
    public int topCasing = 0;
    public int bottomCasing = 0;
    public int leftCasing = 0;
    public int rightCasing = 0;
    protected BlockPos bottomLeft;
    protected BlockPos topRight;
    protected boolean outerValid = false;
    protected boolean isFormed = false;
    protected boolean innerValid = false;
    protected final HashSet<Block> validOuterBlocks;
    protected final HashSet<Block> validInnerBlocks;
    protected final HashSet<BlockPos> controllers = new HashSet<>();
    protected final HashMap<Long, BlockEntity> beCache = new HashMap<>();
    protected final HashMap<Long, BlockState> bsCache = new HashMap<>(10000);
    protected final HashSet<Long> allBlocks = new HashSet<>(10000);
    protected BlockPosInstance controllerPos;
    protected BlockPosInstance initialPos;
    protected Direction multiblockDirection;
    protected MultiblockControllerBE controllerBe;
    private static final Pattern SPECIAL_BLOCKS = Pattern.compile(".*(fusion_proxy|fusion_core|controller|port|irradiator|rotor|chamber_terminal).*");
    private static final Pattern CONTROLLERS = Pattern.compile(".*(controller|terminal).*");

    protected AbstractMultiblock(HashSet<Block> validOuterBlocks, HashSet<Block> validInnerBlocks, MultiblockController controller) {
        this.validOuterBlocks = validOuterBlocks;
        this.validInnerBlocks = validInnerBlocks;
        this.controller = controller;
        controllerPos = BlockPosInstance.of(controller().controllerBE().getBlockPos());
    }

    public void dispose() {
        MultiblockHandler.get(getLevel().dimension()).removeMultiblock(this);
    }

    public HashSet<Block> validCornerBlocks() {
        return validOuterBlocks;
    }

    public int height() {
        return height;
    }
    public int width() {
        return width;
    }
    public int depth() {
        return depth;
    }
    public int maxHeight() {
        return 24;
    }
    public int minHeight() {
        return 3;
    }
    public int maxWidth() {
        return 24;
    }
    public int minWidth() {
        return 3;
    }
    public int maxDepth() {
        return 24;
    }
    public int minDepth() {
        return 3;
    }

    public boolean isFormed() {
        return isFormed;
    }

    public boolean isPort(BlockState bs) {
        return bs.getBlock().asItem().toString().contains("port");
    }

    @Override
    public HashSet<Block> validOuterBlocks() { return validOuterBlocks;  }

    @Override
    public HashSet<Block> validInnerBlocks() { return validInnerBlocks; }

    protected Level getLevel() {
        if (controller() == null || controller().controllerBE() == null) {
            return null;
        }
        return controller().controllerBE().getLevel();
    }

    protected void addIfNotExists(BlockPos pos, HashSet<Long> list) {
        list.add(pos.asLong());
    }

    protected void addIfNotExists(long pos, HashSet<Long> list) {
        list.add(pos);
    }

    protected BlockPosInstance initialPos() {
        if (controllerPos == null) {
            controllerPos = BlockPosInstance.copy(controller().controllerBE().getBlockPos());
        }
        if (initialPos == null) {
            initialPos = BlockPosInstance.copy(controllerPos);
        }
        return initialPos.revert();
    }

    public BlockPos getBottomLeftBlock() {
        if (controllerPos != null) {
            controllerPos.revert();
        }
        return new BlockPos(getLeftPos(leftCasing).below(bottomCasing).relative(getControllerDirection(), -depth+1));
    }

    public BlockPos getTopRightBlock() {
        if (controllerPos != null) {
            controllerPos.revert();
        }
        return new BlockPos(getRightPos(rightCasing).above(topCasing));
    }

    public BlockPos getCenterBlock() {
        BlockPos bottomLeft = getBottomLeftBlock();
        BlockPos topRight = getTopRightBlock();
        return new BlockPos(
                (bottomLeft.getX() + topRight.getX()) / 2,
                (bottomLeft.getY() + topRight.getY()) / 2,
                (bottomLeft.getZ() + topRight.getZ()) / 2
        );
    }

    public BlockState getBlockState(long pos) {
        BlockState cached = bsCache.get(pos);
        if (cached != null) {
            return cached;
        }
        BlockState state = getLevel().getBlockState(BlockPos.of(pos));
        bsCache.put(pos, state);
        return state;
    }

    protected BlockState getBlockState(BlockPosInstance relative, boolean force) {
        BlockState bs = getBlockState(relative);
        if(bs.isAir() && force) {
            bs = getLevel().getBlockState(relative);
        }
        return bs;
    }

    public BlockState getCachedBlockState(BlockPos pos) {
        if (bsCache.containsKey(pos.asLong())) {
            return bsCache.get(pos.asLong());
        }
        return null;
    }

    public BlockState getBlockState(BlockPos pos) {
        final long packedPos = pos.asLong();
        if (bsCache.containsKey(packedPos)) {
            return bsCache.get(packedPos);
        }
        BlockState state = getLevel().getBlockState(pos);
        bsCache.put(packedPos, state);
        return state;
    }

    public boolean isValidForOuter(BlockPos pos)
    {
        if (getLevel() == null) return false;
        try {
            return  validOuterBlocks().contains(getBlockState(pos).getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    public boolean isValidCorner(BlockPos pos)
    {
        try {
            return  validCornerBlocks().contains(getBlockState(pos).getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    public boolean isValidForInner(BlockState bs)
    {
        return bs.isAir() || validInnerBlocks().contains(bs.getBlock());
    }

    public boolean isValidForInner(BlockPos pos)
    {
        if (getLevel() == null) return false;
        return isValidForInner(getBlockState(pos));
    }

    public void cacheBlockStates() {
        if(!MISC_CONFIG.EXPERIMENTAL_BLOCK_INDEXING.get()) {
            return;
        }
        int minX = bottomLeft.getX();
        int minY = bottomLeft.getY();
        int minZ = bottomLeft.getZ();
        int maxX = topRight.getX();
        int maxY = topRight.getY();
        int maxZ = topRight.getZ();
        ServerLevel serverLevel = (ServerLevel) getLevel();
        ChunkAccess currentChunk = null;
        int lastChunkX = Integer.MIN_VALUE;
        int lastChunkZ = Integer.MIN_VALUE;

        for (int x = minX; x <= maxX; x++) {
            int chunkX = x >> 4;
            for (int z = minZ; z <= maxZ; z++) {
                int chunkZ = z >> 4;
                if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
                    currentChunk = serverLevel.getChunkSource().getChunk(chunkX, chunkZ, true);
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                }
                if (currentChunk == null) continue;

                for (int y = minY; y <= maxY; y++) {
                    int sectionIndex = serverLevel.getSectionIndex(y);
                    LevelChunkSection section = currentChunk.getSections()[sectionIndex];
                    if (section == null || section.hasOnlyAir()) continue;

                    int localX = x & 15;
                    int localY = y & 15;
                    int localZ = z & 15;

                    BlockState state = section.getBlockState(localX, localY, localZ);

                    bsCache.put(BlockPos.asLong(x, y, z), state);
                }
            }
        }
    }

    public int resolveHeight()
    {
        for (int i = 1; i <= maxHeight()+2; i++) {
            if (!isValidForOuter(initialPos().above(i))) {
                topCasing = i - 1;
                height = i;
                break;
            }
        }
        for (int i = 1; i <= maxHeight()+2; i++) {
            if (!isValidForOuter(initialPos().below(i))) {
                bottomCasing = i - 1;
                height += i - 1;
                break;
            }
        }

        return height;
    }

    public int resolveWidth()
    {
        for(int i = 1; i <= maxWidth()+2; i++) {
            if (!isValidForOuter(getLeftPos(i).above(topCasing))) {
                leftCasing = i-1;
                width = i;
                break;
            }
        }
        for(int i = 1; i <= maxWidth()+2; i++) {
            if (!isValidForOuter(getRightPos(i).above(topCasing))) {
                rightCasing = i-1;
                width += i-1;
                break;
            }
        }
        return width;
    }

    public int resolveDepth()
    {
        for(int i = 1; i <= maxDepth()+2; i++) {
            if (!isValidForOuter(getForwardPos(i).below(bottomCasing))) {
                depth = i;
                break;
            }
        }
        return depth;
    }

    protected Block getBlock(BlockPos pos) {
        if(getLevel() == null) {
            return AIR;
        }
        return getBlockState(pos).getBlock();
    }

    protected MultiblockControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return controllerBe;
    }

    public void resolveDimensions()
    {
        if (getMultiblockDirection() == null)  return;
        resolveHeight();
        resolveDepth();
        resolveWidth();
        topRight = getTopRightBlock();
        bottomLeft = getBottomLeftBlock();
        controllerBE().topRight = new BlockPos(topRight);
        controllerBE().bottomLeft = new BlockPos(bottomLeft);
    }

    @Override
    public void validateOuter() {
        outerValid = false;
        resolveDimensions();

        if (width > maxWidth() || height > maxHeight() || depth > maxDepth())
        {
            validationResult = ValidationResult.TOO_BIG;
            return;
        }

        if (width < minWidth() || height < minHeight() || depth < minDepth())
        {
            validationResult = ValidationResult.TOO_SMALL;
            return;
        }

        BlockPos leftFront = new BlockPosInstance(getLeftPos(leftCasing));
        BlockPos leftBack = new BlockPosInstance(getLeftPos(leftCasing).relative(getControllerDirection(), -depth+1));
        BlockPos rightFront = new BlockPosInstance(getRightPos(rightCasing));
        BlockPos rightBack = new BlockPosInstance(getRightPos(rightCasing).relative(getControllerDirection(), -depth+1));
        int minX = MathUtils.min(leftFront.getX(), rightFront.getX(), leftBack.getX(), rightBack.getX());
        int minZ = MathUtils.min(leftFront.getZ(), rightFront.getZ(), leftBack.getZ(), rightBack.getZ());
        int maxX = MathUtils.max(leftFront.getX(), rightFront.getX(), leftBack.getX(), rightBack.getX());
        int maxZ = MathUtils.max(leftFront.getZ(), rightFront.getZ(), leftBack.getZ(), rightBack.getZ());
        bottomLeft = new BlockPosInstance(minX, leftFront.getY() - bottomCasing, minZ);
        topRight = new BlockPosInstance(maxX, leftFront.getY() + topCasing, maxZ);
        cacheBlockStates();
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        if (((y == 0 || y == height-1) && (z == 0 || z == depth - 1))
                        || ((y == 0 || y == height-1) && (x == 0 || x == width - 1))
                        || ((z == 0 || z == depth-1) && (x == 0 || x == width - 1))
                        ) {
                            if (!isValidCorner(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z))) {
                                validationResult = ValidationResult.WRONG_CORNER;
                                errorBlockPos = new BlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                                return;
                            }
                        } else if (!isValidForOuter(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z))) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            errorBlockPos = new BlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                            return;
                        }

                        processOuterBlock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    }
                }
            }
        }
        if (controllers.size() > 1) {
            validationResult = ValidationResult.TOO_MANY_CONTROLLERS;
            return;
        }
        outerValid = true;
        validationResult = ValidationResult.VALID;
    }

    protected void processOuterBlock(BlockPos pos) {
        attachMultiblock(pos);
        addIfNotExists(pos, allBlocks);
        if (CONTROLLERS.matcher(getBlockState(pos).getBlock().asItem().toString()).matches()) {
            controllers.add(pos);
        }
        if (isPort(getBlockState(pos))) {
            if(getBlockEntity(pos, true) instanceof MultiblockAttachable attachableBe) {
                attachableBe.setMultiblock(this);
            }
            connectedPorts++;
        }
    }

    public void validateInner() {
        innerValid = false;
        for(int y = 1; y < resolveHeight()-1; y++) {
            for(int x = 1; x < resolveWidth()-1; x++) {
                for (int z = 1; z < resolveDepth()-1; z++) {
                    BlockPosInstance toCheck = new BlockPosInstance(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    if (!isValidForInner(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(toCheck);
                        return;
                    }
                    processInnerBlock(toCheck.copy());
                }
            }
        }
        innerValid = true;
        validationResult =  ValidationResult.VALID;
    }

    protected boolean processInnerBlock(BlockPos toCheck) {
        addIfNotExists(toCheck, allBlocks);
        attachMultiblock(toCheck);
        return true;
    }

    protected void attachMultiblock(BlockPos pos) {
        attachMultiblock(getBlockEntity(pos));
    }

    protected BlockEntity getBlockEntity(BlockPos pos, boolean...forceFlag) {
        boolean force = forceFlag.length > 0 && forceFlag[0];
        if (beCache.containsKey(pos.asLong())) {
            if(force) {
                beCache.put(pos.asLong(), getLevel().getExistingBlockEntity(pos));
            }
            return beCache.get(pos.asLong());
        }
        BlockEntity be = getLevel().getExistingBlockEntity(pos);
        if(!getBlockState(pos).isAir()) {
            beCache.put(pos.asLong(), be);
        }
        return be;
    }

    protected void attachMultiblock(BlockEntity be) {
        if (be instanceof MultiblockAttachable part) {
            part.setMultiblock(this);
        }
    }

    public boolean isLoaded(BlockPos pos)
    {
        return getLevel().isLoaded(pos);
    }

    public void onControllerRemoved() {
        for(long packedPos: allBlocks) {
            if (!isLoaded(BlockPos.of(packedPos))) continue;
            BlockEntity be = getBlockEntity(packedPos);
            if (be instanceof MultiblockAttachable<?,?> multiblockAttachable) {
                multiblockAttachable.setMultiblock(null);
            }
        }
        dispose();
    }

    private BlockEntity getBlockEntity(long packedPos) {
        if (beCache.containsKey(packedPos)) {
            return beCache.get(packedPos);
        }
        BlockEntity be = getLevel().getBlockEntity(BlockPos.of(packedPos));
        beCache.put(packedPos, be);
        return be;
    }

    public BlockPos getForwardPos(int i) {
        return initialPos().relative(getControllerDirection(), -i);
    }

    public BlockPos getLeftPos(int i)
    {
        return getSidePos(-i);
    }

    public BlockPos getRightPos(int i)
    {
        return getSidePos(i);
    }

    public BlockPos getSidePos(int i) {
        return switch (getMultiblockDirection().ordinal()) {
            case 3 -> initialPos().east(i);
            case 5 -> initialPos().north(i);
            case 2 -> initialPos().west(i);
            case 4 -> initialPos().south(i);
            default -> null;
        };
    }

    protected abstract Direction getControllerDirection();

    protected Direction getMultiblockDirection() {
        if(multiblockDirection == null) {
            multiblockDirection = getControllerDirection();
        }
        return multiblockDirection;
    }

    @Override
    public void validate() {
        connectedPorts = 0;
        long startTime = System.currentTimeMillis();
        topRight = null;
        bottomLeft = null;
        validationResult = ValidationResult.INCOMPLETE;
        controllers.clear();
        validateOuter();
        if (isOuterValid()) {
            validateInner();
        } else{
            innerValid = false;
            clearStats();
        }
        innerValid = validationResult.isValid;
        isFormed = outerValid && innerValid;
        if (isFormed) {
            validationResult = ValidationResult.VALID;
        } else {
            controller.clearStats();
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        debugLog("NCN validation " + initialPos().toShortString() + " in " + elapsedTime + "ms " + validationResult);
    }

    public boolean isInnerValid() {
        return innerValid;
    }

    public boolean isOuterValid() {
        return outerValid;
    }

    public MultiblockController controller() {
        return controller;
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        //we only update if something changes within the multiblock
        if (shouldRefreshCache(state, pos, neighbor)) {
            hasToRefresh = true;
        }
    }

    private boolean shouldRefreshCache(BlockState state, BlockPos pos, BlockPos neighbor) {
        if (bottomLeft != null && topRight != null) {
            if (neighbor.getX() < bottomLeft.getX() || neighbor.getY() < bottomLeft.getY() || neighbor.getZ() < bottomLeft.getZ() ||
                    neighbor.getX() > topRight.getX() || neighbor.getY() > topRight.getY() || neighbor.getZ() > topRight.getZ()) {
                return false;
            }
        }
        if(!allBlocks.contains(neighbor.asLong())) {
            return false;
        }
        BlockEntity neighborBe = getBlockEntity(neighbor);
        if (neighborBe instanceof MultiblockAttachable<?,?> part) {
            return part.canInvalidateCache();
        }
        return true;
    }
    protected boolean canTick = true;

    public void tick() {
        if(!canTick || !hasToRefresh) return;

        canTick = false;
        validationResult = ValidationResult.INCOMPLETE;
        innerValid = false;
        outerValid = false;
        isFormed = false;
        hasToRefresh = false;
        validate();
        canTick = true;
    }

    public void removeFromCacheIfChanged(BlockPos pos) {
        if (beCache.containsKey(pos.asLong())) {
            BlockEntity be = getLevel().getBlockEntity(pos);
            if(be != beCache.get(pos.asLong())) {
                beCache.remove(pos.asLong());
                hasToRefresh = true;
            }
        }
        if (bsCache.containsKey(pos.asLong())) {
            BlockState bs = getLevel().getBlockState(pos);
            BlockState cachedBs = bsCache.get(pos.asLong());
            if(cachedBs == null || !bs.is(cachedBs.getBlock())) {
                bsCache.remove(pos.asLong());
                hasToRefresh = true;
            }
        }
    }

    public void onBlockDestroyed(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        removeFromCacheIfChanged(pos);
        controller.clearStats();
    }

    public boolean onBlockChange(BlockPos pos) {
        removeFromCacheIfChanged(pos);
        if (hasToRefresh) {
            return true;
        }
        if (containsPos(pos)) {
            BlockState cachedState = getCachedBlockState(pos);
            BlockState actualState = getBlockState(pos);
            if(cachedState == null) {
                hasToRefresh = true;
                return true;
            }
            if (SPECIAL_BLOCKS.matcher(cachedState.getBlock().getDescriptionId()).matches()) {
                if (actualState.is(cachedState.getBlock())) {
                    return true;
                }
            }
            hasToRefresh = true;
            return true;
        }
        resolveDimensions();
        if (bottomLeft == null || topRight == null) return false;
        if (pos.getX() >= bottomLeft.getX() && pos.getY() >= bottomLeft.getY() && pos.getZ() >= bottomLeft.getZ()
                && pos.getX() <= topRight.getX() && pos.getY() <= topRight.getY() && pos.getZ() <= topRight.getZ()) {
            BlockState cachedState = getCachedBlockState(pos);
            BlockState actualState = getBlockState(pos);
            if(cachedState == null) {
                hasToRefresh = true;
                return true;
            }
            if (SPECIAL_BLOCKS.matcher(cachedState.getBlock().getDescriptionId()).matches()) {
                if (actualState.is(cachedState.getBlock())) {
                    return true;
                }
            }
            hasToRefresh = true;
            return true;
        }
        if(!isFormed) {
            hasToRefresh = true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
        return false;
    }

    public boolean isLoaded() {
        if (controllerPos == null) return false;
        if (getLevel() == null) return false;
        return getLevel().getChunkSource().hasChunk(controllerPos.getX() >> 4, controllerPos.getZ() >> 4);
    }

    public ChunkPos getChunk() {
        return new ChunkPos(controllerPos.getX() >> 4, controllerPos.getZ() >> 4);
    }

    public boolean containsPos(BlockPos pos) {
        return allBlocks.contains(pos.asLong());
    }

    public boolean isValidForTicking() {
        return controller() != null && controller().controllerBE() != null;
    }

    public void wipeCache() {
        hasToRefresh = true;
        beCache.clear();
        bsCache.clear();
        allBlocks.clear();
        controllers.clear();
        bottomLeft = null;
        topRight = null;
        errorBlockPos = BlockPos.ZERO;
        validationResult = ValidationResult.INCOMPLETE;
        isFormed = false;
        innerValid = false;
        outerValid = false;
        height = 0;
        width = 0;
        depth = 0;
    }
}
