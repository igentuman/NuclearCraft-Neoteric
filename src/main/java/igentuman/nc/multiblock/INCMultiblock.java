package igentuman.nc.multiblock;

import net.minecraft.world.level.block.Block;

import java.util.List;

public interface INCMultiblock {
    boolean isInnerValid();
    boolean isOuterValid();
    boolean isFormed();
    int height();
    int width();
    int depth();

    int maxHeight();
    int minHeight();
    int maxWidth();
    int minWidth();
    int maxDepth();
    int minDepth();

    List<Block> validOuterBlocks();
    List<Block> validInnerBlocks();

    void validateOuter();
    void validateInner();

    void validate();

    INCMultiblockController controller();



}
