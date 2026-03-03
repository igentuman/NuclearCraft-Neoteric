package igentuman.nc.setup.registration;

import igentuman.api.platform.NCVibrations;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static igentuman.nc.NuclearCraft.MODID;

public class GameEvents {
    public static final DeferredRegister<GameEvent> GAME_EVENTS =
            DeferredRegister.create(Registries.GAME_EVENT, MODID);

    public static final DeferredHolder<GameEvent, GameEvent> BLACKHOLE_VIBRATION =
            GAME_EVENTS.register("blackhole_vibration", () -> new GameEvent(32));

    public static void init(IEventBus bus) {
        GAME_EVENTS.register(bus);
    }

    public static void commonSetup() {
        NCVibrations.registerFrequency(BLACKHOLE_VIBRATION, 15);
    }
}
