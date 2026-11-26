package igentuman.nc.api.multiblock;

import net.minecraft.core.Vec3i;

/** Inclusive ranges for the outer dimensions of a multiblock. */
public record StructureSizeRange(Vec3i minimum, Vec3i maximum) {
    public static StructureSizeRange cube(int minimum, int maximum) {
        return new StructureSizeRange(new Vec3i(minimum, minimum, minimum), new Vec3i(maximum, maximum, maximum));
    }
}
