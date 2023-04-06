package igentuman.nc.phosphophyllite.multiblock2.validated;

import igentuman.nc.NuclearCraft;
import igentuman.nc.phosphophyllite.util.Util;
import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.ValidationException;
import igentuman.nc.phosphophyllite.multiblock2.modular.IModularMultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModule;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModuleRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@NonnullDefault
@SuppressWarnings("RedundantThrows")
public interface IValidatedMultiblock<
        TileType extends BlockEntity & IValidatedMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IValidatedMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IValidatedMultiblock<TileType, BlockType, ControllerType>
        > extends IModularMultiblockController<TileType, BlockType, ControllerType> {
    
    enum AssemblyState {
        ASSEMBLED,
        DISASSEMBLED
    }
    
    default Module<TileType, BlockType, ControllerType> validatedModule() {
        //noinspection unchecked,ConstantConditions
        return module(IValidatedMultiblock.class, Module.class);
    }
    
    default AssemblyState assemblyState() {
        return validatedModule().assemblyState;
    }
    
    default void requestValidation() {
        validatedModule().requestValidation();
    }
    
    
    /**
     * the three validation stages are for ordering validation steps to ensure that the most expensive checks are only done after everything else has passed
     * put the cheapest checks in stage 1, most expensive in 3, anything in the middle in stage 2
     *
     * @throws ValidationException: validation failed if
     */
    default void validateStage1() throws ValidationException {
    }
    
    default void validateStage2() throws ValidationException {
    }
    
    default void validateStage3() throws ValidationException {
    }
    
    default void transitionToState(AssemblyState newAssemblyState) {
        validatedModule().transitionToState(newAssemblyState);
    }
    
    default void onStateTransition(AssemblyState oldAssemblyState, AssemblyState newAssemblyState) {
        switch (oldAssemblyState) {
            case ASSEMBLED -> {
                switch (newAssemblyState) {
                    case DISASSEMBLED -> onDisassembled();
                    case ASSEMBLED -> onReassembled();
                }
            }
            case DISASSEMBLED -> {
                switch (newAssemblyState) {
                    case ASSEMBLED -> onAssembled();
                    case DISASSEMBLED -> onRedisassembled();
                }
            }
        }
    }
    
    default void undefinedStateTransition(AssemblyState oldAssemblyState, AssemblyState newAssemblyState) {
    }
    
    default void onAssembled() {
    }
    
    default void onReassembled() {
    }
    
    default void onDisassembled() {
    }
    
    default void onRedisassembled() {
    }
    
    default void tick() {
    }
    
    default void disassembledTick() {
    }
    
    class Module<
            TileType extends BlockEntity & IValidatedMultiblockTile<TileType, BlockType, ControllerType>,
            BlockType extends Block & IValidatedMultiblockBlock,
            ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IValidatedMultiblock<TileType, BlockType, ControllerType>
            > extends MultiblockControllerModule<TileType, BlockType, ControllerType> {
        
        protected AssemblyState assemblyState = AssemblyState.DISASSEMBLED;
        private long updateAssemblyAtTick = Long.MAX_VALUE;
        
        @Nullable
        protected ValidationException lastValidationError = null;
        
        private final ObjectArrayList<IAssembledTickMultiblockModule> assembledTickMultiblockModules = new ObjectArrayList<>();
        private final ObjectArrayList<IValidatedMultiblockControllerModule> validatedMultiblockModules = new ObjectArrayList<>();
        
        @OnModLoad
        public static void register() {
            MultiblockControllerModuleRegistry.registerModule(IValidatedMultiblock.class, Module::new);
        }
        
        
        public Module(IValidatedMultiblock<TileType, BlockType, ControllerType> controller) {
            super(controller);
        }
        
        @Override
        public void postModuleConstruction() {
            for (final var value : modules()) {
                if (value instanceof IAssembledTickMultiblockModule module) {
                    assembledTickMultiblockModules.add(module);
                }
                if (value instanceof IValidatedMultiblockControllerModule module) {
                    validatedMultiblockModules.add(module);
                }
            }
        }
        
        @Override
        public void split(List<ControllerType> others) {
            updateAssemblyAtTick = Long.MIN_VALUE;
        }
        
        @Override
        public void merge(ControllerType other) {
            disassembledBlockStates();
            updateAssemblyAtTick = Long.MIN_VALUE;
        }
        
        @Override
        public void onPartAdded(@Nonnull TileType tile) {
            requestValidation();
        }
        
        @Override
        public void onPartRemoved(@Nonnull TileType tile) {
            requestValidation();
        }
        
        public void requestValidation() {
            updateAssemblyAtTick = NuclearCraft.tickNumber() + 1;
        }
        
        private void updateAssemblyState() {
            if (updateAssemblyAtTick > NuclearCraft.tickNumber()) {
                return;
            }
            updateAssemblyAtTick = Long.MAX_VALUE;
            for (final var tileTypeControllerTypeMultiblockControllerModule : validatedMultiblockModules) {
                if (!tileTypeControllerTypeMultiblockControllerModule.canValidate()) {
                    return;
                }
            }
            lastValidationError = null;
            try {
                for (final var tileTypeControllerTypeMultiblockControllerModule : validatedMultiblockModules) {
                    tileTypeControllerTypeMultiblockControllerModule.validateStage1();
                }
                controller.validateStage1();
                for (final var tileTypeControllerTypeMultiblockControllerModule : validatedMultiblockModules) {
                    tileTypeControllerTypeMultiblockControllerModule.validateStage2();
                }
                controller.validateStage2();
                for (final var tileTypeControllerTypeMultiblockControllerModule : validatedMultiblockModules) {
                    tileTypeControllerTypeMultiblockControllerModule.validateStage3();
                }
                controller.validateStage3();
            } catch (ValidationException validationError) {
                lastValidationError = validationError;
            }
            transitionToState(lastValidationError == null ? AssemblyState.ASSEMBLED : AssemblyState.DISASSEMBLED);
        }
        
        public final void transitionToState(AssemblyState newAssemblyState) {
            switch (newAssemblyState) {
                case ASSEMBLED -> assembledBlockStates();
                case DISASSEMBLED -> disassembledBlockStates();
            }
            final var oldAssemblyState = assemblyState;
            for (var module : validatedMultiblockModules) {
                module.onStateTransition(oldAssemblyState, newAssemblyState);
            }
            controller.onStateTransition(oldAssemblyState, newAssemblyState);
            assemblyState = newAssemblyState;
        }
        
        private final Long2ObjectLinkedOpenHashMap<BlockState> newStates = new Long2ObjectLinkedOpenHashMap<>();
        
        
        private void assembledBlockStates() {
            newStates.clear();
            
            final int size = controller.blocks.size();
            final TileType[] tileElements = controller.blocks.tileElements();
            final var posElements = controller.blocks.posElements();
            if (tileElements.length < size || posElements.length < size) {
                throw new IllegalStateException("Arrays too short");
            }
            for (int i = 0; i < size; i++) {
                final var entity = tileElements[i];
                // TODO: this is slow, *very* slow
                final var module = entity.validatedModule();
                final var pos = posElements[i];
                final BlockState oldState = entity.getBlockState();
                BlockState newState = module.assembledBlockState(oldState);
                if (newState != oldState) {
                    newStates.put(pos, newState);
                    entity.setBlockState(newState);
                }
            }
            if (!newStates.isEmpty()) {
                Util.setBlockStates(newStates, controller.level);
            }
        }
        
        private void disassembledBlockStates() {
            newStates.clear();
            final int size = controller.blocks.size();
            final TileType[] tileElements = controller.blocks.tileElements();
            final var posElements = controller.blocks.posElements();
            if (tileElements.length < size || posElements.length < size) {
                throw new IllegalStateException("Arrays too short");
            }
            for (int i = 0; i < size; i++) {
                final var entity = tileElements[i];
                // TODO: this is slow, *very* slow
                final var module = entity.validatedModule();
                final var pos = posElements[i];
                final BlockState oldState = entity.getBlockState();
                BlockState newState = module.disassembledBlockState(oldState);
                if (newState != oldState) {
                    newStates.put(pos, newState);
                    entity.setBlockState(newState);
                }
            }
            if (!newStates.isEmpty()) {
                Util.setBlockStates(newStates, controller.level);
            }
        }
        
        @Override
        public void update() {
            updateAssemblyState();
            
            for (final var tileTypeControllerTypeMultiblockControllerModule : validatedMultiblockModules) {
                if (!tileTypeControllerTypeMultiblockControllerModule.canTick()) {
                    return;
                }
            }
            
            if (assemblyState == AssemblyState.ASSEMBLED) {
                assembledTickMultiblockModules.forEach(IAssembledTickMultiblockModule::preTick);
                controller.tick();
                assembledTickMultiblockModules.forEach(IAssembledTickMultiblockModule::postTick);
            } else if (assemblyState == AssemblyState.DISASSEMBLED) {
                assembledTickMultiblockModules.forEach(IAssembledTickMultiblockModule::preDisassembledTick);
                controller.disassembledTick();
                assembledTickMultiblockModules.forEach(IAssembledTickMultiblockModule::postDisassembledTick);
            }
        }
    }
}
