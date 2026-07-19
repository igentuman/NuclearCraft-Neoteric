package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KugelblitzValidator implements IMultiblockValidator {

    private static final int[][] AXES = {
            {0, 5, 0}, {0, -5, 0}, {-5, 0, 0}, {5, 0, 0}, {0, 0, -5}, {0, 0, 5}
    };
    private static final int[][] RINGS = {
            {0, 4, 0}, {0, -4, 0}, {-4, 0, 0}, {4, 0, 0}, {0, 0, -4}, {0, 0, 4}
    };

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!(cache instanceof KugelblitzCache kc)) return false;
        kc.resetStats();
        Set<Long> positions = kc.getStructurePositions();
        positions.clear();

        BlockPos center = findCenter(level, controllerPos);
        if (center == null) return false;

        if (!validateWalls(level, center, kc, positions)) return false;
        if (!validateRings(level, center, positions)) return false;
        if (!validateCorners(level, center, positions)) return false;
        if (!validateInterior(level, center, positions)) return false;

        positions.add(controllerPos.asLong());
        kc.center = center.asLong();
        return true;
    }

    private BlockPos findCenter(Level level, BlockPos controllerPos) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -6; dz <= 6; dz++) {
                    BlockPos c = controllerPos.offset(dx, dy, dz);
                    if (hasAllConcentrators(level, c)) return c;
                }
            }
        }
        return null;
    }

    private boolean hasAllConcentrators(Level level, BlockPos center) {
        Block photon = blockOf("photon_concentrator");
        for (int[] a : AXES) {
            if (!level.getBlockState(center.offset(a[0], a[1], a[2])).is(photon)) return false;
        }
        return true;
    }

    private boolean validateWalls(Level level, BlockPos center, KugelblitzCache kc, Set<Long> positions) {
        Block photon = blockOf("photon_concentrator");
        Block transformer = blockOf("quantum_transformer");
        Block flux = blockOf("quantum_flux_regulator");
        Block stabilizer = blockOf("event_horizon_stabilizer");

        List<Block> reference = null;
        for (int[] a : AXES) {
            BlockPos faceCenter = center.offset(a[0], a[1], a[2]);
            if (!level.getBlockState(faceCenter).is(photon)) return false;
            int[][] basis = basis(a);
            List<Block> wall = new ArrayList<>(25);
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    BlockPos p = faceCenter.offset(basis[0][0] * i + basis[1][0] * j,
                            basis[0][1] * i + basis[1][1] * j,
                            basis[0][2] * i + basis[1][2] * j);
                    BlockState state = level.getBlockState(p);
                    if (!isCasing(state)) return false;
                    if (state.is(transformer)) kc.transformers++;
                    if (state.is(flux)) kc.fluxRegulators++;
                    if (state.is(stabilizer)) kc.stabilizers++;
                    wall.add(state.getBlock());
                    positions.add(p.asLong());
                }
            }
            if (reference == null) {
                reference = wall;
            } else if (!reference.equals(wall)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateRings(Level level, BlockPos center, Set<Long> positions) {
        for (int[] r : RINGS) {
            BlockPos ringCenter = center.offset(r[0], r[1], r[2]);
            int[][] basis = basis(r);
            for (int i = -3; i <= 3; i++) {
                for (int j = -3; j <= 3; j++) {
                    if (Math.abs(i) != 3 && Math.abs(j) != 3) continue;
                    if (Math.abs(i) == 3 && Math.abs(j) == 3) continue;
                    BlockPos p = ringCenter.offset(basis[0][0] * i + basis[1][0] * j,
                            basis[0][1] * i + basis[1][1] * j,
                            basis[0][2] * i + basis[1][2] * j);
                    if (!isFrame(level.getBlockState(p))) return false;
                    positions.add(p.asLong());
                }
            }
        }
        return true;
    }

    private boolean validateCorners(Level level, BlockPos center, Set<Long> positions) {
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    BlockPos p = center.offset(sx * 3, sy * 3, sz * 3);
                    if (!isCasing(level.getBlockState(p))) return false;
                    positions.add(p.asLong());
                }
            }
        }
        return true;
    }

    private boolean validateInterior(Level level, BlockPos center, Set<Long> positions) {
        Block blackHole = blockOf("black_hole");
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    if (x * x + y * y + z * z > 16) continue;
                    BlockPos p = center.offset(x, y, z);
                    BlockState state = level.getBlockState(p);
                    if (!state.isAir() && !state.is(blackHole)) return false;
                    positions.add(p.asLong());
                }
            }
        }
        return true;
    }

    private int[][] basis(int[] normal) {
        if (normal[1] != 0) return new int[][]{{1, 0, 0}, {0, 0, 1}};
        if (normal[0] != 0) return new int[][]{{0, 1, 0}, {0, 0, 1}};
        return new int[][]{{1, 0, 0}, {0, 1, 0}};
    }

    private boolean isCasing(BlockState state) {
        return state.is(KugelblitzTags.CASING);
    }

    private boolean isFrame(BlockState state) {
        return state.is(blockOf("neutronium_frame"))
                || state.is(blockOf("chamber_terminal"))
                || state.is(blockOf("chamber_port"));
    }

    private static Block blockOf(String name) {
        return ModEntries.get(name).block().get();
    }
}
