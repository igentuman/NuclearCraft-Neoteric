package igentuman.nc.world.structure;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.LOGGER;
import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.handler.config.WorldConfig.VILLAGE_CONFIG;
import static igentuman.nc.util.NcUtils.rlFromString;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScientistHouseStructure {

    private static Field rawTemplatesField;
    private static Field templatesField;

    static {
        try {
            rawTemplatesField = ObfuscationReflectionHelper.findField(StructureTemplatePool.class, "f_210559_");
            templatesField = ObfuscationReflectionHelper.findField(StructureTemplatePool.class, "f_210560_");
        } catch (Exception e) {
            LOGGER.error("Failed to find StructureTemplatePool fields for scientist house injection: {}", e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent ev) {
        if(!VILLAGE_CONFIG.generateScientistHouse.get()) return;
        if (ev.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            for(String biome : new String[]{"plains", "snowy", "savanna", "desert", "taiga"}) {
                addToPool(rlFromString("minecraft:village/" + biome + "/houses"), rl("scientist_house/" + biome), ev.getRegistryAccess());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void addToPool(ResourceLocation poolId, ResourceLocation toAdd, RegistryAccess regAccess) {
        if (rawTemplatesField == null || templatesField == null) return;
        try {
            Registry<StructureTemplatePool> registry = regAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
            StructureTemplatePool pool = Objects.requireNonNull(registry.get(poolId), poolId.getPath());

            List<Pair<StructurePoolElement, Integer>> rawTemplates = (List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool);
            ObjectArrayList<StructurePoolElement> templates = (ObjectArrayList<StructurePoolElement>) templatesField.get(pool);

            if (!(rawTemplates instanceof ArrayList)) {
                rawTemplates = new ArrayList<>(rawTemplates);
                rawTemplatesField.set(pool, rawTemplates);
            }

            SinglePoolElement addedElement = (SinglePoolElement) SinglePoolElement.single(toAdd.toString()).apply(StructureTemplatePool.Projection.RIGID);
            rawTemplates.add(Pair.of(addedElement, 1));
            templates.add(addedElement);
        } catch (Exception e) {
            LOGGER.error("Failed to add {} to village pool {}: {}", toAdd, poolId, e.getMessage());
        }
    }
}