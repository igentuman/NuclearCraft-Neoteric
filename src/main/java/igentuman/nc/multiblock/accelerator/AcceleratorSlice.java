package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.api.multiblock.part.ElectromagnetDef;
import igentuman.nc.api.multiblock.part.RFAmplifierDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class AcceleratorSlice {

    public static final int[][] OFFSETS = {
            {2, 1}, {2, 3}, {1, 2}, {3, 2}, {3, 3}, {1, 1}, {1, 3}, {3, 1}
    };

    public enum Kind { MAGNET, AMPLIFIER, FILLER }

    public record Component(Kind kind, String tier, double value) {
    }

    private AcceleratorSlice() {
    }

    public static int index(int right, int up) {
        for (int i = 0; i < OFFSETS.length; i++) {
            if (OFFSETS[i][0] == right && OFFSETS[i][1] == up) return i;
        }
        return -1;
    }

    @Nullable
    public static Component classify(AcceleratorCache cache, BlockPos pos, BlockState state, Block yoke,
                                     Map<BlockPos, CoolerDef> coolers) {
        if (state.isAir()) return new Component(Kind.FILLER, "air", 0);
        ElectromagnetDef magnet = AcceleratorComponents.electromagnet(state);
        if (magnet != null) {
            cache.addEnergyPerTick(magnet.power);
            cache.addHeatPerTick(magnet.heat);
            cache.includeMaximumTemperatureK(magnet.maxTemp / 1_000L);
            cache.addComponentEfficiency(magnet.efficiency / 100D);
            return new Component(Kind.MAGNET, magnet.name, magnet.magneticField);
        }
        RFAmplifierDef amplifier = AcceleratorComponents.rfAmplifier(state);
        if (amplifier != null) {
            cache.addEnergyPerTick(amplifier.power);
            cache.addHeatPerTick(amplifier.heat);
            cache.includeMaximumTemperatureK(amplifier.maxTemp / 1_000L);
            cache.addComponentEfficiency(amplifier.efficiency / 100D);
            return new Component(Kind.AMPLIFIER, amplifier.name, amplifier.voltage);
        }
        CoolerDef cooler = AcceleratorComponents.cooler(state);
        if (cooler != null) {
            coolers.put(pos.immutable(), cooler);
            return new Component(Kind.FILLER, cooler.id().toString(), 0);
        }
        return state.is(yoke) ? new Component(Kind.FILLER, "yoke", 0) : null;
    }

    @Nullable
    public static String validate(AcceleratorCache cache, Component[] slice) {
        int magnets = 0;
        int amplifiers = 0;
        double amplifierSum = 0;
        for (Component component : slice) {
            if (component.kind() == Kind.MAGNET) magnets++;
            else if (component.kind() == Kind.AMPLIFIER) amplifiers++;
            amplifierSum += component.value();
        }
        if (magnets > 0 && amplifiers > 0) return "mixed_optics";
        if (amplifiers > 0) {
            if (amplifiers != slice.length) return "incomplete_rf_slice";
            cache.addVoltage(Math.round(amplifierSum / slice.length));
            return null;
        }
        if (magnets == 0) return null;
        boolean verticalPair = isMagnet(slice, 0) && isMagnet(slice, 1);
        boolean horizontalPair = isMagnet(slice, 2) && isMagnet(slice, 3);
        boolean diagonalMagnet = isMagnet(slice, 4) || isMagnet(slice, 5)
                || isMagnet(slice, 6) || isMagnet(slice, 7);
        if (magnets == 4 && verticalPair && horizontalPair && !diagonalMagnet) {
            cache.addQuadrupoleField((slice[0].value() + slice[1].value() + slice[2].value() + slice[3].value()) / 4D);
            return null;
        }
        if (magnets == 2 && !diagonalMagnet && (verticalPair ^ horizontalPair)) {
            Component first = verticalPair ? slice[0] : slice[2];
            Component second = verticalPair ? slice[1] : slice[3];
            if (!first.tier().equals(second.tier())) return "mixed_dipole_tier";
            cache.addDipoleField((first.value() + second.value()) / 2D);
            return null;
        }
        return "invalid_magnet_slice";
    }

    public static void applyCoolers(AcceleratorCache cache, Map<BlockPos, CoolerDef> coolers,
                                    Predicate<BlockPos> inside) {
        for (Map.Entry<BlockPos, CoolerDef> entry : coolers.entrySet()) {
            List<BlockState> neighbors = new ArrayList<>(6);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = entry.getKey().relative(direction);
                if (!inside.test(neighbor)) continue;
                neighbors.add(cache.getBlockState(neighbor));
            }
            if (entry.getValue().satisfiesPlacementRules(neighbors)) {
                cache.addCoolingPerTick(entry.getValue().coolingPerTick());
            }
        }
    }

    private static boolean isMagnet(Component[] slice, int index) {
        return slice[index].kind() == Kind.MAGNET;
    }
}
