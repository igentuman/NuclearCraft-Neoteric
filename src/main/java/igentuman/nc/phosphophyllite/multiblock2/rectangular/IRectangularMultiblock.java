package igentuman.nc.phosphophyllite.multiblock2.rectangular;

import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.phosphophyllite.util.Util;
import igentuman.nc.util.annotation.OnModLoad;
import igentuman.nc.util.joml.Vector3i;
import igentuman.nc.util.joml.Vector3ic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.ValidationException;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModule;
import igentuman.nc.phosphophyllite.multiblock2.modular.MultiblockControllerModuleRegistry;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblock;
import igentuman.nc.phosphophyllite.multiblock2.validated.IValidatedMultiblockControllerModule;

import javax.annotation.Nullable;

@NonnullDefault
public interface IRectangularMultiblock<
        TileType extends BlockEntity & IRectangularMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IRectangularMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IRectangularMultiblock<TileType, BlockType, ControllerType>
        > extends IValidatedMultiblock<TileType, BlockType, ControllerType> {
    
    @Nullable
    default Vector3ic minSize() {
        return null;
    }
    
    @Nullable
    default Vector3ic maxSize() {
        return null;
    }
    
    default boolean orientationAgnostic() {
        return true;
    }
    
    default boolean xzAgnostic() {
        return true;
    }
    
    default boolean cornerSpecificValidation() {
        return true;
    }
    
    default boolean frameSpecificValidation() {
        return true;
    }
    
    default void rectangularValidationStarted() {
    }
    
    default void rectangularBlockValidated(Block block) {
    }
    
    default boolean allowedInteriorBlock(Block block) {
        return false;
    }
    
    final class Module<
            TileType extends BlockEntity & IRectangularMultiblockTile<TileType, BlockType, ControllerType>,
            BlockType extends Block & IRectangularMultiblockBlock,
            ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & IRectangularMultiblock<TileType, BlockType, ControllerType>
            > extends MultiblockControllerModule<TileType, BlockType, ControllerType> implements IValidatedMultiblockControllerModule {
        
        private boolean cornerSpecificValidation;
        private boolean frameSpecificValidation;
        private int foundMultiblockBlocks;
        
        @OnModLoad
        public static void register() {
            MultiblockControllerModuleRegistry.registerModule(IRectangularMultiblock.class, Module::new);
        }
        
        public Module(IRectangularMultiblock<TileType, BlockType, ControllerType> controller) {
            super(controller);
        }
        
        @Override
        public void validateStage1() throws ValidationException {
            final var min = controller.min();
            final var max = controller.max();
            int minX = min.x();
            int minY = min.y();
            int minZ = min.z();
            int maxX = max.x();
            int maxY = max.y();
            int maxZ = max.z();
            
            final var allowedOrientations = new Vector3i[controller.orientationAgnostic() ? 6 : controller.xzAgnostic() ? 2 : 1];
            
            // TODO: garbage, less of it plz
            if (controller.orientationAgnostic()) {
                allowedOrientations[0] = new Vector3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
                allowedOrientations[1] = new Vector3i(maxX - minX + 1, maxZ - minZ + 1, maxY - minY + 1);
                
                allowedOrientations[2] = new Vector3i(maxY - minY + 1, maxX - minX + 1, maxZ - minZ + 1);
                allowedOrientations[3] = new Vector3i(maxY - minY + 1, maxZ - minZ + 1, maxX - minX + 1);
                
                allowedOrientations[4] = new Vector3i(maxZ - minZ + 1, maxX - minX + 1, maxY - minY + 1);
                allowedOrientations[5] = new Vector3i(maxZ - minZ + 1, maxY - minY + 1, maxX - minX + 1);
            } else if (controller.xzAgnostic()) {
                // TODO: these explode
                allowedOrientations[0] = new Vector3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
                allowedOrientations[1] = new Vector3i(maxZ - minZ + 1, maxY - minY + 1, maxX - minX + 1);
            } else {
                allowedOrientations[0] = new Vector3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
            }
            
            final var minSize = controller.minSize();
            final var maxSize = controller.maxSize();
            
            Vector3i dimensions = null;
            for (Vector3i allowedOrientation : allowedOrientations) {
                if (minSize != null) {
                    if (
                            allowedOrientation.x < minSize.x() ||
                                    allowedOrientation.y < minSize.y() ||
                                    allowedOrientation.z < minSize.z()
                    ) {
                        continue;
                    }
                }
                if (maxSize != null) {
                    if (
                            allowedOrientation.x > maxSize.x() ||
                                    allowedOrientation.y > maxSize.y() ||
                                    allowedOrientation.z > maxSize.z()
                    ) {
                        continue;
                    }
                }
                dimensions = allowedOrientation;
                break;
            }
            // dimension check failed in all orientations
            if (dimensions == null) {
                final var minSizenn = minSize != null ? minSize : new Vector3i();
                final var maxSizenn = maxSize != null ? maxSize : new Vector3i();
                throw new ValidationException(Component.translatable("multiblock.error.phosphophyllite.dimensions",
                        allowedOrientations[0].x, allowedOrientations[0].y, allowedOrientations[0].z,
                        minSizenn.x(), minSizenn.y(), minSizenn.z(),
                        maxSizenn.x(), maxSizenn.y(), maxSizenn.z()));
            }
        }
        
        @Override
        public void validateStage2() throws ValidationException {
            controller.rectangularValidationStarted();
            cornerSpecificValidation = controller.cornerSpecificValidation();
            frameSpecificValidation = controller.frameSpecificValidation();
            foundMultiblockBlocks = 0;
            Util.chunkCachedBlockStateIteration(controller.min(), controller.max(), controller.level, this::blockValidation);
            if (foundMultiblockBlocks != controller.blocks.size()) {
                throw new ValidationException(Component.translatable("multiblock.error.phosphophyllite.mismatched_block_count", foundMultiblockBlocks, controller.blocks.size()));
            }
        }
        
        private void blockValidation(BlockState blockState, Vector3ic pos) throws ValidationException {
            final var min = controller.min();
            final var max = controller.max();
            int minX = min.x();
            int minY = min.y();
            int minZ = min.z();
            int maxX = max.x();
            int maxY = max.y();
            int maxZ = max.z();
            
            Block block = blockState.getBlock();
            int extremes = 0;
            if (pos.x() == minX || pos.x() == maxX) {
                extremes++;
            }
            if (pos.y() == minY || pos.y() == maxY) {
                extremes++;
            }
            if (pos.z() == minZ || pos.z() == maxZ) {
                extremes++;
            }
            // use of old switch for case to case rolling is intentional
            switch (extremes) {
                case 3: {
                    if (cornerSpecificValidation) {
                        if (block instanceof IRectangularMultiblockBlock && controller.blockTypeValidator.test(block)) {
                            if (((IRectangularMultiblockBlock) block).isGoodForCorner()) {
                                foundMultiblockBlocks++;
                                break;
                            }
                        }
                        throw new InvalidBlock(block, pos, "corner");
                    }
                }
                case 2: {
                    if (frameSpecificValidation) {
                        if (block instanceof IRectangularMultiblockBlock && controller.blockTypeValidator.test(block)) {
                            if (((IRectangularMultiblockBlock) block).isGoodForFrame()) {
                                foundMultiblockBlocks++;
                                break;
                            }
                        }
                        throw new InvalidBlock(block, pos, "frame");
                    }
                }
                case 1: {
                    if (block instanceof IRectangularMultiblockBlock && controller.blockTypeValidator.test(block)) {
                        if (((IRectangularMultiblockBlock) block).isGoodForExterior()) {
                            foundMultiblockBlocks++;
                            break;
                        }
                    }
                    throw new InvalidBlock(block, pos, "exterior");
                }
                default: {
                    if (block instanceof IRectangularMultiblockBlock && controller.blockTypeValidator.test(block)) {
                        if (((IRectangularMultiblockBlock) block).isGoodForInterior()) {
                            foundMultiblockBlocks++;
                            break;
                        }
                        throw new InvalidBlock(block, pos, "interior");
                    }
                    
                    if (!controller.allowedInteriorBlock(block)) {
                        throw new InvalidBlock(block, pos, "interior");
                    }
                }
            }
            controller.rectangularBlockValidated(block);
        }
        
    }
}
