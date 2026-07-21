package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.impl.CubicMultiblockValidator;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TurbineValidator implements IMultiblockValidator {

    private final CubicMultiblockValidator shape;

    private final Block controller;
    private final Block bearing;
    private final Block rotorShaft;
    private final Set<Block> coilBlocks = new HashSet<>();
    private final Set<Block> bladeBlocks = new HashSet<>();

    public TurbineValidator() {
        int min = Multiblocks.turbineMinSize;
        int max = Multiblocks.turbineMaxSize;

        controller = blockOf("turbine_controller");
        bearing = blockOf("turbine_bearing");
        rotorShaft = blockOf("turbine_rotor_shaft");

        Block port = blockOf("turbine_port");
        Block casing = blockOf("turbine_casing");
        Block glass = blockOf("turbine_glass");
        Set<Block> shellBlocks = new HashSet<>(List.of(port, casing, glass, bearing));
        for (String c : igentuman.nc.setup.entries.Turbine.COILS) {
            coilBlocks.add(blockOf("turbine_" + c + "_coil"));
        }
        shellBlocks.addAll(coilBlocks);

        Set<Block> interiorBlocks = new HashSet<>();
        interiorBlocks.add(rotorShaft);
        for (String b : igentuman.nc.setup.entries.Turbine.BLADES) {
            bladeBlocks.add(blockOf(b));
        }
        interiorBlocks.addAll(bladeBlocks);

        this.shape = new CubicMultiblockValidator(
                (state, be) -> state.is(controller),
                (state, be) -> shellBlocks.contains(state.getBlock()),
                (state, be) -> state.isAir() || interiorBlocks.contains(state.getBlock()),
                min, max, min, max, min, max);
    }

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!(cache instanceof TurbineCache tc)) {
            return shape.validate(level, controllerPos, facing, cache);
        }
        if (!shape.validate(level, controllerPos, facing, tc)) {
            tc.resetStats();
            return false;
        }
        tc.resetStats();

        List<BlockPos> bearings = new ArrayList<>();
        Map<Long, String> coilNames = new HashMap<>();
        classify(level, tc, bearings, coilNames);

        if (bearings.size() != 2) return fail(tc);

        int[] box = boundingBox(tc);
        int minX = box[0], minY = box[1], minZ = box[2], maxX = box[3], maxY = box[4], maxZ = box[5];
        tc.width = maxX - minX + 1;
        tc.height = maxY - minY + 1;
        tc.depth = maxZ - minZ + 1;

        Direction.Axis axis = axisOf(bearings.get(0), bearings.get(1));
        if (axis == null) return fail(tc);

        if (!checkProportions(axis, tc)) return fail(tc);

        int cx = (minX + maxX) / 2, cy = (minY + maxY) / 2, cz = (minZ + maxZ) / 2;

        Set<Long> expectedBearings = expectedBearings(axis, minX, minY, minZ, maxX, maxY, maxZ, cx, cy, cz);
        Set<Long> detectedBearings = new HashSet<>();
        for (BlockPos b : bearings) detectedBearings.add(b.asLong());
        if (!detectedBearings.equals(expectedBearings)) return fail(tc);

        Set<Long> rotorLine = expectedRotorLine(axis, minX, minY, minZ, maxX, maxY, maxZ, cx, cy, cz);
        if (!tc.rotorPositions.equals(rotorLine)) return fail(tc);
        for (long key : rotorLine) {
            BlockState bs = tc.getBlockState(level, BlockPos.of(key));
            if (!bs.hasProperty(TurbineRotorBlock.FACING)
                    || bs.getValue(TurbineRotorBlock.FACING).getAxis() != axis) {
                return fail(tc);
            }
        }

        double flow = countBlades(level, tc, rotorLine, axis);
        if (tc.bladeCount % 4 != 0) return fail(tc);

        countCoils(level, tc, coilNames);

        tc.axis = axis;
        tc.bearingPos1 = bearings.get(0).asLong();
        tc.bearingPos2 = bearings.get(1).asLong();
        tc.flow = flow;
        return true;
    }

    private boolean fail(TurbineCache tc) {
        tc.resetStats();
        tc.getStructurePositions().clear();
        return false;
    }

    private void classify(Level level, TurbineCache tc, List<BlockPos> bearings, Map<Long, String> coilNames) {
        for (long key : tc.getStructurePositions()) {
            BlockPos p = BlockPos.of(key);
            Block b = tc.getBlockState(level, p).getBlock();
            if (b == rotorShaft) {
                tc.rotorPositions.add(key);
            } else if (b == bearing) {
                bearings.add(p);
            } else if (coilBlocks.contains(b)) {
                tc.coilPositions.add(key);
                coilNames.put(key, coilName(b));
            }
        }
    }

    private int[] boundingBox(TurbineCache tc) {
        boolean first = true;
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for (long key : tc.getStructurePositions()) {
            BlockPos p = BlockPos.of(key);
            if (first) {
                minX = maxX = p.getX();
                minY = maxY = p.getY();
                minZ = maxZ = p.getZ();
                first = false;
            } else {
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                minZ = Math.min(minZ, p.getZ());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
                maxZ = Math.max(maxZ, p.getZ());
            }
        }
        return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    private static Direction.Axis axisOf(BlockPos a, BlockPos b) {
        int dx = a.getX() != b.getX() ? 1 : 0;
        int dy = a.getY() != b.getY() ? 1 : 0;
        int dz = a.getZ() != b.getZ() ? 1 : 0;
        if (dx + dy + dz != 1) return null;
        if (dx == 1) return Direction.Axis.X;
        if (dy == 1) return Direction.Axis.Y;
        return Direction.Axis.Z;
    }

    /** For the rotor axis, the two perpendicular bounding dims must be equal and odd (single-block cross-section centre). */
    private boolean checkProportions(Direction.Axis axis, TurbineCache tc) {
        int a, b;
        switch (axis) {
            case X -> { a = tc.height; b = tc.depth; }
            case Y -> { a = tc.width; b = tc.depth; }
            default -> { a = tc.width; b = tc.height; }
        }
        return a == b && a % 2 != 0;
    }

    private Set<Long> expectedBearings(Direction.Axis axis, int minX, int minY, int minZ,
                                       int maxX, int maxY, int maxZ, int cx, int cy, int cz) {
        Set<Long> set = new HashSet<>();
        switch (axis) {
            case X -> {
                set.add(new BlockPos(minX, cy, cz).asLong());
                set.add(new BlockPos(maxX, cy, cz).asLong());
            }
            case Y -> {
                set.add(new BlockPos(cx, minY, cz).asLong());
                set.add(new BlockPos(cx, maxY, cz).asLong());
            }
            default -> {
                set.add(new BlockPos(cx, cy, minZ).asLong());
                set.add(new BlockPos(cx, cy, maxZ).asLong());
            }
        }
        return set;
    }

    private Set<Long> expectedRotorLine(Direction.Axis axis, int minX, int minY, int minZ,
                                        int maxX, int maxY, int maxZ, int cx, int cy, int cz) {
        Set<Long> set = new HashSet<>();
        switch (axis) {
            case X -> {
                for (int x = minX + 1; x < maxX; x++) set.add(new BlockPos(x, cy, cz).asLong());
            }
            case Y -> {
                for (int y = minY + 1; y < maxY; y++) set.add(new BlockPos(cx, y, cz).asLong());
            }
            default -> {
                for (int z = minZ + 1; z < maxZ; z++) set.add(new BlockPos(cx, cy, z).asLong());
            }
        }
        return set;
    }

    /** Walks blades outward perpendicular to the rotor axis from each shaft segment; sums flow, tallies valid blades. */
    private double countBlades(Level level, TurbineCache tc, Set<Long> rotorLine, Direction.Axis axis) {
        double flow = 0;
        List<Direction> perpendicular = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d.getAxis() != axis) perpendicular.add(d);
        }
        for (long key : rotorLine) {
            BlockPos rotor = BlockPos.of(key);
            for (Direction d : perpendicular) {
                BlockPos p = rotor;
                while (true) {
                    p = p.relative(d);
                    Block b = tc.getBlockState(level, p).getBlock();
                    if (!bladeBlocks.contains(b)) break;
                    if (tc.bladePositions.add(p.asLong())) {
                        flow += bladeFlow(b);
                    }
                }
            }
        }
        tc.bladeCount = tc.bladePositions.size();
        return flow;
    }

    /** Validates coils in dependency schedule order, then averages efficiency exactly as the original (invalid coils contribute 0). */
    private void countCoils(Level level, TurbineCache tc, Map<Long, String> coilNames) {
        Map<String, List<Long>> byName = new HashMap<>();
        coilNames.forEach((pos, name) -> byName.computeIfAbsent(name, k -> new ArrayList<>()).add(pos));

        for (String name : ModEntries.COIL_SCHEDULE) {
            List<Long> positions = byName.get(name);
            if (positions == null) continue;
            TurbineCoilDef def = ModEntries.TURBINE_COILS.get(name);
            if (def == null) continue;
            for (long key : positions) {
                if (tc.validCoils.contains(key)) continue;
                if (TurbineCoilValidator.isValid(def, level, BlockPos.of(key), tc, tc.validCoils)) {
                    tc.validCoils.add(key);
                }
            }
        }

        List<Long> ordered = new ArrayList<>(tc.coilPositions);
        ordered.sort(Long::compareTo);
        double eff = 0;
        int active = 0;
        for (long key : ordered) {
            TurbineCoilDef def = ModEntries.TURBINE_COILS.get(coilNames.get(key));
            double real = (def != null && tc.validCoils.contains(key)) ? def.getEfficiency() : 0;
            if (eff == 0) eff = real;
            eff = (eff + real) / 2;
            if (tc.validCoils.contains(key)) active++;
        }
        tc.coilsEfficiency = eff;
        tc.activeCoils = active;
    }

    private double bladeFlow(Block b) {
        BladeDef def = ModEntries.TURBINE_BLADES.get(BuiltInRegistries.BLOCK.getKey(b).getPath());
        return def == null ? 0 : def.getEfficiency() / 100.0;
    }

    private static String coilName(Block b) {
        String path = BuiltInRegistries.BLOCK.getKey(b).getPath();
        if (path.startsWith("turbine_") && path.endsWith("_coil")) {
            return path.substring("turbine_".length(), path.length() - "_coil".length());
        }
        return null;
    }

    private static Block blockOf(String name) {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.block() == null) return Blocks.AIR;
        Block b = entry.block().get();
        return b == null ? Blocks.AIR : b;
    }
}
