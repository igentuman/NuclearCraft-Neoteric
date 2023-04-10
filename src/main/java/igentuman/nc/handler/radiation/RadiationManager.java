package igentuman.nc.handler.radiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.network.toClient.PacketRadiationData;
import igentuman.nc.handler.radiation.capability.IRadiationEntity;
import igentuman.nc.capability.Capabilities;
import igentuman.nc.setup.registration.NcDamageSource;
import igentuman.nc.setup.registration.NcParticleTypes;
import igentuman.nc.util.NBTConstants;
import igentuman.nc.util.NcUtils;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.collection.HashList;
import igentuman.nc.util.functions.Chunk3D;
import igentuman.nc.util.functions.ConstantPredicates;
import igentuman.nc.util.functions.Coord4D;
import igentuman.nc.util.math.MathUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

@NothingNullByDefault
public class RadiationManager implements IRadiationManager {

     public static final RadiationManager INSTANCE = new RadiationManager();
    private static final String DATA_HANDLER_NAME = "nc_radiation";
    private static final IntSupplier MAX_RANGE = () -> 8 * 16;
    private static final Random RAND = new Random();

    public static final double BASELINE = 0.000_000_100; // 100 nSv/h
    public static final double MIN_MAGNITUDE = 0.000_010; // 10 uSv/h

    private boolean loaded;

    private final Table<Chunk3D, Coord4D, RadiationSource> radiationTable = HashBasedTable.create();
    private final Table<Chunk3D, Coord4D, IRadiationSource> radiationView = Tables.unmodifiableTable(radiationTable);

    private final Map<UUID, PreviousRadiationData> playerEnvironmentalExposureMap = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, PreviousRadiationData> playerExposureMap = new Object2ObjectOpenHashMap<>();

    // client fields
    private RadiationScale clientRadiationScale = RadiationScale.NONE;
    private double clientEnvironmentalRadiation = BASELINE;
    private double clientMaxMagnitude = BASELINE;

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private RadiationDataHandler dataHandler;

    private RadiationManager() {
    }

    @Override
    public boolean isRadiationEnabled() {
        return true;
    }

    private void markDirty() {
        if (dataHandler != null) {
            dataHandler.setDirty();
        }
    }

    @Override
    public DamageSource getRadiationDamageSource() {
        return NcDamageSource.RADIATION;
    }

    @Override
    public double getRadiationLevel(Entity entity) {
        return getRadiationLevel(new Coord4D(entity));
    }

    /**
     * Calculates approximately how long in seconds radiation will take to decay
     *
     * @param magnitude Magnitude
     * @param source    {@code true} for if it is a {@link IRadiationSource} or an {@link IRadiationEntity} decaying
     */
    public int getDecayTime(double magnitude, boolean source) {
        double decayRate = source ? 0.9995D : 0.9995D;
        int seconds = 0;
        double localMagnitude = magnitude;
        while (localMagnitude > RadiationManager.MIN_MAGNITUDE) {
            localMagnitude *= decayRate;
            seconds++;
        }
        return seconds;
    }

    @Override
    public Table<Chunk3D, Coord4D, IRadiationSource> getRadiationSources() {
        return radiationView;
    }

    @Override
    public void removeRadiationSources(Chunk3D chunk) {
        Map<Coord4D, RadiationSource> chunkSources = radiationTable.row(chunk);
        if (!chunkSources.isEmpty()) {
            chunkSources.clear();
            markDirty();
            updateClientRadiationForAll(chunk.dimension);
        }
    }

    @Override
    public void removeRadiationSource(Coord4D coord) {
        Chunk3D chunk = new Chunk3D(coord);
        if (radiationTable.contains(chunk, coord)) {
            radiationTable.remove(chunk, coord);
            markDirty();
            updateClientRadiationForAll(coord.dimension);
        }
    }

    @Override
    public double getRadiationLevel(Coord4D coord) {
        return getRadiationLevelAndMaxMagnitude(coord).level();
    }

    public LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(Entity player) {
        return getRadiationLevelAndMaxMagnitude(new Coord4D(player));
    }

