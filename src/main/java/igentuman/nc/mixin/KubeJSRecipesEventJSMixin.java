package igentuman.nc.mixin;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.kubejs.NCKubeJsEvents;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipesEventJS.class)
public class KubeJSRecipesEventJSMixin {
    //this doesn't work for some reason
    @Inject(method = "post", at = @At("HEAD"), cancellable = false, remap = false)
    private void post(RecipeManager recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo callback) {


        NuclearCraft.LOGGER.warn("kubejs post injected");
    }
}
