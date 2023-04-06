package igentuman.nc.phosphophyllite.multiblock2.modular;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockBlock;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockTile;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.util.annotation.NonnullDefault;

import javax.annotation.Nonnull;
import java.util.List;

@NonnullDefault
public abstract class MultiblockControllerModule<
        TileType extends BlockEntity & IMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IModularMultiblockController<TileType, BlockType, ControllerType>
        > {
    
    public final ControllerType controller;
    
    public MultiblockControllerModule(ControllerType controller) {
        this.controller = controller;
    }
    
    public MultiblockControllerModule(IModularMultiblockController<TileType, BlockType, ControllerType> controller) {
        //noinspection unchecked
        this((ControllerType) controller);
    }
    
    public void postModuleConstruction() {
    }
    
    public List<MultiblockControllerModule<TileType, BlockType, ControllerType>> modules() {
        return controller.modules();
    }
    
    
    public boolean canAttachPart(TileType tile) {
        return true;
    }
    
    public void onPartAdded(@Nonnull TileType tile) {
    }
    
    public void onPartRemoved(@Nonnull TileType tile) {
    }
    
    public void onPartLoaded(TileType tile) {
    }
    
    public void onPartUnloaded(TileType tile) {
    }
    
    public void onPartAttached(TileType tile) {
    }
    
    public void onPartDetached(TileType tile) {
    }
    
    public void onPartPlaced(TileType tile) {
    }
    
    public void onPartBroken(TileType tile) {
    }
    
    public void merge(ControllerType other) {
    }
    
    public void split(List<ControllerType> others) {
    }
    
    public void update() {
    }

}
