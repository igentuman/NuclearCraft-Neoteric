package igentuman.nc.setup;

import com.google.common.collect.ImmutableSet;
import igentuman.nc.setup.entries.Processors;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.setup.Registers.POI_TYPES;
import static igentuman.nc.setup.Registers.VILLAGER_PROFESSIONS;

public final class NCVillagers {

    private NCVillagers() {
    }

    public static final DeferredHolder<PoiType, PoiType> ANALYZER_POI = POI_TYPES.register("analyzer_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModEntries.get(Processors.ANALYZER).block().get()
                    .getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> NUCLEAR_SCIENTIST =
            VILLAGER_PROFESSIONS.register("nuclear_scientist", () -> new VillagerProfession("nuclear_scientist",
                    holder -> holder.is(ANALYZER_POI.getKey()), holder -> holder.is(ANALYZER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void init() {
    }
}
