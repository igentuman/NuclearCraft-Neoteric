package igentuman.nc.compat.computer;

import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ComputerApi<T extends BlockEntity> {

    protected final T be;
    private final ComputerErrors errors;

    protected ComputerApi(T be, ComputerErrors errors) {
        this.be = be;
        this.errors = errors;
    }

    public abstract String typeName();

    public final BlockEntity blockEntity() {
        return be;
    }

    protected final RuntimeException error(String message) {
        return errors.create(message);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other.getClass() == getClass() && ((ComputerApi<?>) other).be == be;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(be) * 31 + getClass().hashCode();
    }
}
