package igentuman.nc.multiblock;

import igentuman.api.platform.NCNames;
import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.api.nc.multiblock.Multiblock;
import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.MultiblockPortBE;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.LOGGER;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.CommonConfig.MISC_CONFIG;
import static net.minecraft.world.level.block.Blocks.AIR;

public abstract class AbstractMultiblock implements Multiblock {

    protected int failedValidations = 0;
    protected boolean toDeleteFlag = false;
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
    protected final List<TagKey<Block>> outerTags = new ArrayList<>();
    protected final List<TagKey<Block>> innerTags = new ArrayList<>();
    protected final HashSet<BlockPos> controllers = new HashSet<>();
    protected final HashSet<BlockPos> updatedBlocks = new HashSet<>();
    protected final HashMap<Long, BlockEntity> beCache = new HashMap<>();
    protected final HashMap<Long, BlockState> bsCache = new HashMap<>(10000);
    protected final HashSet<Long> allBlocks = new HashSet<>(10000);
    protected BlockPosInstance controllerPos;
    protected BlockPosInstance initialPos;
    protected Direction multiblockDirection;
    protected MultiblockControllerBE controllerBe;
    private static final Pattern SPECIAL_BLOCKS = Pattern.compile(".*(fusion_proxy|fusion_core|controller|port|irradiator|rotor|chamber_terminal).*");
    private static final Pattern CONTROLLERS = Pattern.compile(".*(controller|terminal).*");
    private Level level;
    protected AABB structureBounds;
    public boolean isValidating = true;
    protected boolean fullValidation = true;
    public final HashSet<MultiblockPortBE> ports = new HashSet<>();
    protected AbstractMultiblock(HashSet<Block> validOuterBlocks, HashSet<Block> validInnerBlocks, MultiblockController controller) {
        this.validOuterBlocks = validOuterBlocks;
        this.validInnerBlocks = validInnerBlocks;
        this.controller = controller;
        controllerPos = BlockPosInstance.of(controller().controllerBE().getBlockPos());

        debugLog("Created " + getClass().getSimpleName() + " at " + controllerPos.toShortString() +
                " with " + validOuterBlocks.size() + " valid outer blocks and " +
                validInnerBlocks.size() + " valid inner blocks");
    }

    protected AbstractMultiblock(TagKey<Block> outerTag, HashSet<Block> extraOuterBlocks,
                                 TagKey<Block> innerTag, HashSet<Block> extraInnerBlocks,
                                 MultiblockController controller) {
        this.validOuterBlocks = extraOuterBlocks != null ? extraOuterBlocks : new HashSet<>();
        this.validInnerBlocks = extraInnerBlocks != null ? extraInnerBlocks : new HashSet<>();
        if (outerTag != null) this.outerTags.add(outerTag);
        if (innerTag != null) this.innerTags.add(innerTag);
        this.controller = controller;
        controllerPos = BlockPosInstance.of(controller().controllerBE().getBlockPos());

        debugLog("Created " + getClass().getSimpleName() + " at " + controllerPos.toShortString() +
                " with outer tag " + (outerTag != null ? outerTag.location() : "none") +
                " and inner tag " + (innerTag != null ? innerTag.location() : "none") +
                " (+" + validOuterBlocks.size() + " explicit outer, +" + validInnerBlocks.size() + " explicit inner)");
    }

    public boolean isMarkedForRemoval() {
        return toDeleteFlag;
    }

    public void setForRemoval() {
        toDeleteFlag = true;
    }

    public void dispose() {
        debugLog("Disposing multiblock " + getClass().getSimpleName() + " at " + controllerPos.toShortString());
        
        clearCaches();
        
        MultiblockHandler.get(getLevel().dimension()).removeMultiblock(this);
    }

    public void clearCaches() {
        bsCache.clear();
        beCache.clear();
        allBlocks.clear();
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
        return NCNames.of(bs.getBlock().asItem()).contains("port");
    }

    @Override
    public HashSet<Block> validOuterBlocks() { return validOuterBlocks;  }

