package igentuman.nc.phosphophyllite.multiblock2.rectangular;

import igentuman.nc.phosphophyllite.util.BlockStates;
import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.modular.ICoreMultiblockTileModule;
import igentuman.nc.phosphophyllite.multiblock2.validated.IAssemblyStateTileModule;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblockTile;

import static igentuman.nc.phosphophyllite.multiblock2.rectangular.AxisPosition.*;

@NonnullDefault
public interface IRectangularMultiblockTile<
        TileType extends BlockEntity & IRectangularMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IRectangularMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IRectangularMultiblock<TileType, BlockType, ControllerType>
        > extends IValidatedMultiblockTile<TileType, BlockType, ControllerType> {
    
    final class Module<
            TileType extends BlockEntity & IRectangularMultiblockTile<TileType, BlockType, ControllerType>,
            BlockType extends Block & IRectangularMultiblockBlock,
            ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IRectangularMultiblock<TileType, BlockType, ControllerType>
            > extends TileModule<TileType> implements ICoreMultiblockTileModule<TileType, BlockType, ControllerType>, IAssemblyStateTileModule {
        
        private final boolean AXIS_POSITIONS = iface.getBlockState().hasProperty(X_AXIS_POSITION);
        private final boolean FACE_DIRECTION = iface.getBlockState().hasProperty(BlockStates.FACING);
        
        @OnModLoad
        public static void register() {
            ModuleRegistry.registerTileModule(IRectangularMultiblockTile.class, Module::new);
        }
        
        public Module(IRectangularMultiblockTile<TileType, BlockType, ControllerType> iface) {
            super(iface);
        }
        
        @Override
        public BlockState assembledBlockState(BlockState state) {
            if (AXIS_POSITIONS) {
                BlockPos pos = iface.getBlockPos();
                
                if (pos.getX() == iface.controller().min().x()) {
                    state = state.setValue(X_AXIS_POSITION, AxisPosition.LOWER);
                } else if (pos.getX() == iface.controller().max().x()) {
                    state = state.setValue(X_AXIS_POSITION, AxisPosition.UPPER);
                } else {
                    state = state.setValue(X_AXIS_POSITION, AxisPosition.MIDDLE);
                }
                
                if (pos.getY() == iface.controller().min().y()) {
                    state = state.setValue(Y_AXIS_POSITION, AxisPosition.LOWER);
                } else if (pos.getY() == iface.controller().max().y()) {
                    state = state.setValue(Y_AXIS_POSITION, AxisPosition.UPPER);
                } else {
                    state = state.setValue(Y_AXIS_POSITION, AxisPosition.MIDDLE);
                }
                
                if (pos.getZ() == iface.controller().min().z()) {
                    state = state.setValue(Z_AXIS_POSITION, AxisPosition.LOWER);
                } else if (pos.getZ() == iface.controller().max().z()) {
                    state = state.setValue(Z_AXIS_POSITION, AxisPosition.UPPER);
                } else {
                    state = state.setValue(Z_AXIS_POSITION, AxisPosition.MIDDLE);
                }
            }
            if (FACE_DIRECTION) {
                BlockPos pos = iface.getBlockPos();
                if (pos.getX() == iface.controller().min().x()) {
                    state = state.setValue(BlockStates.FACING, Direction.WEST);
                } else if (pos.getX() == iface.controller().max().x()) {
                    state = state.setValue(BlockStates.FACING, Direction.EAST);
                } else if (pos.getY() == iface.controller().min().y()) {
                    state = state.setValue(BlockStates.FACING, Direction.DOWN);
                } else if (pos.getY() == iface.controller().max().y()) {
                    state = state.setValue(BlockStates.FACING, Direction.UP);
                } else if (pos.getZ() == iface.controller().min().z()) {
                    state = state.setValue(BlockStates.FACING, Direction.NORTH);
                } else if (pos.getZ() == iface.controller().max().z()) {
                    state = state.setValue(BlockStates.FACING, Direction.SOUTH);
                }
            }
            return state;
        }
        
        @Override
        public BlockState disassembledBlockState(BlockState state) {
            return state;
        }
    }
}
