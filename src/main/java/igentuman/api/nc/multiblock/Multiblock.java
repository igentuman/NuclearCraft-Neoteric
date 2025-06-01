package igentuman.api.nc.multiblock;

import net.minecraft.world.level.block.Block;
import java.util.List;

public interface Multiblock {

    /**
     * Returns if the inner part of the multiblock is valid
     */
    boolean isInnerValid();

    /**
     * Returns if the outer part of the multiblock is valid
     */
    boolean isOuterValid();

    /**
     * Returns if the multiblock is formed
     */
    boolean isFormed();

    /**
     * Returns if the multiblock chunk is loaded
     */
    boolean isLoaded();

    /**
     * Returns current height, width and depth of the multiblock
     */
    int height();
    int width();
    int depth();

    /**
     * Returns max and min height, width and depth of the multiblock
     */
    int maxHeight();
    int minHeight();
    int maxWidth();
    int minWidth();
    int maxDepth();
    int minDepth();

    /**
     *
     * Returns list of blocks that are valid for the outer part of the multiblock
     */
    List<Block> validOuterBlocks();

    /**
     *
     * Returns list of blocks that are valid for the inner part of the multiblock
     */
    List<Block> validInnerBlocks();

    /**
     * Do not call this method directly, use validate() instead
     * Checks if the outer part of the multiblock is valid
     */
    void validateOuter();

    /**
     * Do not call this method directly, use validate() instead
     * Checks if the inner part of the multiblock is valid
     */
    void validateInner();

    /**
     * Checks if the multiblock is formed
     */
    void validate();

    MultiblockController controller();

    /**
     * Disposes the multiblock
     */
    void dispose();

    /**
     * Invalidates the stats of the multiblock, in case of updates
     */
    void clearStats();
}
