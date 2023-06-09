package igentuman.nc.multiblock;

import igentuman.nc.block.entity.fission.FissionBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNCMultiblock implements INCMultiblock {

    protected Class parentBe;
    protected int height;
    protected int width;
    protected int depth;
    protected INCMultiblockController controller;
    public ValidationResult validationResult;

    public int topCasing = 0;
    public int bottomCasing = 0;
    public int leftCasing = 0;
    public int rightCasing = 0;

    protected boolean outerValid = false;
    public boolean refreshOuterCacheFlag = true;
    public boolean refreshInnerCacheFlag = true;
    protected boolean isFormed = false;
    private boolean innerValid = false;

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
    protected List<BlockPos> allBlocks = new ArrayList<>();

    public List<Block> validOuterBlocks() {
        return List.of();
    }

    public List<Block> validInnerBlocks() {
        return List.of();
    }

    protected Level getLevel() {
        return  controller().controllerBE().getLevel();
    }

    protected BlockPos controllerPos() {
        return controller().controllerBE().getBlockPos();
    }
    public void validateOuter() {

    }

    public void validateInner() {

    }

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
        //neighbor inside box
        if(neighbor.equals(controllerPos()) || !allBlocks.contains(neighbor)) return;
        refreshInnerCacheFlag = true;
        refreshOuterCacheFlag = true;
        isFormed = false;
    }
}
