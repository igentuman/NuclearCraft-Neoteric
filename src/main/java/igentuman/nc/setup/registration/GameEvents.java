package igentuman.nc.setup.registration;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

public class GameEvents {
    public static final DeferredRegister<GameEvent> GAME_EVENTS =
            DeferredRegister.create(Registries.GAME_EVENT, MODID);

    public static final RegistryObject<GameEvent> BLACKHOLE_VIBRATION =
            GAME_EVENTS.register("blackhole_vibration", () -> new GameEvent("blackhole_vibration", 32));

    public static void init()
    {
        GAME_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void commonSetup() {
        if (VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT instanceof Object2IntOpenHashMap<GameEvent> frequencyForEvent) {
            frequencyForEvent.put(BLACKHOLE_VIBRATION.get(), 15);
        }
    }
}
