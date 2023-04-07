package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class AbstractNCMultiblock implements INCMultiblock {

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
    private boolean isFormed = false;
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
}
