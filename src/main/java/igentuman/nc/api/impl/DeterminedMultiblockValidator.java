package igentuman.nc.api.impl;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.util.MultiblockStructure;
import igentuman.nc.util.MultiblocksProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

import static igentuman.nc.multiblock.MultiblockDebug.bounds;
import static igentuman.nc.multiblock.MultiblockDebug.fail;
import static igentuman.nc.multiblock.MultiblockDebug.step;

/** Validates a multiblock against a saved NBT structure, matched at any horizontal orientation. */
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
        step("loading fixed structure template {}", structureName);
        if (!ensureLoaded()) {
            fail("multiblock.validation.missing_template", controllerPos,
                    NuclearCraft.rl(structureName), null);
            cache.getStructurePositions().clear();
            return false;
        }
        Rotation rotation = rotationFor(facing);
        cache.getStructurePositions().clear();
        resolveBounds(controllerPos, rotation);
        step("validating {} template cells rotation={}", structure.getBlocks().size(), rotation);

        for (Map.Entry<BlockPos, BlockState> e : structure.getBlocks().entrySet()) {
            BlockPos local = e.getKey().subtract(controllerLocal);
            BlockPos rotated = local.rotate(rotation);
            BlockPos worldPos = controllerPos.offset(rotated);

            BlockState expected = e.getValue().rotate(rotation);
            BlockState actual = cache.getBlockState(level, worldPos);

            boolean isController = e.getKey().equals(controllerLocal);
            if (isController) {
                if (!controllerPredicate.test(actual, null)) {
                    fail("multiblock.validation.wrong_controller", worldPos,
                            blockId(expected), blockId(actual));
                    cache.getStructurePositions().clear();
                    return false;
                }
            } else if (controllerPredicate.test(actual, null)) {
                fail("multiblock.validation.extra_controller", worldPos,
                        blockId(expected), blockId(actual));
                cache.getStructurePositions().clear();
                return false;
            } else if (expected.isAir()) {
                if (!actual.isAir()) {
                    fail("multiblock.validation.expected_air", worldPos,
                            blockId(expected), blockId(actual));
                    cache.getStructurePositions().clear();
                    return false;
                }
            } else if (!igentuman.nc.multiblock.StructuralBlockState.equivalent(actual, expected)) {
                fail("multiblock.validation.wrong_block", worldPos,
                        blockId(expected), blockId(actual));
                cache.getStructurePositions().clear();
                return false;
            }

            cache.getStructurePositions().add(worldPos.asLong());
            cache.getBlockEntity(level, worldPos);
        }
        step("fixed structure template passed");
        return true;
    }

    private void resolveBounds(BlockPos controllerPos, Rotation rotation) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos templatePos : structure.getBlocks().keySet()) {
            BlockPos worldPos = controllerPos.offset(templatePos.subtract(controllerLocal).rotate(rotation));
            minX = Math.min(minX, worldPos.getX());
            minY = Math.min(minY, worldPos.getY());
            minZ = Math.min(minZ, worldPos.getZ());
            maxX = Math.max(maxX, worldPos.getX());
            maxY = Math.max(maxY, worldPos.getY());
            maxZ = Math.max(maxZ, worldPos.getZ());
        }
        bounds(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    private static net.minecraft.resources.ResourceLocation blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
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
