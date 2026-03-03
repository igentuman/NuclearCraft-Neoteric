package igentuman.api.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/**
 * Platform translation layer for music disc items.
 * <p>
 * MC 1.21 deleted {@code RecordItem}. Music discs are now plain {@code Item}s
 * with the {@code JukeboxPlayable} data component. Songs are data-driven via
 * {@code Registries.JUKEBOX_SONG}.
 */
public final class NCMusicDiscs {

    private NCMusicDiscs() {}

    // ---- JukeboxSong resource keys ----

    public static final ResourceKey<JukeboxSong> WANDERER =
            createKey("wanderer");
    public static final ResourceKey<JukeboxSong> END_OF_THE_WORLD =
            createKey("end_of_the_world");
    public static final ResourceKey<JukeboxSong> MONEY_FOR_NOTHING =
            createKey("money_for_nothing");
    public static final ResourceKey<JukeboxSong> HYPERSPACE =
            createKey("hyperspace");

    private static ResourceKey<JukeboxSong> createKey(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, rl(name));
    }

    // ---- Tag helpers ----

    /**
     * ItemTags.MUSIC_DISCS was removed in 1.21.1 — discs are identified by
     * the JukeboxPlayable data component instead. This returns a custom tag
     * for NC's own music disc items so datagen can still group them.
     */
    public static TagKey<Item> musicDiscsTag() {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "music_discs"));
    }

    // ---- Item factory ----

    /**
     * Creates a music disc item linked to the given jukebox song.
     * Replaces {@code new MusicDiscItem(analogOutput, sound, props, length)}.
     */
    public static Item createDisc(ResourceKey<JukeboxSong> songKey) {
        return new Item(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .jukeboxPlayable(songKey));
    }

    // ---- Datapack bootstrap ----

    /**
     * Bootstrap-registers NC's jukebox songs into the data-driven registry.
     * Wire this into your {@code RegistrySetBuilder}:
     * {@code .add(Registries.JUKEBOX_SONG, NCMusicDiscs::bootstrap)}
     */
    public static void bootstrap(BootstrapContext<JukeboxSong> context) {
        HolderGetter<SoundEvent> sounds = context.lookup(Registries.SOUND_EVENT);

        register(context, sounds, WANDERER, "music.wanderer", 3600, 15);
        register(context, sounds, END_OF_THE_WORLD, "music.end_of_the_world", 3600, 15);
        register(context, sounds, MONEY_FOR_NOTHING, "music.money_for_nothing", 3600, 15);
        register(context, sounds, HYPERSPACE, "music.hyperspace", 3600, 15);
    }

    private static void register(
            BootstrapContext<JukeboxSong> context,
            HolderGetter<SoundEvent> sounds,
            ResourceKey<JukeboxSong> key,
            String soundId,
            int lengthInSeconds,
            int comparatorOutput
    ) {
        ResourceKey<SoundEvent> soundKey = ResourceKey.create(Registries.SOUND_EVENT, rl(soundId));
        Holder.Reference<SoundEvent> soundHolder = sounds.getOrThrow(soundKey);

        context.register(key, new JukeboxSong(
                soundHolder,
                Component.translatable("item." + MODID + "." + key.location().getPath()),
                (float) lengthInSeconds,
                comparatorOutput
        ));
    }
}
