package igentuman.nc.multiblock.fusion;

import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.api.multiblock.part.ElectromagnetDef;
import igentuman.nc.api.multiblock.part.RFAmplifierDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.MultiblockDebug.step;

public class FusionReactorValidator extends AbstractMultiblockValidator<FusionReactorCache> {

    private static final int MAX_SIZE = 32;
    private static final int MAX_CONNECTOR_DISTANCE = MAX_SIZE / 2 + 1;
    private static final List<Direction> SIDES = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final ResourceLocation EXPECTED_CASING = rl("fusion_reactor_casing");
    private static final ResourceLocation EXPECTED_CONNECTOR = rl("fusion_reactor_connector");
    private static final ResourceLocation EXPECTED_AIR = BuiltInRegistries.BLOCK.getKey(Blocks.AIR);

    private Block connector;
    private Block proxy;
    private int size;

    private boolean resolveBlocks() {
        if (connector == null) connector = blockOf("fusion_reactor_connector");
        if (proxy == null) proxy = blockOf("fusion_reactor_core_proxy");
        return connector != null;
    }

    @Override
    protected boolean findBounds(FusionReactorCache cache) {
        size = 0;
        BlockPos core = cache.controllerPos();
        if (!resolveBlocks()) {
            return fail("multiblock.fusion.size_unresolved", core, EXPECTED_CONNECTOR, cache.getBlockState(core.above()));
        }
        int reach = MAX_CONNECTOR_DISTANCE + 3;
        cache.prefetch(core.offset(-reach, 0, -reach), core.offset(reach, 2, reach));
        BlockPos mid = core.above();
        size = 1;
        for (int dist = 2; dist <= MAX_CONNECTOR_DISTANCE; dist++) {
            BlockPos missing = null;
            int count = 0;
            for (Direction side : SIDES) {
                BlockPos pos = mid.relative(side, dist);
                if (cache.getBlockState(pos).is(connector)) count++;
                else if (missing == null) missing = pos;
            }
            if (count == 4) {
                size = dist;
                continue;
            }
            if (count > 0) {
                return fail("multiblock.fusion.incomplete_connectors", missing, EXPECTED_CONNECTOR, cache.getBlockState(missing));
            }
            break;
        }
        int radius = size + 3;
        cache.setWorkingBounds(core.offset(-radius, 0, -radius), core.offset(radius, 2, radius));
        step("fusion ring validation size={}", size);
        return true;
    }

    private static List<BlockPos> ring(BlockPos center, int radius, boolean corners) {
        List<BlockPos> cells = new ArrayList<>(radius * 8);
        for (int i = -radius; i < radius; i++) {
            cells.add(center.offset(i, 0, -radius));
            cells.add(center.offset(radius, 0, i));
            cells.add(center.offset(-i, 0, radius));
            cells.add(center.offset(-radius, 0, -i));
        }
        if (!corners) cells.removeIf(p -> Math.abs(p.getX() - center.getX()) == radius && Math.abs(p.getZ() - center.getZ()) == radius);
        return cells;
    }

    @Override
    protected boolean validateShell(FusionReactorCache cache) {
        BlockPos mid = cache.controllerPos().above();
        for (BlockPos pos : ring(mid, size + 1, true)) {
            if (!casing(cache, pos, "multiblock.fusion.wrong_inner_wall")) return false;
        }
        for (BlockPos pos : ring(mid, size + 3, false)) {
            if (!casing(cache, pos, "multiblock.fusion.wrong_outer_wall")) return false;
        }
        for (BlockPos pos : ring(mid.below(), size + 2, true)) {
            if (!casing(cache, pos, "multiblock.fusion.wrong_bottom_wall")) return false;
        }
        for (BlockPos pos : ring(mid.above(), size + 2, true)) {
            if (!casing(cache, pos, "multiblock.fusion.wrong_top_wall")) return false;
        }
        return true;
    }

