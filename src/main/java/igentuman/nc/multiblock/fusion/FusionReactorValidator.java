package igentuman.nc.multiblock.fusion;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Validates the fusion reactor's toroidal ring and empty interior, then tallies magnet and amplifier stats. */
public class FusionReactorValidator implements IMultiblockValidator {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 32;

    @Override
    public boolean validate(Level level, BlockPos corePos, Direction facing, IMultiblockCache cache) {
        NuclearCraft.LOGGER.debug("[Fusion] validate start core={} facing={} cache={}",
                corePos, facing, cache == null ? "null" : cache.getClass().getSimpleName());
        if (!(cache instanceof FusionReactorCache fc)) {
            NuclearCraft.LOGGER.debug("[Fusion] validate FAIL: cache is not FusionReactorCache");
            return false;
        }
        fc.resetStats();
        fc.getStructurePositions().clear();

        int size = resolveSize(level, corePos);
        NuclearCraft.LOGGER.debug("[Fusion] resolved size={}", size);
        if (size < MIN_SIZE) {
            NuclearCraft.LOGGER.debug("[Fusion] validate FAIL: size {} < MIN_SIZE {}", size, MIN_SIZE);
            return false;
        }

        if (!validateRing(level, corePos, size, fc)) {
            NuclearCraft.LOGGER.debug("[Fusion] validate FAIL: ring invalid");
            return false;
        }
        if (!validateInterior(level, corePos, size, fc)) {
            NuclearCraft.LOGGER.debug("[Fusion] validate FAIL: interior not empty");
            return false;
        }

        collectFunctionalParts(level, corePos, size, fc);
        addCoreProxies(corePos, fc);

        fc.size = size;
        NuclearCraft.LOGGER.debug("[Fusion] validate OK size={} casing={} connectors={} magnets={} amplifiers={} magField={} magPower={} magEff={} maxMagTemp={} rfAmp={} rfPower={} rfEff={} minRfTemp={} positions={}",
                size, fc.casingCount, fc.connectorCount, fc.magnetCount, fc.amplifierCount,
                fc.magneticFieldStrength, fc.magnetsPower, fc.magnetsEfficiency, fc.maxMagnetsTemp,
                fc.rfAmplification, fc.rfAmplifiersPower, fc.rfEfficiency, fc.minRFAmplifiersTemp,
                fc.getStructurePositions().size());
        return true;
    }

    private int resolveSize(Level level, BlockPos corePos) {
        BlockPos mid = corePos.above();
        Block connector = blockOf("fusion_reactor_connector");
        if (connector == Blocks.AIR) {
            NuclearCraft.LOGGER.debug("[Fusion] resolveSize FAIL: block 'fusion_reactor_connector' not registered (got AIR)");
            return 0;
        }

        int size = 1;
        for (int dist = 2; dist <= MAX_SIZE / 2 + 1; dist++) {
            int count = 0;
            for (Direction side : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                BlockPos p = mid.relative(side, dist);
                if (level.getBlockState(p).is(connector)) count++;
            }
            NuclearCraft.LOGGER.debug("[Fusion] resolveSize: dist={} connectorsFound={}/4", dist, count);
            if (count == 4) {
                size = dist;
            } else {
                break;
            }
        }
        return size;
    }

