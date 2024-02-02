package igentuman.nc.client.sound;

import igentuman.nc.registry.SoundEventRegistryObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundHandler {

    private SoundHandler() {
    }
    private static final Long2ObjectMap<SoundInstance> soundMap = new Long2ObjectOpenHashMap<>();
    private static SoundEngine soundEngine;
    private static boolean hadPlayerSounds;

    public static void clearPlayerSounds() {

    }

    public static void clearPlayerSounds(UUID uuid) {

    }

    private static void startSound(LevelAccessor world, UUID uuid, Map<UUID, PlayerSound> knownSounds, Function<Player, PlayerSound> soundCreator) {
        if (knownSounds.containsKey(uuid)) {
            if (playerSoundsEnabled()) {
                //Check if it needs to be restarted
                restartSounds(knownSounds.get(uuid));
            }
        } else {
            Player player = world.getPlayerByUUID(uuid);
            if (player != null) {
                PlayerSound sound = soundCreator.apply(player);
                playSound(sound);
                knownSounds.put(uuid, sound);
            }
        }
    }

    @SafeVarargs
    private static void startSounds(LevelAccessor world, UUID uuid, Map<UUID, PlayerSound[]> knownSounds, Function<Player, PlayerSound>... soundCreators) {
        if (knownSounds.containsKey(uuid)) {
            if (playerSoundsEnabled()) {
                restartSounds(knownSounds.get(uuid));
            }
        } else {
            Player player = world.getPlayerByUUID(uuid);
            if (player != null) {
                PlayerSound[] sounds = new PlayerSound[soundCreators.length];
                for (int i = 0; i < soundCreators.length; i++) {
                    playSound(sounds[i] = soundCreators[i].apply(player));
                }
                knownSounds.put(uuid, sounds);
            }
        }
    }

    private static void restartSounds(PlayerSound... sounds) {
        for (PlayerSound sound : sounds) {
            if (!sound.isStopped() && soundEngine != null) {
                playSound(sound);
            }
        }
    }

    public static void restartSounds() {
        boolean hasPlayerSounds = playerSoundsEnabled();
        if (hasPlayerSounds != hadPlayerSounds) {
            hadPlayerSounds = hasPlayerSounds;
            if (hasPlayerSounds) {

            }
        }
    }



    private static boolean playerSoundsEnabled() {
        return getVolume(SoundSource.MASTER) > 0 && getVolume(SoundSource.PLAYERS) > 0;
    }

    private static float getVolume(SoundSource category) {
        return Minecraft.getInstance().options.getSoundSourceVolume(category);
    }

    public static void playSound(SoundEventRegistryObject<?> soundEventRO) {
        playSound(soundEventRO.get());
    }

    public static void playSound(SoundEvent sound) {
        playSound(SimpleSoundInstance.forUI(sound, 1, 1));
    }

    public static void playSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public static void stopSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().stop(sound);
    }

    public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos) {
        SoundInstance s = soundMap.get(pos.asLong());
        if (s == null || !Minecraft.getInstance().getSoundManager().isActive(s)) {
            s = new TileTickableSound(soundEvent, category, random, pos, volume);

            if (!isClientPlayerInRange(s)) {
                return null;
            }

            playSound(s);
            s = soundMap.get(pos.asLong());
        }
        return s;
    }

    public static void stopTileSound(BlockPos pos) {
        long posKey = pos.asLong();
        SoundInstance s = soundMap.get(posKey);
        if (s != null) {
            Minecraft.getInstance().getSoundManager().stop(s);
            soundMap.remove(posKey);
        }
    }

    private static boolean isClientPlayerInRange(SoundInstance sound) {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) {
            return true;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        Sound s = sound.getSound();
        if (s == null) {
            sound.resolve(Minecraft.getInstance().getSoundManager());
            s = sound.getSound();
        }
        int attenuationDistance = s.getAttenuationDistance();
        float scaledDistance = Math.max(sound.getVolume(), 1) * attenuationDistance;
        return player.position().distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) < scaledDistance * scaledDistance;
    }

    @SubscribeEvent
    public static void onSoundEngineSetup(SoundEngineLoadEvent event) {
        if (soundEngine == null) {
            soundEngine = event.getEngine();
        }
    }

    public static void onTilePlaySound(PlaySoundEvent event) {
        SoundInstance resultSound = event.getSound();
        ResourceLocation soundLoc = event.getOriginalSound().getLocation();
        if (!soundLoc.getNamespace().startsWith(MODID)) {
            return;
        }
        if (event.getOriginalSound() instanceof PlayerSound sound) {
            event.setSound(sound);
            return;
        }

        if (event.getName().startsWith("tile.") && resultSound != null) {
            BlockPos pos = new BlockPos((int) (resultSound.getX() - 0.5), (int) (resultSound.getY() - 0.5), (int) (resultSound.getZ() - 0.5));
            soundMap.put(pos.asLong(), resultSound);
        }
    }

    private static class TileTickableSound extends AbstractTickableSoundInstance {

        private final float originalVolume;
        private final int checkInterval = 20 + ThreadLocalRandom.current().nextInt(20);

        TileTickableSound(SoundEvent soundEvent, SoundSource category, RandomSource random, BlockPos pos, float volume) {
            super(soundEvent, category, random);
            this.originalVolume = volume * 1;
            this.x = pos.getX() + 0.5F;
            this.y = pos.getY() + 0.5F;
            this.z = pos.getZ() + 0.5F;
            this.volume = this.originalVolume;
            this.looping = true;
            this.delay = 0;
        }

        @Override
        @SuppressWarnings("UnstableApiUsage")
        public void tick() {
            if (Minecraft.getInstance().level.getGameTime() % checkInterval == 0) {
                if (!isClientPlayerInRange(this)) {
                    //If the player is not in range of hearing this sound anymore; go ahead and shutdown
                    stop();
                    return;
                }
                volume = originalVolume;
                SoundInstance s = ForgeHooksClient.playSound(soundEngine, this);

                if (s == this) {
                    volume = originalVolume * 1;
                } else if (s == null) {
                    stop();
                } else {
                    volume = s.getVolume() * 1;
                }
            }
        }


        @Override
        public float getVolume() {
            if (this.sound == null) {
                this.resolve(Minecraft.getInstance().getSoundManager());
            }
            return super.getVolume();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}