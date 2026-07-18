package igentuman.nc.setup;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public final class NCJukeboxSongs {
    private NCJukeboxSongs() {
    }

    public record RecordDef(String name, DeferredHolder<SoundEvent, SoundEvent> sound, int lengthSeconds, int comparatorOutput) {
        public ResourceKey<JukeboxSong> key() {
            return ResourceKey.create(Registries.JUKEBOX_SONG, rl(name));
        }
    }

    public static final Map<String, RecordDef> RECORDS = new LinkedHashMap<>();

    private static void add(String name, DeferredHolder<SoundEvent, SoundEvent> sound, int lengthSeconds) {
        RECORDS.put(name, new RecordDef(name, sound, lengthSeconds, 15));
    }

    static {
        add("end_of_the_world", NCSounds.RECORD_END_OF_THE_WORLD, 180);
        add("hyperspace", NCSounds.RECORD_HYPERSPACE, 185);
        add("money_for_nothing", NCSounds.RECORD_MONEY_FOR_NOTHING, 315);
        add("wanderer", NCSounds.RECORD_WANDERER, 167);
    }

    public static ResourceKey<JukeboxSong> key(String name) {
        return RECORDS.get(name).key();
    }

    public static void bootstrap(BootstrapContext<JukeboxSong> ctx) {
        HolderGetter<SoundEvent> sounds = ctx.lookup(Registries.SOUND_EVENT);
        for (RecordDef def : RECORDS.values()) {
            ctx.register(def.key(), new JukeboxSong(
                    sounds.getOrThrow(def.sound().getKey()),
                    Component.translatable("jukebox_song." + MODID + "." + def.name()),
                    (float) def.lengthSeconds(),
                    def.comparatorOutput()
            ));
        }
    }
}
