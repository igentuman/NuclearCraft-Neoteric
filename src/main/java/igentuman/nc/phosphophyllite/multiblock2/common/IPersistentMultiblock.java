package igentuman.nc.phosphophyllite.multiblock2.common;

import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModule;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModuleRegistry;
import igentuman.nc.phosphophyllite.multiblock2.rectangular.IRectangularMultiblockBlock;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblock;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblockControllerModule;
import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.phosphophyllite.util.Util;

import javax.annotation.Nullable;

@NonnullDefault
public interface IPersistentMultiblock<
        TileType extends BlockEntity & IPersistentMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IRectangularMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IPersistentMultiblock<TileType, BlockType, ControllerType>
        > extends IValidatedMultiblock<TileType, BlockType, ControllerType> {
    
    CompoundTag mergeNBTs(CompoundTag nbtA, CompoundTag nbtB);
    
    void read(CompoundTag nbt);
    
    @Nullable
    CompoundTag write();
    
    default Module<TileType, BlockType, ControllerType> persistentModule() {
        //noinspection unchecked,ConstantConditions
        return module(IPersistentMultiblock.class, Module.class);
    }
    
    default void dirty() {
        final var module = module(IPersistentMultiblock.class, Module.class);
        assert module != null;
        module.dirty();
    }
    
    final class Module<
            TileType extends BlockEntity & IPersistentMultiblockTile<TileType, BlockType, ControllerType>,
            BlockType extends Block & IRectangularMultiblockBlock,
            ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IPersistentMultiblock<TileType, BlockType, ControllerType>
            > extends MultiblockControllerModule<TileType, BlockType, ControllerType> implements IValidatedMultiblockControllerModule {
        @Nullable
        private TileType saveDelegate;
        private boolean hasSaveDelegate = false;
        @Nullable
        private IPersistentMultiblockTile.Module<TileType, BlockType, ControllerType> saveDelegateModule;
        @Nullable
        private CompoundTag nbt;
        private boolean shouldReadNBT = false;
        private int expectedBlocks = 0;
        @Nullable
        private IValidatedMultiblock.AssemblyState lastAssemblyState;
        
        @OnModLoad
        public static void register() {
            MultiblockControllerModuleRegistry.registerModule(IPersistentMultiblock.class, Module::new);
        }
        
        public Module(IPersistentMultiblock<TileType, BlockType, ControllerType> controller) {
            super(controller);
        }
        
        private void partAdded(TileType newPart) {
            final var persistentModule = newPart.module(IPersistentMultiblockTile.class, IPersistentMultiblockTile.Module.class);
            assert persistentModule != null;
            if (expectedBlocks == 0) {
                expectedBlocks = persistentModule.expectedBlocks;
            }
            if (persistentModule.expectedBlocks != 0 && persistentModule.expectedBlocks != expectedBlocks) {
                // merging controllers, reset to zero
                expectedBlocks = 0;
            }
            if (lastAssemblyState == null) {
                lastAssemblyState = persistentModule.lastAssemblyState;
            }
            
            final var newNBT = persistentModule.nbt;
            persistentModule.nbt = null;
            if (newNBT == null) {
                return;
            }
            if (saveDelegate == null) {
                saveDelegate = newPart;
                //noinspection unchecked
                saveDelegateModule = persistentModule;
            }
            if (nbt != null) {
                if (nbt.equals(newNBT)) {
                    return;
                }
                nbt = controller.mergeNBTs(nbt, newNBT);
            } else {
                nbt = newNBT;
            }
            shouldReadNBT = true;
        }
        
        private void partRemoved(TileType oldPart) {
            if (oldPart == saveDelegate) {
                saveDelegate = null;
                saveDelegateModule = null;
            }
        }
        
        public void onPartLoaded(TileType tile) {
            partAdded(tile);
        }
        
        public void onPartUnloaded(TileType tile) {
            partRemoved(tile);
        }
        
        public void onPartAttached(TileType tile) {
            partAdded(tile);
        }
        
        public void onPartDetached(TileType tile) {
            partRemoved(tile);
        }
        
        public void onPartPlaced(TileType tile) {
            // cannot have nbt when first placed
            expectedBlocks = 0;
        }
        
        public void onPartBroken(TileType tile) {
            expectedBlocks = 0;
            partRemoved(tile);
            if (tile == saveDelegate) {
                hasSaveDelegate = false;
            }
        }
        
        private void pickDelegate() {
            if (hasSaveDelegate) {
                return;
            }
            saveDelegate = controller.randomTile();
            //noinspection unchecked
            saveDelegateModule = saveDelegate.module(IPersistentMultiblockTile.class, IPersistentMultiblockTile.Module.class);
            if (saveDelegateModule != null) {
                saveDelegateModule.nbt = null;
            }
            hasSaveDelegate = true;
        }
        
        @Override
        public boolean canValidate() {
            return expectedBlocks <= controller.blocks.size();
        }
    
        @Override
        public boolean canTick() {
            return canValidate();
        }
        
        @Override
        public void onStateTransition(AssemblyState oldAssemblyState, AssemblyState newAssemblyState) {
            if (shouldReadNBT) {
                shouldReadNBT = false;
                if (nbt != null) {
                    controller.read(nbt);
                }
            }
        }
        
        void dirty() {
            pickDelegate();
            nbt = null;
            if (saveDelegateModule != null) {
                saveDelegateModule.nbt = null;
            }
            Util.markRangeDirty(controller.level, controller.min(), controller.max());
        }
        
        boolean isSaveDelegate(TileType tile) {
            pickDelegate();
            return tile == saveDelegate;
        }
        
        @Nullable
        CompoundTag getNBT() {
            if (nbt == null) {
                nbt = controller.write();
            }
            return nbt;
        }

    }
}