    public LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(Coord4D coord) {
        double level = BASELINE;
        double maxMagnitude = BASELINE;
        for (Chunk3D chunk : new Chunk3D(coord).expand(4)) {
            for (Map.Entry<Coord4D, RadiationSource> entry : radiationTable.row(chunk).entrySet()) {
                // we only compute exposure when within the MAX_RANGE bounds
                if (entry.getKey().distanceTo(coord) <= MAX_RANGE.getAsInt()) {
                    RadiationSource source = entry.getValue();
                    level += computeExposure(coord, source);
                    maxMagnitude = Math.max(maxMagnitude, source.getMagnitude());
                }
            }
        }
        return new LevelAndMaxMagnitude(level, maxMagnitude);
    }

    @Override
    public void radiate(Coord4D coord, double magnitude) {
        if (!isRadiationEnabled()) {
            return;
        }
        Map<Coord4D, RadiationSource> radiationSourceMap = radiationTable.row(new Chunk3D(coord));
        RadiationSource src = radiationSourceMap.get(coord);
        if (src == null) {
            radiationSourceMap.put(coord, new RadiationSource(coord, magnitude));
        } else {
            src.radiate(magnitude);
        }
        markDirty();
        //Update radiation levels immediately
        updateClientRadiationForAll(coord.dimension);
    }