    @Override
    public HashSet<Block> validInnerBlocks() { return validInnerBlocks; }

    protected Level getLevel() {
        if(level instanceof ServerLevel) return  level;
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

        BlockPos blockPos = BlockPos.of(pos);
        BlockState state = getLevel().getBlockState(blockPos);
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
        if (getLevel() == null) {
            return false;
        }
        try {
            BlockState state = getBlockState(pos);
            Block block = state.getBlock();
            if (validOuterBlocks().contains(block)) return true;
            for (TagKey<Block> tag : outerTags) {
                if (state.is(tag)) return true;
            }
        } catch (NullPointerException e) {
            debugLog("NullPointerException when checking outer block at " + pos.toShortString() + ": " + e.getMessage());
        }
        return false;
    }

    public boolean isValidCorner(BlockPos pos)
    {
        try {
            BlockState state = getBlockState(pos);
            Block block = state.getBlock();
            if (validCornerBlocks().contains(block)) return true;
            // NeoForge 1.21.1: check outer tags for corners too (corners are a subset of outer)
            for (TagKey<Block> tag : outerTags) {
                if (state.is(tag)) return true;
            }
        } catch (NullPointerException ignored) { }
        return false;
    }

    public boolean isValidForInner(BlockState bs)
    {
        if (bs.isAir()) return true;
        if (validInnerBlocks().contains(bs.getBlock())) return true;
        // NeoForge 1.21.1: check tag membership via BlockState.is(TagKey)
        for (TagKey<Block> tag : innerTags) {
            if (bs.is(tag)) return true;
        }
        return false;
    }

    public boolean isValidForInner(BlockPos pos)
    {
        if (getLevel() == null) {
            debugLog("Level is null when checking inner block at " + pos.toShortString());
            return false;
        }
        return isValidForInner(getBlockState(pos));
    }

