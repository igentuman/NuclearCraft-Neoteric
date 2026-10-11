package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.compat.computer.ComputerErrors;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface NcPeripheral extends IPeripheral {

    ComputerErrors LUA_ERRORS = message -> raise(new LuaException(message));

    String typeName();

    BlockEntity blockEntity();

    @Override
    default String getType() {
        return typeName();
    }

    @Override
    default Object getTarget() {
        return blockEntity();
    }

    @Override
    default boolean equals(@Nullable IPeripheral other) {
        return equals((Object) other);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException raise(Throwable error) throws E {
        throw (E) error;
    }
}
