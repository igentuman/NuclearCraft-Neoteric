package igentuman.nc.multiblock.validation;

import net.minecraft.core.BlockPos;

public final class UnloadedStructureException extends RuntimeException {
    private final BlockPos position;

    public UnloadedStructureException(BlockPos position) {
        super("Structure chunk is unloaded", null, false, false);
        this.position = position.immutable();
    }

    public BlockPos position() { return position; }
}
