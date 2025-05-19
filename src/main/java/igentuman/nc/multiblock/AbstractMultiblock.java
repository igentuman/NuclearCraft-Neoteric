package igentuman.nc.multiblock;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.api.nc.multiblock.Multiblock;
import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.debugLog;

public abstract class AbstractMultiblock implements Multiblock {

    public boolean hasToRefresh = true;
    public BlockPos errorBlockPos = BlockPos.ZERO;
    protected int refreshCooldown = 20;
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
    protected NCBlockPos bottomLeft;
    protected NCBlockPos topRight;
    protected boolean outerValid = false;
    protected boolean isFormed = false;
    protected boolean innerValid = false;
    protected final List<Block> validOuterBlocks;
    protected final List<Block> validInnerBlocks;
    protected final List<BlockPos> controllers = new ArrayList<>();
    protected final HashMap<Long, BlockEntity> beCache = new HashMap<>();
    protected final HashMap<Long, BlockState> bsCache = new HashMap<>();
    protected final List<BlockPos> allBlocks = new ArrayList<>();
    protected NCBlockPos controllerPos;
    protected NCBlockPos initialPos;
    protected Direction multiblockDirection;
    private static final Pattern SPECIAL_BLOCKS = Pattern.compile(".*(fusion_proxy|fusion_core|controller|port|irradiator|rotor|chamber_terminal).*");
    private static final Pattern CONTROLLERS = Pattern.compile(".*(controller|terminal).*");

    protected AbstractMultiblock(List<Block> validOuterBlocks, List<Block> validInnerBlocks, MultiblockController controller) {
        this.validOuterBlocks = validOuterBlocks;
        this.validInnerBlocks = validInnerBlocks;
        this.controller = controller;
        controllerPos = NCBlockPos.of(controller().controllerBE().getBlockPos());
    }

    public void dispose() {
        MultiblockHandler.get(getLevel().dimension()).removeMultiblock(this);
    }

