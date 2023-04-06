package igentuman.nc.phosphophyllite.multiblock2.common;

import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockTile;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.modular.IModularMultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModule;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModuleRegistry;
import igentuman.nc.phosphophyllite.multiblock2.validated.IAssembledTickMultiblockModule;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblock;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblockBlock;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblockTile;
import igentuman.nc.phosphophyllite.util.FastArraySet;

import javax.annotation.Nonnull;

@NonnullDefault
public interface ITickablePartsMultiblock<
        TileType extends BlockEntity & IMultiblockTile<TileType, BlockType, ControllerType> & IValidatedMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IValidatedMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & ITickablePartsMultiblock<TileType, BlockType, ControllerType> & IValidatedMultiblock<TileType, BlockType, ControllerType>
        > extends IModularMultiblockController<TileType, BlockType, ControllerType> {
    
    
    /**
     * any Tile implementing this will be ticked
     */
    interface Tickable {
        void preTick();
        
        void postTick();
    }
    
    final class Module<
            TileType extends BlockEntity & IMultiblockTile<TileType, BlockType, ControllerType> & IValidatedMultiblockTile<TileType, BlockType, ControllerType>,
            BlockType extends Block & IValidatedMultiblockBlock,
            ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & ITickablePartsMultiblock<TileType, BlockType, ControllerType> & IValidatedMultiblock<TileType, BlockType, ControllerType>
            > extends MultiblockControllerModule<TileType, BlockType, ControllerType> implements IAssembledTickMultiblockModule {
        
        private final FastArraySet<Tickable> tickables = new FastArraySet<>();
        
        @OnModLoad
        public static void register() {
            MultiblockControllerModuleRegistry.registerModule(ITickablePartsMultiblock.class, Module::new);
        }
        
        public Module(IModularMultiblockController<TileType, BlockType, ControllerType> controller) {
            super(controller);
        }
        
        @Override
        public void onPartAdded(@Nonnull TileType tile) {
            if (tile instanceof Tickable tickable) {
                tickables.add(tickable);
            }
        }
        
        @Override
        public void onPartRemoved(@Nonnull TileType tile) {
            if (tile instanceof Tickable tickable) {
                tickables.remove(tickable);
            }
        }
        
        @Override
        public void preTick() {
            tickables.elements().forEach(Tickable::preTick);
        }
        
        @Override
        public void postTick() {
            tickables.elements().forEach(Tickable::postTick);
        }
    }
}