    @Override
    public void radiate(LivingEntity entity, double magnitude) {
        if (!isRadiationEnabled()) {
            return;
        }
        if (!(entity instanceof Player player) || NcUtils.isPlayingMode(player)) {
            entity.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> c.radiate(magnitude * (1 - Math.min(1, getRadiationResistance(entity)))));
        }
    }


    public void clearSources() {
        if (!radiationTable.isEmpty()) {
            radiationTable.clear();
            markDirty();
            updateClientRadiationForAll(ConstantPredicates.alwaysTrue());
        }
    }

    private double computeExposure(Coord4D coord, RadiationSource source) {
        return source.getMagnitude() / Math.max(1, coord.distanceToSquared(source.getPos()));
    }

    private double getRadiationResistance(LivingEntity entity) {
        double resistance = 0;

        return resistance;
    }

    private void updateClientRadiationForAll(ResourceKey<Level> dimension) {
        updateClientRadiationForAll(player -> player.getLevel().dimension() == dimension);
    }

    private void updateClientRadiationForAll(Predicate<ServerPlayer> clearForPlayer) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            //Validate it is not null in case we somehow are being called from the client or at some other unexpected time
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (clearForPlayer.test(player)) {
                    updateClientRadiation(player);
                }
            }
        }
    }

    public void updateClientRadiation(ServerPlayer player) {
        LevelAndMaxMagnitude levelAndMaxMagnitude = RadiationManager.INSTANCE.getRadiationLevelAndMaxMagnitude(player);
        PreviousRadiationData previousRadiationData = playerEnvironmentalExposureMap.get(player.getUUID());
        PreviousRadiationData relevantData = PreviousRadiationData.compareTo(previousRadiationData, levelAndMaxMagnitude.level());
        if (relevantData != null) {
            playerEnvironmentalExposureMap.put(player.getUUID(), relevantData);
            NuclearCraft.packetHandler().sendTo(PacketRadiationData.createEnvironmental(levelAndMaxMagnitude), player);
        }
    }

    public void setClientEnvironmentalRadiation(double radiation, double maxMagnitude) {
        clientEnvironmentalRadiation = radiation;
        clientMaxMagnitude = maxMagnitude;
        clientRadiationScale = RadiationScale.get(clientEnvironmentalRadiation);
    }

    public double getClientEnvironmentalRadiation() {
        return isRadiationEnabled() ? clientEnvironmentalRadiation : BASELINE;
    }

    public double getClientMaxMagnitude() {
        return isRadiationEnabled() ? clientMaxMagnitude : BASELINE;
    }

    public RadiationScale getClientScale() {
        return isRadiationEnabled() ? clientRadiationScale : RadiationScale.NONE;
    }

    public void tickClient(Player player) {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        // perhaps also play Geiger counter sound effect, even when not using item (similar to fallout)
        if (clientRadiationScale != RadiationScale.NONE && player.level.getRandom().nextInt(2) == 0) {
            int count = player.level.getRandom().nextInt(clientRadiationScale.ordinal() * 2);
            int radius = 2;
            for (int i = 0; i < count; i++) {
                double x = player.getX() + player.level.getRandom().nextDouble() * radius * 2 - radius;
                double y = player.getY() + player.level.getRandom().nextDouble() * radius * 2 - radius;
                double z = player.getZ() + player.level.getRandom().nextDouble() * radius * 2 - radius;
                player.level.addParticle(NcParticleTypes.RADIATION.get(), x, y, z, 0, 0, 0);
            }
        }
    }

    public void tickServer(ServerPlayer player) {
        updateEntityRadiation(player);
    }

    private void updateEntityRadiation(LivingEntity entity) {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        LazyOptional<IRadiationEntity> radiationCap = entity.getCapability(Capabilities.RADIATION_ENTITY);
        // each tick, there is a 1/20 chance we will apply radiation to each player
        // this helps distribute the CPU load across ticks, and makes exposure slightly inconsistent
        if (entity.level.getRandom().nextInt(20) == 0) {
            double magnitude = getRadiationLevel(entity);
            if (magnitude > BASELINE && (!(entity instanceof Player player) || NcUtils.isPlayingMode(player))) {
                // apply radiation to the player
                radiate(entity, magnitude / 3_600D); // convert to Sv/s
            }
            radiationCap.ifPresent(IRadiationEntity::decay);
        }
        // update the radiation capability (decay, sync, effects)
        radiationCap.ifPresent(c -> {
            c.update(entity);
            if (entity instanceof ServerPlayer player) {
                double radiation = c.getRadiation();
                PreviousRadiationData previousRadiationData = playerExposureMap.get(player.getUUID());
                PreviousRadiationData relevantData = PreviousRadiationData.compareTo(previousRadiationData, radiation);
                if (relevantData != null) {
                    playerExposureMap.put(player.getUUID(), relevantData);
                    NuclearCraft.packetHandler().sendTo(PacketRadiationData.createPlayer(radiation), player);
                }
            }
        });
    }

    public void tickServerWorld(Level world) {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        if (!loaded) {
            createOrLoad();
        }
    }

    public void tickServer() {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        // each tick, there's a 1/20 chance we'll decay radiation sources (averages to 1 decay operation per second)
        if (RAND.nextInt(20) == 0) {
            Collection<RadiationSource> sources = radiationTable.values();
            if (!sources.isEmpty()) {
                // remove if source gets too low
                sources.removeIf(RadiationSource::decay);
                //Mark dirty regardless if we have any sources as magnitude changes or radiation sources change
                markDirty();
                //Update radiation levels for any players where it has changed
                updateClientRadiationForAll(ConstantPredicates.alwaysTrue());
            }
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        if (dataHandler == null) {
            //Always associate the world with the over world as the frequencies are global
            DimensionDataStorage savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage();
            dataHandler = savedData.computeIfAbsent(tag -> {
                RadiationDataHandler handler = new RadiationDataHandler();
                handler.load(tag);
                return handler;
            }, RadiationDataHandler::new, DATA_HANDLER_NAME);
            dataHandler.setManagerAndSync(this);
            dataHandler.clearCached();
        }

        loaded = true;
    }

    public void reset() {
        //Clear the table directly instead of via the method, so it doesn't mark it as dirty
        radiationTable.clear();
        playerEnvironmentalExposureMap.clear();
        playerExposureMap.clear();
        dataHandler = null;
        loaded = false;
    }

    public void resetClient() {
        setClientEnvironmentalRadiation(BASELINE, BASELINE);
    }

    public void resetPlayer(UUID uuid) {
        playerEnvironmentalExposureMap.remove(uuid);
        playerExposureMap.remove(uuid);
    }

    @SubscribeEvent
    public void onLivingTick(LivingTickEvent event) {
        Level world = event.getEntity().getCommandSenderWorld();
        if (!world.isClientSide() && !(event.getEntity() instanceof Player)) {
            updateEntityRadiation(event.getEntity());
        }
    }

    public record LevelAndMaxMagnitude(double level, double maxMagnitude) {
    }

    public enum RadiationScale {
        NONE,
        LOW,
        MEDIUM,
        ELEVATED,
        HIGH,
        EXTREME;

        /**
         * Get the corresponding RadiationScale from an equivalent dose rate (Sv/h)
         */
        public static RadiationScale get(double magnitude) {
            if (magnitude < 0.00001) { // 10 uSv/h
                return NONE;
            } else if (magnitude < 0.001) { // 1 mSv/h
                return LOW;
            } else if (magnitude < 0.1) { // 100 mSv/h
                return MEDIUM;
            } else if (magnitude < 10) { // 100 Sv/h
                return ELEVATED;
            } else if (magnitude < 100) {
                return HIGH;
            }
            return EXTREME;
        }


        private static final double LOG_BASELINE = Math.log10(MIN_MAGNITUDE);
        private static final double LOG_MAX = Math.log10(100); // 100 Sv
        private static final double SCALE = LOG_MAX - LOG_BASELINE;

        /**
         * Gets the severity of a dose (between 0 and 1) from a provided dosage in Sv.
         */
        public static double getScaledDoseSeverity(double magnitude) {
            if (magnitude < MIN_MAGNITUDE) {
                return 0;
            }
            return Math.min(1, Math.max(0, (-LOG_BASELINE + Math.log10(magnitude)) / SCALE));
        }
/*
        public SoundEvent getSoundEvent() {
            return switch (this) {
                case LOW -> NcSounds.GEIGER_SLOW.get();
                case MEDIUM -> NcSounds.GEIGER_MEDIUM.get();
                case ELEVATED, HIGH -> NcSounds.GEIGER_ELEVATED.get();
                case EXTREME -> NcSounds.GEIGER_FAST.get();
                default -> null;
            };
        }*/
    }

    private record PreviousRadiationData(double magnitude, int power, double base) {

        private static int getPower(double magnitude) {
            return MathUtils.clampToInt(Math.floor(Math.log10(magnitude)));
        }

        @Nullable
        private static PreviousRadiationData compareTo(@Nullable PreviousRadiationData previousRadiationData, double magnitude) {
            if (previousRadiationData == null || Math.abs(magnitude - previousRadiationData.magnitude) >= previousRadiationData.base) {
                //No cached value or the magnitude changed by more than the smallest unit we display
                return getData(magnitude, getPower(magnitude));
            } else if (magnitude < previousRadiationData.magnitude) {
                //Magnitude has decreased, and by a smaller amount than the smallest unit we currently are displaying
                int power = getPower(magnitude);
                if (power < previousRadiationData.power) {
                    //Check if the number of digits decreased, in which case even if we potentially only decreased by a tiny amount
                    // we still need to sync and update it
                    return getData(magnitude, power);
                }
            }
            //No need to sync
            return null;
        }

        private static PreviousRadiationData getData(double magnitude, int power) {
            //Unit display happens using SI units which is in factors of 1,000 (10^3) convert our power to the current SI unit it is for
            int siPower = Math.floorDiv(power, 3) * 3;
            //Note: We subtract two from the power because for places we sync to and read from on the client side
            // we have two decimal places, so we need to shift our target to include those decimals
            double base = Math.pow(10, siPower - 2);
            return new PreviousRadiationData(magnitude, power, base);
        }
    }

    public static class RadiationDataHandler extends SavedData {

        public List<RadiationSource> loadedSources = Collections.emptyList();
        @Nullable
        public RadiationManager manager;

        public void setManagerAndSync(RadiationManager m) {
            manager = m;
            if (CommonConfig.RadiationConfig.ENABLED.get()) {
                for (RadiationSource source : loadedSources) {
                    manager.radiationTable.put(new Chunk3D(source.getPos()), source.getPos(), source);
                }
            }
        }

        public void clearCached() {
            loadedSources = Collections.emptyList();
        }

        public void load(@NotNull CompoundTag nbtTags) {
            if (nbtTags.contains(NBTConstants.RADIATION_LIST, Tag.TAG_LIST)) {
                ListTag list = nbtTags.getList(NBTConstants.RADIATION_LIST, Tag.TAG_COMPOUND);
                loadedSources = new HashList<>(list.size());
                for (Tag nbt : list) {
                    loadedSources.add(RadiationSource.load((CompoundTag) nbt));
                }
            } else {
                loadedSources = Collections.emptyList();
            }
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbtTags) {
            if (manager != null && !manager.radiationTable.isEmpty()) {
                ListTag list = new ListTag();
                for (RadiationSource source : manager.radiationTable.values()) {
                    CompoundTag compound = new CompoundTag();
                    source.write(compound);
                    list.add(compound);
                }
                nbtTags.put(NBTConstants.RADIATION_LIST, list);
            }
            return nbtTags;
        }
    }
}
