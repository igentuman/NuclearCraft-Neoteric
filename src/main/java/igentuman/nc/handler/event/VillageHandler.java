package igentuman.nc.handler.event;

import com.mojang.datafixers.util.Pair;
import igentuman.nc.config.Common;
import igentuman.nc.mixin.StructureTemplatePoolAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

@EventBusSubscriber(modid = MODID)
public class VillageHandler {

    private static final String[] VILLAGE_TYPES = {"plains", "snowy", "savanna", "desert", "taiga"};
    private static final int SCIENTIST_HOUSE_WEIGHT = 4;
    private static final Set<StructureTemplatePool> MODIFIED_POOLS = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent.ServerDataLoad event) {
        if (!Common.SCIENTIST_HOUSE.get()) return;
        HolderLookup.RegistryLookup<StructureTemplatePool> pools = event.getRegistries().lookupOrThrow(Registries.TEMPLATE_POOL);
        for (String type : VILLAGE_TYPES) {
            pools.get(ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("village/" + type + "/houses")))
                    .ifPresent(pool -> addToPool(pool.value(), rl("scientist_house/" + type)));
        }
    }

    private static void addToPool(StructureTemplatePool pool, Identifier template) {
        if (!MODIFIED_POOLS.add(pool)) return;
        StructureTemplatePoolAccessor accessor = (StructureTemplatePoolAccessor) pool;
        StructurePoolElement element = StructurePoolElement.single(template.toString())
                .apply(StructureTemplatePool.Projection.RIGID);
        List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(accessor.nc$getRawTemplates());
        rawTemplates.add(Pair.of(element, SCIENTIST_HOUSE_WEIGHT));
        accessor.nc$setRawTemplates(rawTemplates);
        for (int i = 0; i < SCIENTIST_HOUSE_WEIGHT; i++) {
            accessor.nc$getTemplates().add(element);
        }
    }
}
