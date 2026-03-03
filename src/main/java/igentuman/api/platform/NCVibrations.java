package igentuman.api.platform;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Platform wrapper for VibrationSystem APIs.
 * In 1.21.1: VIBRATION_FREQUENCY_FOR_EVENT is Reference2IntOpenHashMap&lt;ResourceKey&lt;GameEvent&gt;&gt;
 * (was Object2IntOpenHashMap&lt;GameEvent&gt; in Forge).
 */
public final class NCVibrations {
    private NCVibrations() {}

    @SuppressWarnings("unchecked")
    public static void registerFrequency(DeferredHolder<GameEvent, GameEvent> event, int frequency) {
        if (VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT instanceof Reference2IntOpenHashMap<?> map) {
            ((Reference2IntOpenHashMap<ResourceKey<GameEvent>>) map).put(event.getKey(), frequency);
        }
    }
}
