package igentuman.nc.phosphophyllite.multiblock2.modular;

import igentuman.nc.util.annotation.NonnullDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockBlock;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockTile;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;

@NonnullDefault
public interface ICoreMultiblockTileModule<
        TileType extends BlockEntity & IMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType>
        > {
    
    default boolean shouldConnectTo(TileType tile, Direction direction) {
        return true;
    }
    
    default void aboutToAttemptAttach() {
    }
    
    default void aboutToUnloadDetach() {
    }
    
    default void aboutToRemovedDetach() {
    }
    
    default void onControllerChange() {
    }
}
