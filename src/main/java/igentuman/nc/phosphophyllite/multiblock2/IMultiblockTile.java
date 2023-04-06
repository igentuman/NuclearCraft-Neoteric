package igentuman.nc.phosphophyllite.multiblock2;

import igentuman.nc.phosphophyllite.modular.tile.IIsTickingTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.modular.api.IModularTile;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IMultiblockTile<
        TileType extends BlockEntity & IMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType>
        > extends IModularTile, IIsTickingTracker.Tile {
    
    ControllerType createController();
    
    default ControllerType controller() {
        //noinspection ConstantConditions
        return multiblockModule().controller();
    }
    
    @Nullable
    default ControllerType nullableController() {
        return multiblockModule().controller();
    }
    
    default MultiblockTileModule<TileType, BlockType, ControllerType> multiblockModule() {
        //noinspection unchecked,ConstantConditions
        return (MultiblockTileModule<TileType, BlockType, ControllerType>) module(IMultiblockTile.class);
    }
    
    default MultiblockTileModule<TileType, BlockType, ControllerType> createMultiblockModule() {
        return new MultiblockTileModule<>(as(IModularTile.class));
    }
}
