package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.block.accelerator.CoolerBlock;
import igentuman.nc.block.fusion.ElectromagnetBlock;
import igentuman.nc.block.fusion.ElectromagnetSlopeBlock;
import igentuman.nc.block.fusion.RFAmplifierBlock;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.api.multiblock.part.ElectromagnetDef;
import igentuman.nc.api.multiblock.part.RFAmplifierDef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class AcceleratorComponents {

    public static final int MINIMUM_RING_OUTER_SIDE = 11;
    public static final int RING_WALL_OFFSET = 4;
    public static final int CROSS_SECTION = 5;

    private AcceleratorComponents() {
    }

    @Nullable
    public static ElectromagnetDef electromagnet(BlockState state) {
        if (!(state.getBlock() instanceof ElectromagnetBlock)
                || state.getBlock() instanceof ElectromagnetSlopeBlock) return null;
        return ElectromagnetDef.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    @Nullable
    public static RFAmplifierDef rfAmplifier(BlockState state) {
        if (!(state.getBlock() instanceof RFAmplifierBlock)) return null;
        return RFAmplifierDef.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    @Nullable
    public static CoolerDef cooler(BlockState state) {
        return state.getBlock() instanceof CoolerBlock cooler ? cooler.definition() : null;
    }

    public static int[] linearSizeRange() {
        return switch (Multiblocks.acceleratorScalePreset) {
            case 2 -> new int[]{60, 1_000};
            case 3 -> new int[]{600, 10_000};
            default -> new int[]{6, 100};
        };
    }

    public static int[] ringSizeRange() {
        int[] range = linearSizeRange();
        return new int[]{Math.max(range[0], MINIMUM_RING_OUTER_SIDE), range[1]};
    }
}
