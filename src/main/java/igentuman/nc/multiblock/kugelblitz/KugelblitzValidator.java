package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public class KugelblitzValidator extends AbstractMultiblockValidator<KugelblitzCache> {

    private static final int[][] AXES = {
            {0, 5, 0}, {0, -5, 0}, {-5, 0, 0}, {5, 0, 0}, {0, 0, -5}, {0, 0, 5}
    };
    private static final int[][] RINGS = {
            {0, 4, 0}, {0, -4, 0}, {-4, 0, 0}, {4, 0, 0}, {0, 0, -4}, {0, 0, 4}
    };

    private Block controller;
    private Block port;
    private Block frame;
    private Block photon;
    private Block transformer;
    private Block flux;
    private Block stabilizer;
    private Block blackHole;
    private boolean resolved;

    private BlockPos center;

    private boolean resolveBlocks() {
        if (resolved) return true;
        controller = blockOf("chamber_terminal");
        port = blockOf("chamber_port");
        frame = blockOf("neutronium_frame");
        photon = blockOf("photon_concentrator");
        transformer = blockOf("quantum_transformer");
        flux = blockOf("quantum_flux_regulator");
        stabilizer = blockOf("event_horizon_stabilizer");
        blackHole = blockOf("black_hole");
        resolved = controller != null && port != null && frame != null && photon != null
                && transformer != null && flux != null && stabilizer != null && blackHole != null;
        return resolved;
    }

    @Override
    protected boolean findBounds(KugelblitzCache cache) {
        BlockPos origin = cache.controllerPos();
        center = null;
        if (!resolveBlocks()) {
            return fail("multiblock.kugelblitz.center_not_found", origin, rl("photon_concentrator"),
                    (ResourceLocation) null);
        }
        center = findCenter(cache, origin);
        if (center == null) {
            return fail("multiblock.kugelblitz.center_not_found", origin, rl("photon_concentrator"),
                    cache.getBlockState(origin));
        }
        cache.setWorkingCenter(center);
        cache.setWorkingBounds(center.offset(-5, -5, -5), center.offset(5, 5, 5));
        return true;
    }

    private BlockPos findCenter(KugelblitzCache cache, BlockPos origin) {
        for (int[] r : RINGS) {
            int[][] basis = basis(r);
            for (int i = -3; i <= 3; i++) {
                for (int j = -3; j <= 3; j++) {
                    if (!isRingOffset(i, j)) continue;
                    BlockPos candidate = origin.offset(
                            -(r[0] + basis[0][0] * i + basis[1][0] * j),
                            -(r[1] + basis[0][1] * i + basis[1][1] * j),
                            -(r[2] + basis[0][2] * i + basis[1][2] * j));
                    if (hasAllConcentrators(cache, candidate)) return candidate;
                }
            }
        }
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    BlockPos candidate = origin.offset(-sx * 3, -sy * 3, -sz * 3);
                    if (hasAllConcentrators(cache, candidate)) return candidate;
                }
            }
        }
        return null;
    }

    private boolean hasAllConcentrators(KugelblitzCache cache, BlockPos candidate) {
        for (int[] a : AXES) {
            if (!cache.getBlockState(candidate.offset(a[0], a[1], a[2])).is(photon)) return false;
        }
        return true;
    }

    @Override
    protected boolean validateShell(KugelblitzCache cache) {
        step("kugelblitz center={} validating symmetric walls", center.toShortString());
        if (!validateWalls(cache)) return false;
        step("kugelblitz walls passed; validating frame rings");
        if (!validateRings(cache)) return false;
        step("kugelblitz rings passed; validating corners");
        return validateCorners(cache);
    }

    private boolean validateWalls(KugelblitzCache cache) {
        List<Block> reference = null;
        for (int[] a : AXES) {
            BlockPos faceCenter = center.offset(a[0], a[1], a[2]);
            BlockState faceState = cache.getBlockState(faceCenter);
            if (!faceState.is(photon)) {
                return fail("multiblock.kugelblitz.missing_photon_concentrator", faceCenter,
                        rl("photon_concentrator"), faceState);
            }
            int[][] basis = basis(a);
            List<Block> wall = new ArrayList<>(25);
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    BlockPos p = offset(faceCenter, basis, i, j);
                    BlockState state = cache.getBlockState(p);
                    if (!isCasing(state)) {
                        return fail("multiblock.kugelblitz.wrong_wall", p, rl("kugelblitz_casing"), state);
                    }
                    if (!acceptShellBlock(cache, p, state)) return false;
                    if (state.is(transformer)) cache.countTransformer();
                    if (state.is(flux)) cache.countFluxRegulator();
                    if (state.is(stabilizer)) cache.countStabilizer();
                    wall.add(state.getBlock());
                }
            }
            if (reference == null) {
                reference = wall;
            } else if (!reference.equals(wall)) {
                return fail("multiblock.kugelblitz.asymmetric_walls", faceCenter,
                        rl("symmetric_kugelblitz_wall"), faceState);
            }
        }
        return true;
    }

    private boolean validateRings(KugelblitzCache cache) {
        for (int[] r : RINGS) {
            BlockPos ringCenter = center.offset(r[0], r[1], r[2]);
            int[][] basis = basis(r);
            for (int i = -3; i <= 3; i++) {
                for (int j = -3; j <= 3; j++) {
                    if (!isRingOffset(i, j)) continue;
                    BlockPos p = offset(ringCenter, basis, i, j);
                    BlockState state = cache.getBlockState(p);
                    if (!isFrame(state)) {
                        return fail("multiblock.kugelblitz.wrong_frame", p, rl("neutronium_frame"), state);
                    }
                    if (!acceptShellBlock(cache, p, state)) return false;
                }
            }
        }
        return true;
    }

    private boolean validateCorners(KugelblitzCache cache) {
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    BlockPos p = center.offset(sx * 3, sy * 3, sz * 3);
                    BlockState state = cache.getBlockState(p);
                    if (!isCasing(state)) {
                        return fail("multiblock.kugelblitz.wrong_corner", p, rl("kugelblitz_casing"), state);
                    }
                    if (!acceptShellBlock(cache, p, state)) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean validateInterior(KugelblitzCache cache) {
        step("kugelblitz corners passed; validating interior");
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    if (x * x + y * y + z * z > 16) continue;
                    BlockPos p = center.offset(x, y, z);
                    BlockState state = cache.getBlockState(p);
                    if (!state.isAir() && !state.is(blackHole)) {
                        return fail("multiblock.kugelblitz.wrong_inner", p,
                                BuiltInRegistries.BLOCK.getKey(Blocks.AIR), state);
                    }
                }
            }
        }
        return true;
    }

    private boolean acceptShellBlock(KugelblitzCache cache, BlockPos pos, BlockState state) {
        if (state.is(controller) && !pos.equals(cache.controllerPos())) {
            return fail("multiblock.validation.extra_controller", pos, rl("multiblock_shell"), state);
        }
        if (state.is(port) && cache.getBlockEntity(pos) instanceof MultiblockPortBE) {
            cache.addWorkingPort(pos);
        }
        return true;
    }

    private static boolean isRingOffset(int i, int j) {
        boolean edgeI = Math.abs(i) == 3;
        boolean edgeJ = Math.abs(j) == 3;
        return edgeI != edgeJ;
    }

    private static BlockPos offset(BlockPos origin, int[][] basis, int i, int j) {
        return origin.offset(basis[0][0] * i + basis[1][0] * j,
                basis[0][1] * i + basis[1][1] * j,
                basis[0][2] * i + basis[1][2] * j);
    }

    private static int[][] basis(int[] normal) {
        if (normal[1] != 0) return new int[][]{{1, 0, 0}, {0, 0, 1}};
        if (normal[0] != 0) return new int[][]{{0, 1, 0}, {0, 0, 1}};
        return new int[][]{{1, 0, 0}, {0, 1, 0}};
    }

    private static boolean isCasing(BlockState state) {
        return state.is(KugelblitzTags.CASING);
    }

    private boolean isFrame(BlockState state) {
        return state.is(frame) || state.is(controller) || state.is(port);
    }
}