    private boolean validateRing(Level level, BlockPos corePos, int size, FusionReactorCache fc) {
        BlockPos mid = corePos.above();
        int shift = size + 1;
        int wallLen = size * 2 + 3;
        int outerWallLen = wallLen + 2;

        for (Direction side : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            Direction walkDir;
            BlockPos innerStart, outerStart, bottomStart, topStart;

            switch (side) {
                case NORTH -> {
                    walkDir = Direction.EAST;
                    innerStart  = mid.relative(Direction.NORTH, shift).relative(Direction.WEST, shift);
                    outerStart  = mid.relative(Direction.NORTH, shift + 2).relative(Direction.WEST, shift + 1);
                    bottomStart = mid.relative(Direction.NORTH, shift + 1).relative(Direction.WEST, shift + 1).below();
                    topStart    = mid.relative(Direction.NORTH, shift + 1).relative(Direction.WEST, shift + 1).above();
                }
                case SOUTH -> {
                    walkDir = Direction.WEST;
                    innerStart  = mid.relative(Direction.SOUTH, shift).relative(Direction.EAST, shift);
                    outerStart  = mid.relative(Direction.SOUTH, shift + 2).relative(Direction.EAST, shift + 1);
                    bottomStart = mid.relative(Direction.SOUTH, shift + 1).relative(Direction.EAST, shift + 1).below();
                    topStart    = mid.relative(Direction.SOUTH, shift + 1).relative(Direction.EAST, shift + 1).above();
                }
                case WEST -> {
                    walkDir = Direction.SOUTH;
                    innerStart  = mid.relative(Direction.WEST, shift).relative(Direction.NORTH, shift);
                    outerStart  = mid.relative(Direction.WEST, shift + 2).relative(Direction.NORTH, shift + 1);
                    bottomStart = mid.relative(Direction.WEST, shift + 1).relative(Direction.NORTH, shift + 1).below();
                    topStart    = mid.relative(Direction.WEST, shift + 1).relative(Direction.NORTH, shift + 1).above();
                }
                default -> {
                    walkDir = Direction.NORTH;
                    innerStart  = mid.relative(Direction.EAST, shift).relative(Direction.SOUTH, shift);
                    outerStart  = mid.relative(Direction.EAST, shift + 2).relative(Direction.SOUTH, shift + 1);
                    bottomStart = mid.relative(Direction.EAST, shift + 1).relative(Direction.SOUTH, shift + 1).below();
                    topStart    = mid.relative(Direction.EAST, shift + 1).relative(Direction.SOUTH, shift + 1).above();
                }
            }

            for (int i = 0; i < wallLen; i++) {
                BlockPos p = innerStart.relative(walkDir, i);
                if (!isCasing(level, p)) {
                    NuclearCraft.LOGGER.debug("[Fusion] ring FAIL: side={} innerWall i={}/{} pos={} block={} (expected casing tag)",
                            side, i, wallLen, p, blockName(level, p));
                    return false;
                }
                fc.getStructurePositions().add(p.asLong());
                fc.casingCount++;
            }

            for (int i = 0; i < outerWallLen; i++) {
                BlockPos po = outerStart.relative(walkDir, i);
                BlockPos pb = bottomStart.relative(walkDir, i);
                BlockPos pt = topStart.relative(walkDir, i);
                if (!isCasing(level, po)) {
                    NuclearCraft.LOGGER.debug("[Fusion] ring FAIL: side={} outerWall i={}/{} pos={} block={} (expected casing tag)",
                            side, i, outerWallLen, po, blockName(level, po));
                    return false;
                }
                if (!isCasing(level, pb)) {
                    NuclearCraft.LOGGER.debug("[Fusion] ring FAIL: side={} bottomWall i={}/{} pos={} block={} (expected casing tag)",
                            side, i, outerWallLen, pb, blockName(level, pb));
                    return false;
                }
                if (!isCasing(level, pt)) {
                    NuclearCraft.LOGGER.debug("[Fusion] ring FAIL: side={} topWall i={}/{} pos={} block={} (expected casing tag)",
                            side, i, outerWallLen, pt, blockName(level, pt));
                    return false;
                }
                fc.getStructurePositions().add(po.asLong());
                fc.getStructurePositions().add(pb.asLong());
                fc.getStructurePositions().add(pt.asLong());
                fc.casingCount += 3;
            }

            for (int dist = 2; dist <= size; dist++) {
                BlockPos p = mid.relative(side, dist);
                fc.getStructurePositions().add(p.asLong());
                fc.connectorCount++;
            }
        }

        fc.getStructurePositions().add(corePos.asLong());
        fc.getStructurePositions().add(mid.asLong());
        fc.getStructurePositions().add(corePos.above(2).asLong());
        return true;
    }

    private boolean validateInterior(Level level, BlockPos corePos, int size, FusionReactorCache fc) {
        BlockPos mid = corePos.above();
        int shift = size + 2;
        int walkLen = size * 2 + 3;

        for (Direction side : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            Direction walkDir;
            BlockPos start;
            switch (side) {
                case NORTH -> { walkDir = Direction.EAST;  start = mid.relative(Direction.NORTH, shift).relative(Direction.WEST, shift); }
                case SOUTH -> { walkDir = Direction.WEST;  start = mid.relative(Direction.SOUTH, shift).relative(Direction.EAST, shift); }
                case WEST  -> { walkDir = Direction.SOUTH; start = mid.relative(Direction.WEST, shift).relative(Direction.NORTH, shift); }
                default    -> { walkDir = Direction.NORTH; start = mid.relative(Direction.EAST, shift).relative(Direction.SOUTH, shift); }
            }

            for (int i = 0; i < walkLen; i++) {
                BlockPos p = start.relative(walkDir, i);
                if (!level.getBlockState(p).isAir()) {
                    NuclearCraft.LOGGER.debug("[Fusion] interior FAIL: side={} i={}/{} pos={} block={} (expected air)",
                            side, i, walkLen, p, blockName(level, p));
                    return false;
                }
            }
        }
        return true;
    }

