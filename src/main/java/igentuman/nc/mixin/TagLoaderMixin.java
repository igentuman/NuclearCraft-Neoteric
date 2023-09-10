package igentuman.nc.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Inject(method = "load", at = @At("TAIL"), cancellable = true, remap = false)
    private void load(CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> callback) {
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = callback.getReturnValue();

        map = fixMissingFor(new ResourceLocation("mineable/pickaxe"), map);
        map = fixMissingFor(new ResourceLocation("needs_iron_tool"), map);

        callback.setReturnValue(map);
    }

    private static Map<ResourceLocation, List<TagLoader.EntryWithSource>> fixMissingFor(ResourceLocation type, Map<ResourceLocation, List<TagLoader.EntryWithSource>> map) {
        if(!map.containsKey(type)) {
            return map;
        }
        List<TagLoader.EntryWithSource> list = new ArrayList<>();
        for(TagLoader.EntryWithSource entry : map.get(type)) {
            if(!ForgeRegistries.BLOCKS.getValue(entry.entry().getId()).equals(Blocks.AIR)) {
                list.add(entry);
            }
        }
        map.replace(type, list);
        return map;
    }
}
