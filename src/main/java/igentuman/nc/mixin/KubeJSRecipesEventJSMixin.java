package igentuman.nc.mixin;

import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import igentuman.nc.recipes.NcRecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipesEventJS.class)
public class KubeJSRecipesEventJSMixin {

    @Inject(method = "post", at = @At("TAIL"), cancellable = false, remap = false)
    private void post(CallbackInfo callback) {
        NcRecipeType.invalidateCache();
    }
}
