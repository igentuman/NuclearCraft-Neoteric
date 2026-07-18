package igentuman.nc.setup.registration;

import net.minecraft.core.Registry;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

public class GameEvents {
    public static final DeferredRegister<GameEvent> GAME_EVENTS =
            DeferredRegister.create(Registry.GAME_EVENT_REGISTRY, MODID);

    public static final RegistryObject<GameEvent> BLACKHOLE_VIBRATION =
            GAME_EVENTS.register("blackhole_vibration", () -> new GameEvent("blackhole_vibration", 32));

    public static void init(FMLJavaModLoadingContext context)
    {
        GAME_EVENTS.register(context.getModEventBus());
    }

    public static void commonSetup() {
    }
}