    private void collectFunctionalParts(Level level, BlockPos corePos, int size, FusionReactorCache fc) {
        BlockPos mid = corePos;
        int shift = size + 1;
        int wallLen = size * 2 + 3;
        int outerWallLen = wallLen + 2;

        double totalMagField = 0;
        double totalMagEff = 0;
        double totalRfEff = 0;
        int maxMagTemp = Integer.MAX_VALUE;
        int minRfTemp = Integer.MAX_VALUE;

        for (Direction side : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            Direction walkDir;
            BlockPos innerStart, outerStart;

            switch (side) {
                case NORTH -> {
                    walkDir = Direction.EAST;
                    innerStart = mid.relative(Direction.NORTH, shift).relative(Direction.WEST, shift);
                    outerStart = mid.relative(Direction.NORTH, shift + 2).relative(Direction.WEST, shift + 1);
                }
                case SOUTH -> {
                    walkDir = Direction.WEST;
                    innerStart = mid.relative(Direction.SOUTH, shift).relative(Direction.EAST, shift);
                    outerStart = mid.relative(Direction.SOUTH, shift + 2).relative(Direction.EAST, shift + 1);
                }
                case WEST -> {
                    walkDir = Direction.SOUTH;
                    innerStart = mid.relative(Direction.WEST, shift).relative(Direction.NORTH, shift);
                    outerStart = mid.relative(Direction.WEST, shift + 2).relative(Direction.NORTH, shift + 1);
                }
                default -> {
                    walkDir = Direction.NORTH;
                    innerStart = mid.relative(Direction.EAST, shift).relative(Direction.SOUTH, shift);
                    outerStart = mid.relative(Direction.EAST, shift + 2).relative(Direction.SOUTH, shift + 1);
                }
            }

            for (int i = 0; i < wallLen; i++) {
                for (int dy : new int[]{0, 2}) {
                    BlockPos p = innerStart.relative(walkDir, i).relative(Direction.UP, dy);
                    fc.getStructurePositions().add(p.asLong());
                    String bname = blockName(level, p);
                    ElectromagnetDef mag = ElectromagnetDef.get(bname);
                    RFAmplifierDef amp = RFAmplifierDef.get(bname);
                    if (mag != null) {
                        totalMagField += mag.magneticField;
                        totalMagEff += mag.efficiency;
                        fc.magnetsPower += mag.power;
                        if (mag.maxTemp < maxMagTemp) maxMagTemp = mag.maxTemp;
                        fc.magnetCount++;
                    } else if (amp != null) {
                        fc.rfAmplification += amp.voltage;
                        fc.rfAmplifiersPower += amp.power;
                        totalRfEff += amp.efficiency;
                        if (amp.maxTemp < minRfTemp) minRfTemp = amp.maxTemp;
                        fc.amplifierCount++;
                    } else {
                        NuclearCraft.LOGGER.debug("[Fusion] functional (inner) side={} i={} dy={} pos={} block={} not magnet/amplifier",
                                side, i, dy, p, bname);
                    }
                }
            }

            for (int i = 0; i < outerWallLen; i++) {
                for (int dy : new int[]{0, 2}) {
                    BlockPos p = outerStart.relative(walkDir, i).relative(Direction.UP, dy);
                    fc.getStructurePositions().add(p.asLong());
                    String bname = blockName(level, p);
                    ElectromagnetDef mag = ElectromagnetDef.get(bname);
                    RFAmplifierDef amp = RFAmplifierDef.get(bname);
                    if (mag != null) {
                        totalMagField += mag.magneticField;
                        totalMagEff += mag.efficiency;
                        fc.magnetsPower += mag.power;
                        if (mag.maxTemp < maxMagTemp) maxMagTemp = mag.maxTemp;
                        fc.magnetCount++;
                    } else if (amp != null) {
                        fc.rfAmplification += amp.voltage;
                        fc.rfAmplifiersPower += amp.power;
                        totalRfEff += amp.efficiency;
                        if (amp.maxTemp < minRfTemp) minRfTemp = amp.maxTemp;
                        fc.amplifierCount++;
                    } else {
                        NuclearCraft.LOGGER.debug("[Fusion] functional (outer) side={} i={} dy={} pos={} block={} not magnet/amplifier",
                                side, i, dy, p, bname);
                    }
                }
            }
        }

        fc.magneticFieldStrength = totalMagField;
        fc.magnetsEfficiency = fc.magnetCount > 0 ? (int) (totalMagEff / fc.magnetCount) : 0;
        fc.rfEfficiency = fc.amplifierCount > 0 ? (int) (totalRfEff / fc.amplifierCount) : 0;
        fc.maxMagnetsTemp = maxMagTemp == Integer.MAX_VALUE ? 0 : maxMagTemp;
        fc.minRFAmplifiersTemp = minRfTemp == Integer.MAX_VALUE ? 0 : minRfTemp;
    }

    private void addCoreProxies(BlockPos corePos, FusionReactorCache fc) {
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = corePos.offset(x, y, z);
                    fc.getStructurePositions().add(p.asLong());
                }
            }
        }
    }

    private boolean isCasing(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(FusionTags.CASING);
    }

    private String blockName(Level level, BlockPos pos) {
        BlockState bs = level.getBlockState(pos);
        return BuiltInRegistries.BLOCK.getKey(bs.getBlock()).getPath();
    }

    private static Block blockOf(String name) {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.block() == null) return Blocks.AIR;
        Block b = entry.block().get();
        return b == null ? Blocks.AIR : b;
    }
}
