package igentuman.nc.api.impl;

import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.util.MultiblockStructure;
import igentuman.nc.util.MultiblocksProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Validator that loads a saved structure from {@code data/<modid>/structures/<name>.nbt}
 * and verifies the world matches it. Supports all 4 horizontal orientations via the
 * controller's {@code facing} direction (structure is assumed authored facing NORTH).
 *
 * <p>Match rule: world state must equal expected state exactly (after rotation), except
 * at the controller cell which is checked via {@code controllerPredicate}.
 */
public class DeterminedMultiblockValidator implements IMultiblockValidator {

    private final String structureName;
    private final BlockPredicate controllerPredicate;
    private MultiblockStructure structure;
    private BlockPos controllerLocal;

    public DeterminedMultiblockValidator(String structureName, BlockPredicate controllerPredicate) {
        this.structureName = structureName;
        this.controllerPredicate = controllerPredicate;
    }

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!ensureLoaded()) {
            cache.getStructurePositions().clear();
            return false;
        }
        Rotation rotation = rotationFor(facing);
        cache.getStructurePositions().clear();

        for (Map.Entry<BlockPos, BlockState> e : structure.getBlocks().entrySet()) {
            BlockPos local = e.getKey().subtract(controllerLocal);
            BlockPos rotated = local.rotate(rotation);
            BlockPos worldPos = controllerPos.offset(rotated);

            BlockState expected = e.getValue().rotate(rotation);
            BlockState actual = cache.getBlockState(level, worldPos);

            boolean isController = e.getKey().equals(controllerLocal);
            if (isController) {
                if (!controllerPredicate.test(actual, null)) {
                    cache.getStructurePositions().clear();
                    return false;
                }
            } else if (expected.isAir()) {
                if (!actual.isAir()) {
                    cache.getStructurePositions().clear();
                    return false;
                }
            } else if (!actual.equals(expected)) {
                cache.getStructurePositions().clear();
                return false;
            }

            cache.getStructurePositions().add(worldPos.asLong());
            cache.getBlockEntity(level, worldPos);
        }
        return true;
    }

    private boolean ensureLoaded() {
        if (structure != null && controllerLocal != null) return true;
        MultiblockStructure loaded = findLoadedStructure();
        if (loaded == null) loaded = MultiblocksProvider.loadStructureFromClasspath(structureName);
        if (loaded == null) return false;
        BlockPos local = findControllerLocal(loaded);
        if (local == null) return false;
        this.structure = loaded;
        this.controllerLocal = local;
        return true;
    }

    private MultiblockStructure findLoadedStructure() {
        String suffix = "/" + structureName + ".nbt";
        for (MultiblockStructure s : MultiblocksProvider.getStructures()) {
            if (s.getId() != null && s.getId().getPath().endsWith(suffix)) return s;
        }
        return null;
    }

    private BlockPos findControllerLocal(MultiblockStructure s) {
        for (Map.Entry<BlockPos, BlockState> e : s.getBlocks().entrySet()) {
            if (controllerPredicate.test(e.getValue(), null)) return e.getKey();
        }
        return null;
    }

    private static Rotation rotationFor(Direction facing) {
        return switch (facing) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