    public List<Block> validCornerBlocks() {
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

    @Override
    public List<Block> validOuterBlocks() { return validOuterBlocks;  }

    @Override
    public List<Block> validInnerBlocks() { return validInnerBlocks; }

    protected Level getLevel() {
        if (controller() == null || controller().controllerBE() == null) {
            return null;
        }
        return controller().controllerBE().getLevel();
    }

    protected NCBlockPos initialPos() {
        if (controllerPos == null) {
            controllerPos = NCBlockPos.copy(controller().controllerBE().getBlockPos());
        }
        if (initialPos == null) {
            initialPos = NCBlockPos.copy(controllerPos);
        }
        return initialPos.revert();
    }

    public BlockPos getBottomLeftBlock() {
        if (controllerPos != null) {
            controllerPos.revert();
        }
        return new BlockPos(getLeftPos(leftCasing).below(bottomCasing).relative(getControllerDirection(), -depth+1));
    }

    public BlockPos getBottomLeftInnerBlock() {
        if (controllerPos != null) {
            controllerPos.revert();
        }
        return new BlockPos(getLeftPos(leftCasing-1).below(bottomCasing-1).relative(getControllerDirection(), -depth+2));
    }

    public BlockPos getTopRightBlock() {
        if (controllerPos != null) {
            controllerPos.revert();
        }
        return new BlockPos(getRightPos(rightCasing).above(topCasing));
    }

    public BlockPos getTopRightInnerBlock() {
        if (controllerPos != null) {
            controllerPos.revert();
        }
        return new BlockPos(getRightPos(rightCasing-1).above(topCasing-1).relative(getControllerDirection(), -1));
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

    protected BlockState getBlockState(BlockPos pos) {
        if (bsCache.containsKey(pos.asLong())) {
            return bsCache.get(pos.asLong());
        }
        BlockState state = getLevel().getBlockState(pos);
        bsCache.put(pos.asLong(), state);
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
        if (getLevel() == null) return false;
        try {
            return  validCornerBlocks().contains(getBlockState(pos).getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    public boolean isValidForInner(BlockPos pos)
    {
        if (getLevel() == null) return false;
        try {
            BlockState bs = getBlockState(pos);
            if (bs.isAir()) return true;
            return  validInnerBlocks().contains(bs.getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    public int resolveHeight()
    {
        for (int i = 1; i < maxHeight(); i++) {
            if (!isValidForOuter(initialPos().above(i))) {
                topCasing = i - 1;
                height = i;
                break;
            }
            updateDimensions(initialPos().above(i));
        }
        for (int i = 1; i < maxHeight(); i++) {
            if (!isValidForOuter(initialPos().below(i))) {
                bottomCasing = i - 1;
                height += i - 1;
                break;
            }
            updateDimensions(initialPos().below(i));
        }

        return height;
    }

    public int resolveWidth()
    {
        for(int i = 1; i<maxWidth(); i++) {
            if (!isValidForOuter(getLeftPos(i).above(topCasing))) {
                leftCasing = i-1;
                width = i;
                updateDimensions(getLeftPos(i-1).above(topCasing));
                break;
            }
        }
        for(int i = 1; i<maxWidth(); i++) {
            if (!isValidForOuter(getRightPos(i).above(topCasing))) {
                rightCasing = i-1;
                width += i-1;
                updateDimensions(getRightPos(i-1).above(topCasing));
                break;
            }
        }
        return width;
    }

    public int resolveDepth()
    {
        for(int i = 1; i<maxDepth(); i++) {
            if (!isValidForOuter(getForwardPos(i).above(topCasing))) {
                depth = i;
                break;
            }
            updateDimensions(getForwardPos(i).above(topCasing));
        }
        return depth;
    }

    public void resolveDimensions()
    {
        if (getMultiblockDirection() == null)  return;
        resolveHeight();
        resolveDepth();
        resolveWidth();
    }

    @Override
    public void validateOuter() {
        outerValid = false;
        resolveDimensions();
        if (width < minWidth() || height < minHeight() || depth < minDepth())
        {
            validationResult = ValidationResult.TOO_SMALL;
            return;
        }
        if (width > maxWidth() || height > maxHeight() || depth > maxDepth())
        {
            validationResult = ValidationResult.TOO_BIG;
            return;
        }
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        if (!isValidForOuter(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z))) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            errorBlockPos = new BlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                            return;
                        }
                        processOuterBlock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                        //validate corner blocks
                        if (((y == 0 || y == height-1) && (z == 0 || z == depth - 1))
                        || ((y == 0 || y == height-1) && (x == 0 || x == width - 1))
                        || ((z == 0 || z == depth-1) && (x == 0 || x == width - 1))
                        ) {
                            if (!isValidCorner(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z))) {
                                validationResult = ValidationResult.WRONG_CORNER;
                                errorBlockPos = new BlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
                                return;
                            }
                        }
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

    protected void updateDimensions(BlockPos pos) {
        if (topRight == null) {
            topRight = new NCBlockPos(pos);
        }
        if (bottomLeft == null) {
            bottomLeft = new NCBlockPos(pos);
        }
        if (pos.getX() <= bottomLeft.getX() && pos.getY() <= bottomLeft.getY() && pos.getZ() <= bottomLeft.getZ()) {
            bottomLeft.x(pos.getX());
            bottomLeft.y(pos.getY());
            bottomLeft.z(pos.getZ());
        }
        if (pos.getX() >= topRight.getX() && pos.getY() >= topRight.getY() && pos.getZ() >= topRight.getZ()) {
            topRight.x(pos.getX());
            topRight.y(pos.getY());
            topRight.z(pos.getZ());
        }
    }

    protected void processOuterBlock(BlockPos pos) {
        attachMultiblock(pos);
        updateDimensions(pos);
        allBlocks.add(new BlockPos(pos));
        if (CONTROLLERS.matcher(getBlockState(pos).getBlock().asItem().toString()).matches()) {
            controllers.add(pos);
        }
    }

    public void validateInner() {
        innerValid = false;
        for(int y = 1; y < resolveHeight()-1; y++) {
            for(int x = 1; x < resolveWidth()-1; x++) {
                for (int z = 1; z < resolveDepth()-1; z++) {
                    NCBlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z));
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
        allBlocks.add(new BlockPos(toCheck));
        attachMultiblock(toCheck);
        return true;
    }

    protected void attachMultiblock(BlockPos pos) {
        attachMultiblock(getBlockEntity(pos));
    }

    protected BlockEntity getBlockEntity(BlockPos pos) {
        if (beCache.containsKey(pos.asLong())) {
            return beCache.get(pos.asLong());
        }
        BlockEntity be = getLevel().getExistingBlockEntity(pos);
        beCache.put(pos.asLong(), be);
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
        for(BlockPos b: allBlocks) {
            if (!isLoaded(b)) continue;
            BlockEntity be = getBlockEntity(b);
            if (be instanceof MultiblockAttachable multiblockAttachable) {
                multiblockAttachable.setMultiblock(null);
            }
        }
        dispose();
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
            case 3 -> initialPos().revert().east(i);
            case 5 -> initialPos().revert().north(i);
            case 2 -> initialPos().revert().west(i);
            case 4 -> initialPos().revert().south(i);
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
        long startTime = System.currentTimeMillis();
        topRight = null;
        bottomLeft = null;
        validationResult = ValidationResult.INCOMPLETE;
        allBlocks.clear();
        controllers.clear();
        bsCache.clear();
        beCache.clear();
        validateOuter();
        if (isOuterValid()) {
            validateInner();
        } else{
            innerValid = false;
            invalidateStats();
        }
        innerValid = validationResult.isValid;
        isFormed = outerValid && innerValid;
        if (isFormed) {
            validationResult = ValidationResult.VALID;
        } else {
            controller.clearStats();
        }
        //hasToRefresh = !isFormed;
        
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
        if(!allBlocks.contains(neighbor)) {
            return false;
        }
        BlockEntity neighborBe = getBlockEntity(neighbor);
        if (neighborBe instanceof MultiblockAttachable part) {
            return part.canInvalidateCache();
        }
        return true;
    }
    boolean canTick = true;

    public void tick() {
        if(!canTick) return;

        if (!hasToRefresh) {
            return;
        }
        if(refreshCooldown-- >= 0) {
            return;
        }
        canTick = false;

        validationResult = ValidationResult.INCOMPLETE;
        innerValid = false;
        outerValid = false;
        isFormed = false;
        hasToRefresh = false;
        beCache.clear();
        bsCache.clear();
        refreshCooldown = 10;
        validate();
        canTick = true;
    }

    public void onBlockDestroyed(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        controller.clearStats();
    }

    public boolean onBlockChange(BlockPos pos) {
        if (hasToRefresh) return true;
        if (containsPos(pos)) {
            BlockState targetBlock = getBlockState(pos);
            if (SPECIAL_BLOCKS.matcher(targetBlock.getBlock().getDescriptionId()).matches()) {
                if (getLevel().getBlockState(pos).is(targetBlock.getBlock())) {
                    return true;
                }
            }
            hasToRefresh = true;
            if(getLevel().getBlockState(pos).isAir()) {
                controller.clearStats();
            }
            return true;
        }
        resolveDimensions();
        if (bottomLeft == null || topRight == null) return false;
        if (pos.getX() >= bottomLeft.getX() && pos.getY() >= bottomLeft.getY() && pos.getZ() >= bottomLeft.getZ()
                && pos.getX() <= topRight.getX() && pos.getY() <= topRight.getY() && pos.getZ() <= topRight.getZ()) {
            Block targetBlock = getBlockState(pos).getBlock();
            if (SPECIAL_BLOCKS.matcher(targetBlock.getDescriptionId()).matches()) {
                if (getLevel().getBlockState(pos).is(targetBlock)) {
                    return true;
                }
            }
            hasToRefresh = true;
            if(getLevel().getBlockState(pos).isAir()) {
                controller.clearStats();
            }
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
        return allBlocks.contains(pos);
    }

    public boolean isValidForTicking() {
        return controller() != null && controller().controllerBE() != null;
    }
}
