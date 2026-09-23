package igentuman.nc.multiblock.fission;

import igentuman.nc.api.multiblock.AbstractCuboidValidator;
import igentuman.nc.api.multiblock.part.HeatSinkDef;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public class FissionReactorValidator extends AbstractCuboidValidator<FissionReactorCache> {

    private static final int MAX_MODERATOR_RUN = 4;

    private Block controller;
    private Block fuelCell;
    private Block irradiationChamber;
    private Block pileDriverChamber;

    @Override
    protected boolean resolveBlocks() {
        if (controller == null) controller = blockOf("fission_reactor_controller");
        if (fuelCell == null) fuelCell = blockOf("fission_reactor_solid_fuel_cell");
        if (irradiationChamber == null) irradiationChamber = blockOf("fission_reactor_irradiation_chamber");
        if (pileDriverChamber == null) pileDriverChamber = blockOf("fission_reactor_pile-driver_irradiation_chamber");
        return controller != null && fuelCell != null;
    }

    @Override
    protected int minSize() {
        return Multiblocks.fissionMinSize;
    }

    @Override
    protected int maxSize() {
        return Multiblocks.fissionMaxSize;
    }

    @Override
    protected boolean isController(BlockState state) {
        return state.is(controller);
    }

    @Override
    protected boolean isShell(BlockState state) {
        return state.is(FissionTags.CASING);
    }

    @Override
    protected boolean acceptShell(FissionReactorCache cache, BlockPos pos, BlockState state, boolean corner) {
        if (!isShell(state)) return fail("multiblock.validation.wrong_outer", pos, rl("multiblock_shell"), state);
        return true;
    }

    @Override
    protected boolean acceptInterior(FissionReactorCache cache, BlockPos pos, BlockState state) {
        if (state.isAir()) return true;
        if (!state.is(FissionTags.REACTOR_INNER)) {
            return fail("multiblock.validation.wrong_inner", pos, rl("multiblock_interior"), state);
        }
        long key = pos.asLong();
        Block block = state.getBlock();
        if (block instanceof HeatSinkBlock heatSink) {
            cache.workingHeatSinks.put(key, heatSink.getDef().name);
        } else if (block == fuelCell) {
            cache.workingFuelCells.add(key);
        } else if (state.is(FissionTags.MODERATORS)) {
            cache.workingModerators.add(key);
        }
        if (isIrradiator(block)) cache.workingIrradiators.add(key);
        return true;
    }

    private boolean isIrradiator(Block block) {
        return (irradiationChamber != null && block == irradiationChamber)
                || (pileDriverChamber != null && block == pileDriverChamber);
    }

    @Override
    protected boolean validateRelations(FissionReactorCache cache) {
        computeFuelCellAttachments(cache);
        validateHeatSinks(cache);
        computeIrradiators(cache);
        sumCooling(cache);
        return true;
    }

    private void computeFuelCellAttachments(FissionReactorCache cache) {
        for (long key : cache.workingFuelCells) {
            BlockPos pos = BlockPos.of(key);
            int extra = 0;
            for (Direction dir : Direction.values()) {
                if (hasLinkedCell(cache, pos, dir)) extra++;
            }
            cache.workingCellsHeatMult += (extra + 1) * (extra + 2) / 2.0;
            cache.workingCellsEnergyMult += extra + 1;

            int moderators = 0;
            for (Direction dir : Direction.values()) {
                BlockPos neighbour = pos.relative(dir);
                if (cache.getBlockState(neighbour).is(FissionTags.MODERATORS)) {
                    cache.workingActiveModerators.add(neighbour.asLong());
                    moderators++;
                }
            }
            cache.workingModeratorsHeatMult += moderators * (extra + 1) * (Multiblocks.fissionModeratorHeatMultiplier / 100.0);
            cache.workingModeratorsEnergyMult += moderators * (extra + 1) * (Multiblocks.fissionModeratorFeMultiplier / 100.0);
        }
    }

    private boolean hasLinkedCell(FissionReactorCache cache, BlockPos from, Direction dir) {
        int moderators = 0;
        BlockPos pos = from;
        for (int step = 1; step <= MAX_MODERATOR_RUN + 1; step++) {
            pos = pos.relative(dir);
            BlockState state = cache.getBlockState(pos);
            if (state.getBlock() == fuelCell) return true;
            if (state.is(FissionTags.MODERATORS)) {
                if (++moderators > MAX_MODERATOR_RUN) return false;
                continue;
            }
            return false;
        }
        return false;
    }

    private void validateHeatSinks(FissionReactorCache cache) {
        Map<String, List<Long>> byName = new HashMap<>();
        cache.workingHeatSinks.forEach((pos, name) -> byName.computeIfAbsent(name, k -> new ArrayList<>()).add(pos));

        for (String name : ModEntries.HS_SCHEDULE) {
            List<Long> positions = byName.get(name);
            if (positions == null) continue;
            HeatSinkEntry entry = ModEntries.HEAT_SINKS.get(name);
            if (entry == null || !entry.isEnabled()) continue;
            HeatSinkDef def = entry.def();
            for (long key : positions) {
                if (cache.workingValidHeatSinks.contains(key)) continue;
                if (HeatSinkValidator.isValid(def, BlockPos.of(key), cache)) {
                    cache.workingValidHeatSinks.add(key);
                }
            }
        }
    }

    private void computeIrradiators(FissionReactorCache cache) {
        for (long key : cache.workingIrradiators) {
            BlockPos pos = BlockPos.of(key);
            for (Direction dir : Direction.values()) {
                BlockPos first = pos.relative(dir);
                BlockPos second = first.relative(dir);
                if (cache.getBlockState(first).is(FissionTags.MODERATORS)
                        && cache.workingActiveModerators.contains(first.asLong())
                        && cache.getBlockState(second).getBlock() == fuelCell) {
                    cache.workingIrradiationLines++;
                }
            }
        }
    }

    private void sumCooling(FissionReactorCache cache) {
        int[] active = new int[ActiveCoolant.COUNT];
        for (long key : cache.workingValidHeatSinks) {
            String name = cache.workingHeatSinks.get(key);
            HeatSinkEntry entry = ModEntries.HEAT_SINKS.get(name);
            if (entry == null) continue;
            if (entry.def().isActive()) {
                ActiveCoolant coolant = ActiveCoolant.bySinkName(name);
                if (coolant != null) active[coolant.ordinal()]++;
            } else {
                cache.workingTotalCooling += entry.def().heat;
            }
        }
        cache.workingActiveCoolantCounts = active;
    }
}
