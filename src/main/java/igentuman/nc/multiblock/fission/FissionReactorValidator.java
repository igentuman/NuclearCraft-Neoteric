package igentuman.nc.multiblock.fission;

import igentuman.nc.api.impl.CubicMultiblockValidator;
import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fission reactor validator. Validates the cuboid casing shell + interior shape via a composed
 * {@link CubicMultiblockValidator}, then classifies interior components and derives reactor stats
 * into the {@link FissionReactorCache}.
 *
 * <p>Runs on the multiblock executor thread (single thread per dimension), so the transient
 * component-block fields are safe to reuse across {@link #validate} calls.
 */
public class FissionReactorValidator implements IMultiblockValidator {

    private static final int MAX_MODERATOR_RUN = 4;

    private final CubicMultiblockValidator shape;

    private Block fuelCell = Blocks.AIR;
    private Block irradiationChamber = Blocks.AIR;
    private Block pileDriverChamber = Blocks.AIR;

    public FissionReactorValidator() {
        int min = Multiblocks.fissionMinSize;
        int max = Multiblocks.fissionMaxSize;
        this.shape = new CubicMultiblockValidator(
                (state, be) -> state.is(blockOf("fission_reactor_controller")),
                BlockPredicate.ofTag(FissionTags.CASING),
                (state, be) -> state.isAir() || state.is(FissionTags.REACTOR_INNER),
                min, max, min, max, min, max);
    }

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!(cache instanceof FissionReactorCache fc)) {
            return shape.validate(level, controllerPos, facing, cache);
        }
        if (!shape.validate(level, controllerPos, facing, fc)) {
            fc.resetStats();
            return false;
        }
        fc.resetStats();
        fuelCell = blockOf("fission_reactor_solid_fuel_cell");
        irradiationChamber = blockOf("fission_reactor_irradiation_chamber");
        pileDriverChamber = blockOf("fission_reactor_pile-driver_irradiation_chamber");

        classify(level, fc);
        computeFuelCellAttachments(level, fc);
        validateHeatSinks(level, fc);
        computeIrradiators(level, fc);
        sumCooling(fc);
        computeDimensions(fc);
        fc.fuelCellCount = fc.fuelCells.size();
        return true;
    }

    /** Bounding box of the structure (used by the logic to scale heat capacity). */
    private void computeDimensions(FissionReactorCache fc) {
        boolean first = true;
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for (long key : fc.getStructurePositions()) {
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
        fc.width = maxX - minX + 1;
        fc.height = maxY - minY + 1;
        fc.depth = maxZ - minZ + 1;
    }

    private void classify(Level level, FissionReactorCache fc) {
        for (long key : fc.getStructurePositions()) {
            BlockPos p = BlockPos.of(key);
            BlockState bs = fc.getBlockState(level, p);
            Block b = bs.getBlock();
            if (b instanceof HeatSinkBlock hsb) {
                fc.heatSinks.put(key, hsb.getDef().name);
            } else if (b == fuelCell) {
                fc.fuelCells.add(key);
            } else if (bs.is(FissionTags.MODERATORS)) {
                fc.allModerators.add(key);
            }
            if (b == irradiationChamber || b == pileDriverChamber) {
                fc.irradiators.add(key);
            }
        }
    }

    /** Per fuel cell: count linked neighbor cells (through moderator runs) and mark adjacent
     *  moderators active, accumulating the cell + moderator multipliers. */
    private void computeFuelCellAttachments(Level level, FissionReactorCache fc) {
        for (long key : fc.fuelCells) {
            BlockPos p = BlockPos.of(key);
            int extra = 0;
            for (Direction dir : Direction.values()) {
                if (hasLinkedCell(level, fc, p, dir)) extra++;
            }
            fc.cellsHeatMult += (extra + 1) * (extra + 2) / 2.0;
            fc.cellsEnergyMult += extra + 1;

            int mods = 0;
            for (Direction dir : Direction.values()) {
                BlockPos np = p.relative(dir);
                if (fc.getBlockState(level, np).is(FissionTags.MODERATORS)) {
                    fc.activeModerators.add(np.asLong());
                    mods++;
                }
            }
            fc.moderatorsHeatMult += mods * (extra + 1) * (Multiblocks.fissionModeratorHeatMultiplier / 100.0);
            fc.moderatorsEnergyMult += mods * (extra + 1) * (Multiblocks.fissionModeratorFeMultiplier / 100.0);
        }
    }

    /** A direction links to another fuel cell directly, or through a run of up to
     *  {@link #MAX_MODERATOR_RUN} moderators terminated by a fuel cell. */
    private boolean hasLinkedCell(Level level, FissionReactorCache fc, BlockPos from, Direction dir) {
        int mods = 0;
        BlockPos p = from;
        for (int k = 1; k <= MAX_MODERATOR_RUN + 1; k++) {
            p = p.relative(dir);
            BlockState bs = fc.getBlockState(level, p);
            if (bs.getBlock() == fuelCell) return true;
            if (bs.is(FissionTags.MODERATORS)) {
                if (++mods > MAX_MODERATOR_RUN) return false;
                continue;
            }
            return false;
        }
        return false;
    }

    /** Validate heat sinks in topological dependency order (a sink rule may reference another
     *  sink type, which must be validated first). Cyclic groups appear twice in the schedule. */
    private void validateHeatSinks(Level level, FissionReactorCache fc) {
        Map<String, List<Long>> byName = new HashMap<>();
        fc.heatSinks.forEach((pos, name) -> byName.computeIfAbsent(name, k -> new ArrayList<>()).add(pos));

        for (String name : ModEntries.HS_SCHEDULE) {
            List<Long> positions = byName.get(name);
            if (positions == null) continue;
            HeatSinkEntry entry = ModEntries.HEAT_SINKS.get(name);
            if (entry == null || !entry.isEnabled()) continue;
            HeatSinkDef def = entry.def();
            for (long key : positions) {
                if (fc.validHeatSinks.contains(key)) continue;
                BlockPos p = BlockPos.of(key);
                if (HeatSinkValidator.isValid(def, level, p, fc)) {
                    fc.validHeatSinks.add(key);
                    if (def.isActive()) fc.activeHeatSinks.add(key);
                }
            }
        }
    }

    /** An irradiator scores a line per axis where: neighbor is an active moderator and the block
     *  beyond it is a fuel cell (chamber -> moderator -> fuel cell). */
    private void computeIrradiators(Level level, FissionReactorCache fc) {
        for (long key : fc.irradiators) {
            BlockPos p = BlockPos.of(key);
            boolean anyLine = false;
            for (Direction dir : Direction.values()) {
                BlockPos n1 = p.relative(dir);
                BlockPos n2 = n1.relative(dir);
                if (fc.getBlockState(level, n1).is(FissionTags.MODERATORS)
                        && fc.activeModerators.contains(n1.asLong())
                        && fc.getBlockState(level, n2).getBlock() == fuelCell) {
                    fc.irradiationLines++;
                    anyLine = true;
                }
            }
            if (anyLine) fc.validIrradiators.add(key);
        }
    }

    /** Passive sinks contribute their cooling directly; active sinks are tallied per coolant so the
     *  runtime logic can cool only when the matching coolant fluid is available. */
    private void sumCooling(FissionReactorCache fc) {
        int[] active = new int[ActiveCoolant.COUNT];
        for (long key : fc.validHeatSinks) {
            String name = fc.heatSinks.get(key);
            HeatSinkEntry entry = ModEntries.HEAT_SINKS.get(name);
            if (entry == null) continue;
            if (entry.def().isActive()) {
                ActiveCoolant c = ActiveCoolant.bySinkName(name);
                if (c != null) active[c.ordinal()]++;
            } else {
                fc.totalCooling += entry.def().heat;
            }
        }
        fc.activeCoolantCounts = active;
    }

    private static Block blockOf(String name) {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.block() == null) return Blocks.AIR;
        Block b = entry.block().get();
        return b == null ? Blocks.AIR : b;
    }
}
