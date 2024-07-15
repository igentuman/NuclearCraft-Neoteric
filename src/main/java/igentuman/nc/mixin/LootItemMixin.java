package igentuman.nc.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootItem.Serializer.class)
public class LootItemMixin {

    @Inject(method = "deserialize*", at = @At("HEAD"), cancellable = true, remap = false)
    private void deserialize(CallbackInfoReturnable<LootItem> cir, JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
        if(pObject.has("lithium")) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
