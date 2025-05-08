package igentuman.nc.setup.registration;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.content.processors.Processors.MANUFACTORY;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;

public class Villager {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MODID);

    public static final RegistryObject<PoiType> MANUFACTORY_POI = POI_TYPES.register("manufactory_poi",
            () -> new PoiType(ImmutableSet.copyOf(PROCESSORS.get(MANUFACTORY).get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final RegistryObject<VillagerProfession> NUCLEAR_SCIENTIST =
            VILLAGER_PROFESSIONS.register("nuclear_scientist", () -> new VillagerProfession("nuclear_scientist",
                    holder -> holder.get() == MANUFACTORY_POI.get(), holder -> holder.get() == MANUFACTORY_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        POI_TYPES.register(bus);
        VILLAGER_PROFESSIONS.register(bus);
    }
}