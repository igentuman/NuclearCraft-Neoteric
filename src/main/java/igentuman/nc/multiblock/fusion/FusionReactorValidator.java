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
        size = resolveSize(cache, core.above());
        int radius = size + 3;
        cache.setWorkingBounds(core.offset(-radius, 0, -radius), core.offset(radius, 2, radius));
        step("fusion ring validation size={}", size);
        return true;
    }

    private int resolveSize(FusionReactorCache cache, BlockPos mid) {
        int resolved = 1;
        for (int dist = 2; dist <= MAX_CONNECTOR_DISTANCE; dist++) {
            int count = 0;
            for (Direction side : SIDES) {
                if (cache.getBlockState(mid.relative(side, dist)).is(connector)) count++;
            }
            if (count != 4) break;
            resolved = dist;
        }
        return resolved;
    }

    private static Direction offsetDir(Direction side) {
        return side.getAxis() == Direction.Axis.Z ? side.getCounterClockWise() : side.getClockWise();
    }

    private static BlockPos corner(BlockPos origin, Direction side, int outward, int along) {
        return origin.relative(side, outward).relative(offsetDir(side), along);
    }

    @Override
    protected boolean validateShell(FusionReactorCache cache) {
        BlockPos mid = cache.controllerPos().above();
        int shift = size + 1;
        int wallLen = size * 2 + 3;
        int outerWallLen = wallLen + 2;
        for (Direction side : SIDES) {
            Direction walk = offsetDir(side).getOpposite();
            BlockPos innerStart = corner(mid, side, shift, shift);
            BlockPos outerStart = corner(mid, side, shift + 2, shift + 1);
            BlockPos bottomStart = corner(mid, side, shift + 1, shift + 1).below();
            BlockPos topStart = corner(mid, side, shift + 1, shift + 1).above();
            for (int i = 0; i < wallLen; i++) {
                if (!casing(cache, innerStart.relative(walk, i), "multiblock.fusion.wrong_inner_wall")) return false;
                cache.workingCasingCount++;
            }
            for (int i = 0; i < outerWallLen; i++) {
                if (!casing(cache, outerStart.relative(walk, i), "multiblock.fusion.wrong_outer_wall")) return false;
                if (!casing(cache, bottomStart.relative(walk, i), "multiblock.fusion.wrong_bottom_wall")) return false;
                if (!casing(cache, topStart.relative(walk, i), "multiblock.fusion.wrong_top_wall")) return false;
                cache.workingCasingCount += 3;
            }
        }
        return true;
    }

    private boolean casing(FusionReactorCache cache, BlockPos pos, String errorKey) {
        BlockState state = cache.getBlockState(pos);
        if (state.is(FusionTags.CASING)) return true;
        return fail(errorKey, pos, EXPECTED_CASING, state);
    }

    @Override
    protected boolean validateInterior(FusionReactorCache cache) {
        step("fusion ring passed; validating empty plasma interior");
        BlockPos mid = cache.controllerPos().above();
        int shift = size + 2;
        int walkLen = size * 2 + 3;
        for (Direction side : SIDES) {
            Direction walk = offsetDir(side).getOpposite();
            BlockPos start = corner(mid, side, shift, shift);
            for (int i = 0; i < walkLen; i++) {
                BlockPos pos = start.relative(walk, i);
                BlockState state = cache.getBlockState(pos);
                if (!state.isAir()) return fail("multiblock.fusion.interior_not_empty", pos, EXPECTED_AIR, state);
            }
        }
        return true;
    }

    @Override
    protected void calculateStatistics(FusionReactorCache cache) {
        BlockPos core = cache.controllerPos();
        int shift = size + 1;
        int wallLen = size * 2 + 3;
        int outerWallLen = wallLen + 2;
        Tally tally = new Tally();
        for (Direction side : SIDES) {
            Direction walk = offsetDir(side).getOpposite();
            BlockPos innerStart = corner(core, side, shift, shift);
            BlockPos outerStart = corner(core, side, shift + 2, shift + 1);
            for (int i = 0; i < wallLen; i++) {
                tally.add(cache, innerStart.relative(walk, i));
                tally.add(cache, innerStart.relative(walk, i).above(2));
            }
            for (int i = 0; i < outerWallLen; i++) {
                tally.add(cache, outerStart.relative(walk, i));
                tally.add(cache, outerStart.relative(walk, i).above(2));
            }
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