    public void cacheBlockStates(AABB excludeArea) {
        if(!fullValidation) {
            return;
        }

        long startTime = System.currentTimeMillis();
        int minX = bottomLeft.getX();
        int minY = bottomLeft.getY();
        int minZ = bottomLeft.getZ();
        int maxX = topRight.getX();
        int maxY = topRight.getY();
        int maxZ = topRight.getZ();

        debugLog("Caching block states for area: " + minX + "," + minY + "," + minZ + " to " + maxX + "," + maxY + "," + maxZ);

        Level level = getLevel();
        int cachedBlocks = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if(excludeArea != null && excludeArea.contains(new Vec3(x,y,z))) {
                        continue;
                    }
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);
                    bsCache.put(BlockPos.asLong(x, y, z), state);
                    cachedBlocks++;
                }
            }
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        debugLog("Cached " + cachedBlocks + " block states in " + elapsedTime + "ms");
    }

    public int resolveHeight()
    {
        BlockPos start = immutableInitialPos();
        for (int i = 1; i <= maxHeight()+2; i++) {
            BlockPos probe = start.above(i);
            if (!isValidForOuter(probe)) {
                topCasing = i - 1;
                height = i;
                LOGGER.info("[NC-DIAG] Height UP: {} valid, stopped at {} ({})",
                        i - 1, probe.toShortString(), getBlockState(probe).getBlock().getDescriptionId());
                break;
            }
        }
        for (int i = 1; i <= maxHeight()+2; i++) {
            BlockPos probe = start.below(i);
            if (!isValidForOuter(probe)) {
                bottomCasing = i - 1;
                height += i - 1;
                LOGGER.info("[NC-DIAG] Height DOWN: {} valid, stopped at {} ({})",
                        i - 1, probe.toShortString(), getBlockState(probe).getBlock().getDescriptionId());
                break;
            }
        }
        LOGGER.info("[NC-DIAG] Height resolved: {} (top={}, bottom={})", height, topCasing, bottomCasing);
        return height;
    }

    public int resolveWidth()
    {
        for(int i = 1; i <= maxWidth()+2; i++) {
            BlockPos probe = getLeftPos(i).above(topCasing);
            if (!isValidForOuter(probe)) {
                leftCasing = i-1;
                width = i;
                LOGGER.info("[NC-DIAG] Width LEFT: {} valid, stopped at {} ({})",
                        i - 1, probe.toShortString(), getBlockState(probe).getBlock().getDescriptionId());
                break;
            }
        }
        for(int i = 1; i <= maxWidth()+2; i++) {
            BlockPos probe = getRightPos(i).above(topCasing);
            if (!isValidForOuter(probe)) {
                rightCasing = i-1;
                width += i-1;
                LOGGER.info("[NC-DIAG] Width RIGHT: {} valid, stopped at {} ({})",
                        i - 1, probe.toShortString(), getBlockState(probe).getBlock().getDescriptionId());
                break;
            }
        }
        LOGGER.info("[NC-DIAG] Width resolved: {} (left={}, right={})", width, leftCasing, rightCasing);
        return width;
    }

    public int resolveDepth()
    {
        for(int i = 1; i <= maxDepth()+2; i++) {
            BlockPos probe = getForwardPos(i).below(bottomCasing);
            if (!isValidForOuter(probe)) {
                depth = i;
                LOGGER.info("[NC-DIAG] Depth FORWARD: {} valid, stopped at {} ({})",
                        i - 1, probe.toShortString(), getBlockState(probe).getBlock().getDescriptionId());
                break;
            }
        }
        LOGGER.info("[NC-DIAG] Depth resolved: {}", depth);
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

    public void updateAABB()
    {
        if(bottomLeft == null || topRight == null) {
            findCorners();
        }
        structureBounds = AABB.encapsulatingFullBlocks(bottomLeft, topRight);
    }

    @Override
    public void validateOuter() {
        outerValid = false;
        debugLog("Starting outer validation for multiblock at " + controllerPos.toShortString() + " (Type: " + getClass().getSimpleName() + ")");
        
        resolveDimensions();
        debugLog("Resolved dimensions: " + width + "x" + height + "x" + depth + " (WxHxD)");

        if (width > maxWidth() || height > maxHeight() || depth > maxDepth())
        {
            validationResult = ValidationResult.TOO_BIG;
            debugLog("Validation failed - TOO_BIG: " + width + "x" + height + "x" + depth + " exceeds max " + maxWidth() + "x" + maxHeight() + "x" + maxDepth());
            return;
        }

        if (width < minWidth() || height < minHeight() || depth < minDepth())
        {
            validationResult = ValidationResult.TOO_SMALL;
            LOGGER.info("[NC-DIAG] TOO_SMALL: resolved {}W x {}H x {}D, min is {}W x {}H x {}D",
                    width, height, depth, minWidth(), minHeight(), minDepth());
            return;
        }

        findCorners();

        cacheBlockStates(null);
        debugLog("Cached block states for validation area. Corners: " + bottomLeft.toShortString() + " to " + topRight.toShortString() +
                ", Total cached: " + bsCache.size());
        
        int totalOuterBlocks = 0;
        int validOuterBlocks = 0;
        int cornerBlocks = 0;
        int validCornerBlocks = 0;
        Direction controllerDirection = getControllerDirection();
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        BlockPos currentPos = getSidePos(x - leftCasing).above(y - bottomCasing).relative(controllerDirection, -z);
                        totalOuterBlocks++;
                        
                        if (((y == 0 || y == height-1) && (z == 0 || z == depth - 1))
                        || ((y == 0 || y == height-1) && (x == 0 || x == width - 1))
                        || ((z == 0 || z == depth-1) && (x == 0 || x == width - 1))
                        ) {
                            cornerBlocks++;
                            if (!isValidCorner(currentPos)) {
                                validationResult = ValidationResult.WRONG_CORNER;
                                errorBlockPos = new BlockPos(currentPos);
                                debugLog("Validation failed - WRONG_CORNER at " + currentPos.toShortString() + 
                                        " - Expected corner block, found: " + getBlockState(currentPos).getBlock().getDescriptionId());
                                return;
                            }
                            validCornerBlocks++;
                        } else if (!isValidForOuter(currentPos)) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            errorBlockPos = new BlockPos(currentPos);
                            debugLog("Validation failed - WRONG_OUTER at " + currentPos.toShortString() + 
                                    " - Expected outer block, found: " + getBlockState(currentPos).getBlock().getDescriptionId());
                            return;
                        } else {
                            validOuterBlocks++;
                        }
                        processOuterBlock(currentPos);
                    }
                }
            }
        }
        
        debugLog("Outer block validation complete - Total: " + totalOuterBlocks + 
                ", Valid outer: " + validOuterBlocks + ", Corner blocks: " + cornerBlocks + 
                ", Valid corners: " + validCornerBlocks);
        
        if (controllers.size() > 1) {
            validationResult = ValidationResult.TOO_MANY_CONTROLLERS;
            debugLog("Validation failed - TOO_MANY_CONTROLLERS: Found " + controllers.size() + " controllers");
            return;
        }
        
        debugLog("Found " + connectedPorts + " connected ports");
        outerValid = true;
        validationResult = ValidationResult.VALID;
        debugLog("Outer validation completed successfully");
        updateAABB();
    }

    private void findCorners() {
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
        debugLog("Calculated bounds: bottomLeft=" + bottomLeft.toShortString() + ", topRight=" + topRight.toShortString());
    }

    protected void processOuterBlock(BlockPos pos) {
        attachMultiblock(pos);
        addIfNotExists(pos, allBlocks);
        if (CONTROLLERS.matcher(NCNames.of(getBlockState(pos).getBlock().asItem())).matches()) {
            controllers.add(pos);
        }
        if (isPort(getBlockState(pos))) {
            if(getBlockEntity(pos, true) instanceof MultiblockAttachable attachableBe) {
                attachableBe.setMultiblock(this);
            }
            ports.add((MultiblockPortBE) getBlockEntity(pos, true));
            connectedPorts++;
        }
    }

    public void validateInner() {
        validateInner(false);
    }

    public void validateInner(boolean force) {
        innerValid = false;
        debugLog("Starting inner validation for multiblock at " + controllerPos.toShortString());
        
        int totalInnerBlocks = 0;
        int validInnerBlocks = 0;
        int airBlocks = 0;
        
        for(int y = 1; y < resolveHeight()-1; y++) {
            for(int x = 1; x < resolveWidth()-1; x++) {
                for (int z = 1; z < resolveDepth()-1; z++) {
                    BlockPosInstance toCheck = new BlockPosInstance(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                    totalInnerBlocks++;
                    
                    if (!isValidForInner(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(toCheck);
                        debugLog("Validation failed - WRONG_INNER at " + toCheck.toShortString() + 
                                " - Invalid inner block: " + getBlockState(toCheck).getBlock().getDescriptionId());
                        return;
                    }
                    
                    if (getBlockState(toCheck).isAir()) {
                        airBlocks++;
                    } else {
                        validInnerBlocks++;
                    }
                    
                    processInnerBlock(toCheck.copy());
                }
            }
        }
        
        debugLog("Inner validation complete - Total: " + totalInnerBlocks + 
                ", Valid inner blocks: " + validInnerBlocks + ", Air blocks: " + airBlocks);
        
        innerValid = true;
        validationResult = ValidationResult.VALID;
        debugLog("Inner validation completed successfully");
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
        final long packedPos = pos.asLong();

        if (beCache.containsKey(packedPos)) {
            if(force) {
                BlockEntity be = getLevel().getBlockEntity(pos);
                if(be != null) {
                    beCache.put(packedPos, be);
                } else {
                    return null;
                }
            }
            return beCache.get(packedPos);
        }

        BlockEntity be = getLevel().getBlockEntity(pos);
        if(be != null) {
            beCache.put(packedPos, be);
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

    private BlockPos immutableInitialPos() {
        BlockPosInstance p = initialPos();
        return new BlockPos(p.getX(), p.getY(), p.getZ());
    }

    public BlockPos getForwardPos(int i) {
        return immutableInitialPos().relative(getControllerDirection(), -i);
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
        BlockPos pos = immutableInitialPos();
        return switch (getMultiblockDirection().ordinal()) {
            case 3 -> pos.east(i);
            case 5 -> pos.north(i);
            case 2 -> pos.west(i);
            case 4 -> pos.south(i);
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
        failedValidations++;
        if(failedValidations > 10) {
            toDeleteFlag = true;
            return;
        }
        isValidating = true;
        connectedPorts = 0;
        long startTime = System.currentTimeMillis();
        LOGGER.info("[NC-DIAG] === Validation #{} for {} at {} === thread={}, outerTags={}, innerTags={}, explicitOuter={}, explicitInner={}, controllerBlock={}",
                failedValidations, getClass().getSimpleName(), initialPos().toShortString(),
                Thread.currentThread().getName(),
                outerTags.size(), innerTags.size(), validOuterBlocks.size(), validInnerBlocks.size(),
                getLevel() != null ? getLevel().getBlockState(initialPos()).getBlock().getDescriptionId() : "null-level");

        topRight = null;
        bottomLeft = null;
        validationResult = ValidationResult.INCOMPLETE;
        controllers.clear();

        debugLog("Cleared validation caches and reset state");

        validateOuter();
        if (isOuterValid()) {
            debugLog("Outer validation passed, proceeding to inner validation");
            validateInner();
        } else{
            LOGGER.info("[NC-DIAG] Outer validation FAILED: {} at {}", validationResult,
                    errorBlockPos != null ? errorBlockPos.toShortString() : "unknown");
            debugLog("Outer validation failed with result: " + validationResult +
                    (errorBlockPos != null ? " at " + errorBlockPos.toShortString() : ""));
            innerValid = false;
            clearStats();
        }

        innerValid = validationResult.isValid;
        isFormed = outerValid && innerValid;

        if (isFormed) {
            failedValidations--;
            validationResult = ValidationResult.VALID;
            fullValidation = false;
            LOGGER.info("[NC-DIAG] Multiblock FORMED: {} at {}", getClass().getSimpleName(), initialPos().toShortString());
            debugLog("Multiblock formation successful!");
        } else {
            controller.clearStats();
            LOGGER.info("[NC-DIAG] Multiblock FAILED: {} - result={}", getClass().getSimpleName(), validationResult);
            debugLog("Multiblock formation failed - Outer valid: " + outerValid + ", Inner valid: " + innerValid);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        controllerBE().validationTime = elapsedTime;
        controllerBE().validationsCounter++;
        debugLog("=== Validation completed for " + getClass().getSimpleName() + " at " + initialPos().toShortString() +
                " in " + elapsedTime + "ms - Result: " + validationResult + " ===");
        isValidating = false;
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

    public void tick(Level level) {
        if(controllerBE() != null) {
            controllerBE().multiblockTicksCounter++;
        }
        HashSet<BlockPos> changedBlocks = new HashSet<>(updatedBlocks);
        updatedBlocks.clear();
        for(BlockPos pos: changedBlocks) {
            removeFromCacheIfChanged(pos);
        }
        if(!hasToRefresh) return;
        this.level = level;
        hasToRefresh = false;

        debugLog("Tick triggered validation for " + getClass().getSimpleName() + " at " + controllerPos.toShortString());

        validate();
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
        updatedBlocks.add(new BlockPos(pos));
    }

    public void onBlockChange(BlockPos pos) {
        updatedBlocks.add(new BlockPos(pos));
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
        return allBlocks.contains(pos.asLong()) || inAABB(pos);
    }

    private boolean inAABB(BlockPos pos) {
        if(structureBounds == null) return false;
        return structureBounds.contains(pos.getCenter());
    }

    public void wipeCache() {
        if(isValidating) return;
        fullValidation = true;
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

    public boolean isValidating() {
        return isValidating;
    }

    public HashSet<MultiblockPortBE> getPorts() {
        return ports;
    }
}