    private boolean casing(FusionReactorCache cache, BlockPos pos, String errorKey) {
        BlockState state = cache.getBlockState(pos);
        if (!state.is(FusionTags.CASING)) return fail(errorKey, pos, EXPECTED_CASING, state);
        cache.workingCasingCount++;
        return true;
    }

    @Override
    protected boolean validateInterior(FusionReactorCache cache) {
        step("fusion ring passed; validating empty plasma interior");
        for (BlockPos pos : ring(cache.controllerPos().above(), size + 2, true)) {
            BlockState state = cache.getBlockState(pos);
            if (!state.isAir()) return fail("multiblock.fusion.interior_not_empty", pos, EXPECTED_AIR, state);
        }
        return true;
    }

    @Override
    protected void calculateStatistics(FusionReactorCache cache) {
        BlockPos core = cache.controllerPos();
        Tally tally = new Tally();
        for (BlockPos layer : List.of(core, core.above(2))) {
            for (BlockPos pos : ring(layer, size + 1, true)) tally.add(cache, pos);
            for (BlockPos pos : ring(layer, size + 3, false)) tally.add(cache, pos);
        }
        cache.workingMagneticFieldStrength = tally.magneticField;
        cache.workingMagnetsEfficiency = tally.magnets > 0 ? (int) (tally.magnetEfficiency / tally.magnets) : 0;
        cache.workingRfEfficiency = tally.amplifiers > 0 ? (int) (tally.rfEfficiency / tally.amplifiers) : 0;
        cache.workingMaxMagnetsTemp = tally.magnets > 0 ? tally.maxMagnetTemp : 0;
        cache.workingMinRFAmplifiersTemp = tally.amplifiers > 0 ? tally.minRfTemp : 0;
        cache.workingMagnetsPower = tally.magnetPower;
        cache.workingRfAmplification = tally.rfAmplification;
        cache.workingRfAmplifiersPower = tally.rfPower;
        cache.workingMagnetCount = tally.magnets;
        cache.workingAmplifierCount = tally.amplifiers;
        cache.workingConnectorCount = SIDES.size() * (size - 1);
        cache.workingSize = size;
        collectProxies(cache, core);
        step("fusion validation passed casing={} connectors={} magnets={} amplifiers={}", cache.workingCasingCount,
                cache.workingConnectorCount, cache.workingMagnetCount, cache.workingAmplifierCount);
    }

    private void collectProxies(FusionReactorCache cache, BlockPos core) {
        if (proxy == null) return;
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos pos = core.offset(x, y, z);
                    if (cache.getBlockState(pos).is(proxy)) cache.addWorkingPort(pos);
                }
            }
        }
    }

    private static final class Tally {
        private double magneticField;
        private double magnetEfficiency;
        private double rfEfficiency;
        private int magnetPower;
        private int rfAmplification;
        private int rfPower;
        private int maxMagnetTemp = Integer.MAX_VALUE;
        private int minRfTemp = Integer.MAX_VALUE;
        private int magnets;
        private int amplifiers;

        private void add(FusionReactorCache cache, BlockPos pos) {
            String name = BuiltInRegistries.BLOCK.getKey(cache.getBlockState(pos).getBlock()).getPath();
            ElectromagnetDef magnet = ElectromagnetDef.get(name);
            if (magnet != null) {
                magneticField += magnet.magneticField;
                magnetEfficiency += magnet.efficiency;
                magnetPower += magnet.power;
                maxMagnetTemp = Math.min(maxMagnetTemp, magnet.maxTemp);
                magnets++;
                return;
            }
            RFAmplifierDef amplifier = RFAmplifierDef.get(name);
            if (amplifier != null) {
                rfAmplification += amplifier.voltage;
                rfPower += amplifier.power;
                rfEfficiency += amplifier.efficiency;
                minRfTemp = Math.min(minRfTemp, amplifier.maxTemp);
                amplifiers++;
            }
        }
    }
}
