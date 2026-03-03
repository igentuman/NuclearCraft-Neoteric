package igentuman.api.platform;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Platform wrapper for BlockBehaviour.Properties factory methods.
 * In 1.21.1 NeoForge: Properties.copy() → Properties.ofFullCopy()
 */
public final class NCBlockProperties {
    private NCBlockProperties() {}

    public static BlockBehaviour.Properties copy(BlockBehaviour block) {
        return BlockBehaviour.Properties.ofFullCopy(block);
    }
}
