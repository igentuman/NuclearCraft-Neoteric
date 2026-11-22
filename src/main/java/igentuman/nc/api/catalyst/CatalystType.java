package igentuman.nc.api.catalyst;

import net.minecraft.resources.Identifier;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public record CatalystType(Identifier id) {
    public static final CatalystType ENERGY = builtin("energy");
    public static final CatalystType SPEED = builtin("speed");
    public static final CatalystType EFFICIENCY = builtin("efficiency");
    public static final CatalystType ORE_SOURCE = builtin("ore_source");

    public CatalystType {
        Objects.requireNonNull(id, "id");
    }

    private static CatalystType builtin(String path) {
        return new CatalystType(rl(path));
    }
}
