package igentuman.nc.multiblock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class StructuralBlockState {
    private static final Set<String> RUNTIME_PROPERTIES = Set.of("powered", "power", "lit", "active", "hidden");
    private static final Map<BlockState, BlockState> SIGNATURES = new ConcurrentHashMap<>();

    private StructuralBlockState() {}

    public static BlockState signature(BlockState state) {
        return SIGNATURES.computeIfAbsent(state, StructuralBlockState::canonicalize);
    }

    public static boolean equivalent(BlockState first, BlockState second) {
        return first == second || signature(first) == signature(second);
    }

    private static BlockState canonicalize(BlockState state) {
        BlockState defaults = state.getBlock().defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            if (RUNTIME_PROPERTIES.contains(property.getName())) state = reset(state, defaults, property);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState reset(BlockState state, BlockState defaults, Property<T> property) {
        return state.setValue(property, defaults.getValue(property));
    }
}
