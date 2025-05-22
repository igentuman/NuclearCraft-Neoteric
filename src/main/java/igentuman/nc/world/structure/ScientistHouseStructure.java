package igentuman.nc.world.structure;

import com.mojang.datafixers.util.Pair;
import igentuman.nc.mixin.TemplatePoolAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.NcUtils.rlFromString;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScientistHouseStructure {

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent ev) {
        if (ev.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            for(String biome : new String[]{"plains", "snowy", "savanna", "desert", "taiga"}) {
                addToPool(rlFromString("minecraft:village/" + biome + "/houses"), rl("scientist_house/" + biome), ev.getRegistryAccess());
            }
        }
    }

    private static void addToPool(ResourceLocation poolId, ResourceLocation toAdd, RegistryAccess regAccess) {
        Registry<StructureTemplatePool> registry = regAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        StructureTemplatePool pool = (StructureTemplatePool) Objects.requireNonNull((StructureTemplatePool)registry.get(poolId), poolId.getPath());
        TemplatePoolAccess poolAccess = (TemplatePoolAccess)pool;
        if (!(poolAccess.getRawTemplates() instanceof ArrayList)) {
            poolAccess.setRawTemplates(new ArrayList(poolAccess.getRawTemplates()));
        }

        SinglePoolElement addedElement = (SinglePoolElement)SinglePoolElement.single(toAdd.toString()).apply(StructureTemplatePool.Projection.RIGID);
        poolAccess.getRawTemplates().add(Pair.of(addedElement, 1));
        poolAccess.getTemplates().add(addedElement);
    }
}