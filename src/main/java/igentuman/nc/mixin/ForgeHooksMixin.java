package igentuman.nc.mixin;

import igentuman.nc.content.materials.Ores;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;

@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

    @Inject(method = "filterThunks", at = @At("TAIL"), remap = false, cancellable = true)
    private static void filterThunks(Map<ResourceKey<?>, RegistryResourceAccess.EntryThunk<?>> map, CallbackInfoReturnable<Collection<Map.Entry<ResourceKey<?>, RegistryResourceAccess.EntryThunk<?>>>> cir) {
        Map<ResourceKey<?>, RegistryResourceAccess.EntryThunk<?>> map1 = new HashMap<>();
        for(Map.Entry<ResourceKey<?>, RegistryResourceAccess.EntryThunk<?>> entry : cir.getReturnValue()) {
            String name = entry.getKey().location().getPath();
            if(!entry.getKey().location().getNamespace().equals(MODID) && !name.contains("nc_ores_"))  {
                map1.put(entry.getKey(), entry.getValue());
                continue;
            }
            if(name.contains("nc_ores_")) {
                if(Ores.isRegistered(name.replaceAll("nc_ores_|_deepslate|_nether|_end", ""))) {
                    map1.put(entry.getKey(), entry.getValue());
                }
            }
        }
        cir.setReturnValue(map1.entrySet());
    }
}
