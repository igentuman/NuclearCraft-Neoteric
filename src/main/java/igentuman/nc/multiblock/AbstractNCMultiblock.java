package igentuman.nc.multiblock;

import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractNCMultiblock implements INCMultiblock {

    protected Class parentBe;
    public boolean hasToRefresh = true;
    protected int refreshCooldown = 50;
    protected int height;
    protected int width;
    protected int depth;
    protected INCMultiblockController controller;
    public ValidationResult validationResult;
    public HashMap<BlockPos, BlockInfo> cachedBlocks = new HashMap<>();

    public int topCasing = 0;
    public int bottomCasing = 0;
    public int leftCasing = 0;
    public int rightCasing = 0;

    protected boolean outerValid = false;
    public boolean refreshOuterCacheFlag = true;
    public boolean refreshInnerCacheFlag = true;
    public boolean isFormed = false;
    protected boolean innerValid = false;
    protected final List<Block> validOuterBlocks;
    protected final List<Block> validInnerBlocks;

    protected AbstractNCMultiblock(List<Block> validOuterBlocks, List<Block> validInnerBlocks) {
        this.validOuterBlocks = validOuterBlocks;
        this.validInnerBlocks = validInnerBlocks;
    }

    public BlockInfo getBlockInfo(BlockPos pos) {
        if(cachedBlocks.containsKey(pos)) {
            return cachedBlocks.get(pos);
        }
        BlockEntity be = getLevel().getBlockEntity(pos);
        BlockState state = getLevel().getBlockState(pos);
        BlockInfo info = new BlockInfo(state, be);
        cachedBlocks.put(pos, info);
        return info;
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
        return isOuterValid() && isInnerValid();
    }
    protected List<BlockPos> allBlocks = new ArrayList<>();

    @Override
    public List<Block> validOuterBlocks() { return validOuterBlocks;  }

    @Override
    public List<Block> validInnerBlocks() { return validInnerBlocks; }

    protected Level getLevel() {
        return  controller().controllerBE().getLevel();
    }
    protected BlockPos controllerPos;
    protected BlockPos controllerPos() {
        if(controllerPos == null) {
            controllerPos = controller().controllerBE().getBlockPos();
        }
        return  NCBlockPos.of(controllerPos);
    }

    public BlockPos getBottomLeftBlock() {
        return getSidePos(leftCasing).above(bottomCasing).relative(getFacing(), -depth + 1);
    }

    public BlockPos getTopRightBlock() {
        return getSidePos(width - rightCasing - 1).above(height - topCasing - 1).relative(getFacing(), -1);
    }

    public boolean isValidForOuter(BlockPos pos)
    {
        if(getLevel() == null) return false;
        try {
            return  validOuterBlocks().contains(getLevel().getBlockState(pos).getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    public int resolveHeight()
    {
        for (int i = 1; i < maxHeight(); i++) {
            if (!isValidForOuter(controllerPos().above(i))) {
                topCasing = i - 1;
                height = i;
                break;
            }
        }
        for (int i = 1; i < maxHeight(); i++) {
            if (!isValidForOuter(controllerPos().below(i))) {
                bottomCasing = i - 1;
                height += i - 1;
                break;
            }
        }

        return height;
    }

    public int resolveWidth()
    {
        for(int i = 1; i<maxWidth(); i++) {
            if(!isValidForOuter(getLeftPos(i))) {
                leftCasing = i-1;
                width = i;
                break;

            }
        }
        for(int i = 1; i<maxWidth(); i++) {
            if(!isValidForOuter(getRightPos(i))) {
                rightCasing = i-1;
                width += i-1;
                break;
            }
        }
        return width;
    }

    public int resolveDepth()
    {
        for(int i = 1; i<maxDepth(); i++) {
            if(!isValidForOuter(getForwardPos(i).above(topCasing))) {
                depth = i;
                break;
            }
        }
        return depth;
    }

    public void resolveDimensions()
    {
        resolveHeight();
        resolveDepth();
        resolveWidth();
    }

    @Override
    public void validateOuter() {
        resolveDimensions();
        if(width < minWidth() || height < minHeight() || depth < minDepth())
        {
            validationResult = ValidationResult.TOO_SMALL;
            return;
        }
        if(width > maxWidth() || height > maxHeight() || depth > maxDepth())
        {
            validationResult = ValidationResult.TOO_BIG;
            return;
        }
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if(y == 0 || x == 0 || z == 0 || y == height-1 || x == width-1 || z == depth-1) {
                        if (!isValidForOuter(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z))) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            controller().addErroredBlock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                            return;
                        }
                        attachMultiblock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                        allBlocks.add(new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z)));
                    }
                }
            }
        }
        validationResult = ValidationResult.VALID;
    }

    public void validateInner() {
        invalidateStats();
        for(int y = 1; y < resolveHeight()-1; y++) {
            for(int x = 1; x < resolveWidth()-1; x++) {
                for (int z = 1; z < resolveDepth()-1; z++) {
                    BlockPos toCheck = new NCBlockPos(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                    if (isValidForOuter(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        controller().addErroredBlock(toCheck);
                        return;
                    }
                    validateInnerBlock(toCheck);
                    allBlocks.add(toCheck);
                }
            }
        }

        validationResult =  ValidationResult.VALID;
    }

    protected abstract boolean validateInnerBlock(BlockPos toCheck);

    protected abstract void invalidateStats();

    protected void attachMultiblock(BlockPos pos) {
        attachMultiblock(getLevel().getBlockEntity(pos));
    }

    protected void attachMultiblock(BlockEntity be) {
        if(be instanceof IMultiblockAttachable part) {
            part.setMultiblock(this);
        }
    }

    public void onControllerRemoved() {
        for(BlockPos b: allBlocks) {
            BlockEntity be = getLevel().getBlockEntity(b);
            if(be instanceof IMultiblockAttachable) {
                ((IMultiblockAttachable) be).setMultiblock(null);
            }
        }
    }

    public BlockPos getForwardPos(int i) {
        return controllerPos().relative(getFacing(), -i);
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
        return switch (getFacing().ordinal()) {
            case 3 -> controllerPos().east(i);
            case 5 -> controllerPos().north(i);
            case 2 -> controllerPos().west(i);
            case 4 -> controllerPos().south(i);
            default -> null;
        };
    }

    protected abstract Direction getFacing();

    @Override
    public void validate() {
        refreshOuterCacheFlag = true;
        refreshInnerCacheFlag = true;
        allBlocks.clear();

        isOuterValid();
        validateInner();
        innerValid = validationResult.isValid;
        //todo more checks before?
        isFormed = outerValid && innerValid;
    }

    public boolean isInnerValid() {
        if(refreshOuterCacheFlag) return false;
        if(refreshInnerCacheFlag) {
            validateInner();
            refreshInnerCacheFlag = !validationResult.isValid;
            innerValid = validationResult.isValid;
        }
        return innerValid;
    }

    public boolean isOuterValid() {
        if(refreshOuterCacheFlag) {
            validateOuter();
            refreshOuterCacheFlag = !validationResult.isValid;
            outerValid = validationResult.isValid;
        }
        return outerValid;
    }

    public INCMultiblockController controller() {
        return controller;
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        //we only update if something changes within the multiblock
        if(shouldRefreshCache(state, pos, neighbor)) {
            hasToRefresh = true;
        }
    }

    private boolean shouldRefreshCache(BlockState state, BlockPos pos, BlockPos neighbor) {
        boolean isInTheList = allBlocks.contains(neighbor);
        BlockEntity neighborBe = getLevel().getBlockEntity(neighbor);
        if(!isInTheList) return false; //ignore all blocks outside
        if(neighborBe instanceof IMultiblockAttachable part) {
            return part.canInvalidateCache();
        }
        return true;
    }

    public void tick() {
        //not letting to spam structure re validation
        if(hasToRefresh) {
            refreshCooldown--;
            if(refreshCooldown <= 0) {
                refreshCooldown = 10;
                refreshOuterCacheFlag = true;
                refreshInnerCacheFlag = true;
                isFormed = false;
                hasToRefresh = false;
            }
        }
    }
}
