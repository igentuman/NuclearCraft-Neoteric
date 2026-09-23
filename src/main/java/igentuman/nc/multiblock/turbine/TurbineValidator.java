package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.multiblock.AbstractCuboidValidator;
import igentuman.nc.api.multiblock.part.BladeDef;
import igentuman.nc.api.multiblock.part.TurbineCoilDef;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.Turbine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public class TurbineValidator extends AbstractCuboidValidator<TurbineCache> {

    private Block controller;
    private Block bearing;
    private Block rotorShaft;
    private Block port;
    private Block casing;
    private Block glass;
    private final Set<Block> coilBlocks = new HashSet<>();
    private final Set<Block> bladeBlocks = new HashSet<>();
    private boolean resolved;

    @Override
    protected boolean resolveBlocks() {
        if (resolved) return true;
        controller = blockOf("turbine_controller");
        bearing = blockOf("turbine_bearing");
        rotorShaft = blockOf("turbine_rotor_shaft");
        port = blockOf("turbine_port");
        casing = blockOf("turbine_casing");
        glass = blockOf("turbine_glass");
        coilBlocks.clear();
        for (String coil : Turbine.COILS) {
            Block block = blockOf("turbine_" + coil + "_coil");
            if (block != null) coilBlocks.add(block);
        }
        bladeBlocks.clear();
        for (String blade : Turbine.BLADES) {
            Block block = blockOf(blade);
            if (block != null) bladeBlocks.add(block);
        }
        resolved = controller != null && bearing != null && rotorShaft != null && port != null
                && casing != null && glass != null;
        return resolved;
    }

    @Override
    protected int minSize() {
        return Multiblocks.turbineMinSize;
    }

    @Override
    protected int maxSize() {
        return Multiblocks.turbineMaxSize;
    }

    @Override
    protected boolean isController(BlockState state) {
        return state.is(controller);
    }

    @Override
    protected boolean isShell(BlockState state) {
        Block block = state.getBlock();
        return block == casing || block == glass || block == port || block == bearing || coilBlocks.contains(block);
    }

    @Override
    protected boolean acceptShell(TurbineCache cache, BlockPos pos, BlockState state, boolean corner) {
        if (!isShell(state)) return fail("multiblock.validation.wrong_outer", pos, rl("multiblock_shell"), state);
        Block block = state.getBlock();
        if (block == bearing) {
            cache.workingBearings.add(pos.immutable());
        } else if (coilBlocks.contains(block)) {
            cache.workingCoils.put(pos.asLong(), coilName(block));
        }
        return true;
    }

    @Override
    protected boolean acceptInterior(TurbineCache cache, BlockPos pos, BlockState state) {
        if (state.isAir()) return true;
        Block block = state.getBlock();
        if (block == rotorShaft) {
            cache.workingRotors.add(pos.asLong());
            return true;
        }
        if (bladeBlocks.contains(block)) return true;
        return fail("multiblock.validation.wrong_inner", pos, rl("multiblock_interior"), state);
    }

    @Override
    protected boolean validateRelations(TurbineCache cache) {
        BlockPos controllerPos = cache.controllerPos();
        List<BlockPos> bearings = cache.workingBearings;
        step("turbine classified bearings={} rotorBlocks={} coils={}", bearings.size(),
                cache.workingRotors.size(), cache.workingCoils.size());
        if (bearings.size() != 2) {
            return fail("multiblock.turbine.bearing_count", controllerPos, rl("turbine_bearing"), (ResourceLocation) null);
        }

        BlockPos min = cache.workingMin();
        BlockPos max = cache.workingMax();
        Direction.Axis axis = axisOf(bearings.get(0), bearings.get(1));
        if (axis == null) {
            return fail("multiblock.turbine.bearings_not_aligned", bearings.get(0), rl("aligned_turbine_bearings"),
                    (ResourceLocation) null);
        }
        if (!checkProportions(axis, min, max)) {
            return fail("multiblock.turbine.wrong_proportions", controllerPos, rl("odd_square_turbine_cross_section"),
                    (ResourceLocation) null);
        }

        BlockPos center = new BlockPos((min.getX() + max.getX()) / 2, (min.getY() + max.getY()) / 2,
                (min.getZ() + max.getZ()) / 2);

        Set<Long> expectedBearings = expectedBearings(axis, min, max, center);
        Set<Long> detectedBearings = new HashSet<>();
        for (BlockPos pos : bearings) detectedBearings.add(pos.asLong());
        if (!detectedBearings.equals(expectedBearings)) {
            return fail("multiblock.turbine.wrong_bearing_position",
                    mismatch(detectedBearings, expectedBearings, controllerPos), rl("turbine_bearing"),
                    (ResourceLocation) null);
        }

        Set<Long> rotorLine = expectedRotorLine(axis, min, max, center);
        if (!cache.workingRotors.equals(rotorLine)) {
            return fail("multiblock.turbine.wrong_rotor_line", mismatch(cache.workingRotors, rotorLine, controllerPos),
                    rl("turbine_rotor_shaft"), (ResourceLocation) null);
        }
        for (long key : rotorLine) {
            BlockPos pos = BlockPos.of(key);
            BlockState state = cache.getBlockState(pos);
            if (!state.hasProperty(TurbineRotorBlock.FACING)
                    || state.getValue(TurbineRotorBlock.FACING).getAxis() != axis) {
                return fail("multiblock.turbine.rotor_facing", pos, rl("turbine_rotor_shaft"), state);
            }
        }

        cache.workingFlow = countBlades(cache, rotorLine, axis);
        if (cache.workingBlades.size() % 4 != 0) {
            return fail("multiblock.turbine.incomplete_blade_set", controllerPos, rl("turbine_blade_set"),
                    (ResourceLocation) null);
        }
        cache.workingAxis = axis;
        return true;
    }

    @Override
    protected void calculateStatistics(TurbineCache cache) {
        countCoils(cache);
        step("turbine validation passed axis={} blades={} activeCoils={} flow={}", cache.workingAxis,
                cache.workingBlades.size(), cache.workingActiveCoils, cache.workingFlow);
    }

    private static BlockPos mismatch(Set<Long> detected, Set<Long> expected, BlockPos fallback) {
        for (long key : expected) if (!detected.contains(key)) return BlockPos.of(key);
        for (long key : detected) if (!expected.contains(key)) return BlockPos.of(key);
        return fallback;
    }

    private static Direction.Axis axisOf(BlockPos first, BlockPos second) {
        int dx = first.getX() != second.getX() ? 1 : 0;
        int dy = first.getY() != second.getY() ? 1 : 0;
        int dz = first.getZ() != second.getZ() ? 1 : 0;
        if (dx + dy + dz != 1) return null;
        if (dx == 1) return Direction.Axis.X;
        if (dy == 1) return Direction.Axis.Y;
        return Direction.Axis.Z;
    }

    private static boolean checkProportions(Direction.Axis axis, BlockPos min, BlockPos max) {
        int width = max.getX() - min.getX() + 1;
        int height = max.getY() - min.getY() + 1;
        int depth = max.getZ() - min.getZ() + 1;
        int first;
        int second;
        switch (axis) {
            case X -> {
                first = height;
                second = depth;
            }
            case Y -> {
                first = width;
                second = depth;
            }
            default -> {
                first = width;
                second = height;
            }
        }
        return first == second && first % 2 != 0;
    }

    private static Set<Long> expectedBearings(Direction.Axis axis, BlockPos min, BlockPos max, BlockPos center) {
        Set<Long> set = new HashSet<>();
        switch (axis) {
            case X -> {
                set.add(new BlockPos(min.getX(), center.getY(), center.getZ()).asLong());
                set.add(new BlockPos(max.getX(), center.getY(), center.getZ()).asLong());
            }
            case Y -> {
                set.add(new BlockPos(center.getX(), min.getY(), center.getZ()).asLong());
                set.add(new BlockPos(center.getX(), max.getY(), center.getZ()).asLong());
            }
            default -> {
                set.add(new BlockPos(center.getX(), center.getY(), min.getZ()).asLong());
                set.add(new BlockPos(center.getX(), center.getY(), max.getZ()).asLong());
            }
        }
        return set;
    }

    private static Set<Long> expectedRotorLine(Direction.Axis axis, BlockPos min, BlockPos max, BlockPos center) {
        Set<Long> set = new HashSet<>();
        switch (axis) {
            case X -> {
                for (int x = min.getX() + 1; x < max.getX(); x++) {
                    set.add(new BlockPos(x, center.getY(), center.getZ()).asLong());
                }
            }
            case Y -> {
                for (int y = min.getY() + 1; y < max.getY(); y++) {
                    set.add(new BlockPos(center.getX(), y, center.getZ()).asLong());
                }
            }
            default -> {
                for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                    set.add(new BlockPos(center.getX(), center.getY(), z).asLong());
                }
            }
        }
        return set;
    }

    private double countBlades(TurbineCache cache, Set<Long> rotorLine, Direction.Axis axis) {
        double flow = 0;
        List<Direction> perpendicular = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis) perpendicular.add(direction);
        }
        for (long key : rotorLine) {
            BlockPos rotor = BlockPos.of(key);
            for (Direction direction : perpendicular) {
                BlockPos pos = rotor;
                while (true) {
                    pos = pos.relative(direction);
                    Block block = cache.getBlockState(pos).getBlock();
                    if (!bladeBlocks.contains(block)) break;
                    if (cache.workingBlades.add(pos.asLong())) flow += bladeFlow(block);
                }
            }
        }
        return flow;
    }

    private void countCoils(TurbineCache cache) {
        Map<String, List<Long>> byName = new HashMap<>();
        cache.workingCoils.forEach((pos, name) -> byName.computeIfAbsent(name, k -> new ArrayList<>()).add(pos));

        for (String name : ModEntries.COIL_SCHEDULE) {
            List<Long> positions = byName.get(name);
            if (positions == null) continue;
            TurbineCoilDef def = ModEntries.TURBINE_COILS.get(name);
            if (def == null) continue;
            for (long key : positions) {
                if (cache.workingValidCoils.contains(key)) continue;
                if (TurbineCoilValidator.isValid(def, BlockPos.of(key), cache)) {
                    cache.workingValidCoils.add(key);
                }
            }
        }

        List<Long> ordered = new ArrayList<>(cache.workingCoils.keySet());
        ordered.sort(Long::compareTo);
        double efficiency = 0;
        int active = 0;
        for (long key : ordered) {
            TurbineCoilDef def = ModEntries.TURBINE_COILS.get(cache.workingCoils.get(key));
            boolean valid = cache.workingValidCoils.contains(key);
            double real = def != null && valid ? def.getEfficiency() : 0;
            if (efficiency == 0) efficiency = real;
            efficiency = (efficiency + real) / 2;
            if (valid) active++;
        }
        cache.workingCoilsEfficiency = efficiency;
        cache.workingActiveCoils = active;
    }

    private static double bladeFlow(Block block) {
        BladeDef def = ModEntries.TURBINE_BLADES.get(BuiltInRegistries.BLOCK.getKey(block).getPath());
        return def == null ? 0 : def.getEfficiency() / 100.0;
    }

    private static String coilName(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (path.startsWith("turbine_") && path.endsWith("_coil")) {
            return path.substring("turbine_".length(), path.length() - "_coil".length());
        }
        return null;
    }
}
