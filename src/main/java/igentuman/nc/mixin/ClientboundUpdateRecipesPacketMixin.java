package igentuman.nc.mixin;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static igentuman.nc.NuclearCraft.MODID;

@Mixin(ClientboundUpdateRecipesPacket.class)
public class ClientboundUpdateRecipesPacketMixin {

    @Inject(method = "fromNetwork", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fromNetwork(FriendlyByteBuf buffer, CallbackInfoReturnable<Recipe<?>> callback) {
        ResourceLocation recipeType = buffer.readResourceLocation();
        ResourceLocation recipeLoc = buffer.readResourceLocation();
        if(!recipeLoc.getPath().contains(recipeType.getPath()) && recipeType.getNamespace().equals(MODID)) {
            recipeType = new ResourceLocation(recipeType.getNamespace(), recipeLoc.getPath().split("/")[0]);
        }
        ResourceLocation finalRecipeType = recipeType;
        Recipe<?> recipe = Registry.RECIPE_SERIALIZER.getOptional(recipeType).orElseThrow(() -> {
            return new IllegalArgumentException("Unknown recipe serializer " + finalRecipeType);
        }).fromNetwork(recipeLoc, buffer);
       callback.setReturnValue(recipe);
    }

    @Inject(method = "toNetwork", at = @At("HEAD"), cancellable = true, remap = false)
    private static <T extends Recipe<?>> void  toNetwork(FriendlyByteBuf buffer, T recipe, CallbackInfo ci) {
        buffer.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer()));
        buffer.writeResourceLocation(recipe.getId());
        ((net.minecraft.world.item.crafting.RecipeSerializer<T>)recipe.getSerializer()).toNetwork(buffer, recipe);
    }
}
