package igentuman.nc.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * Fixes ConcurrentModificationException on /reload: FluidTags.getWrappers()
     * returns the live TagRegistry list, which the reload thread mutates while
     * the tick thread iterates it in updateFluidOnEyes. Iterate a snapshot instead.
     */
    @Redirect(
            method = "updateFluidOnEyes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tags/FluidTags;getWrappers()Ljava/util/List;"
            )
    )
    private List<?> nc$snapshotFluidWrappers() {
        List<?> live = FluidTags.getWrappers();
        for (int i = 0; i < 8; i++) {
            try {
                return new ArrayList<>(live);
            } catch (ConcurrentModificationException ignored) {
            }
        }
        return Collections.emptyList();
    }
}
