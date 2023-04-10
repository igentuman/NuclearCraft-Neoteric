package igentuman.nc.network;

import igentuman.nc.util.functions.TriConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public abstract class BasePacketHandler {

    protected static SimpleChannel createChannel(ResourceLocation name) {

        return NetworkRegistry.ChannelBuilder.named(name)
              .clientAcceptedVersions("NC"::equals)
              .serverAcceptedVersions("NC"::equals)
              .networkProtocolVersion(() -> "NC")
              .simpleChannel();
    }

    public static String readString(FriendlyByteBuf buffer) {
        return buffer.readUtf(Short.MAX_VALUE);
    }

    public static Vec3 readVector3d(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeVector3d(FriendlyByteBuf buffer, Vec3 vector) {
        buffer.writeDouble(vector.x());
        buffer.writeDouble(vector.y());
        buffer.writeDouble(vector.z());
    }

    public static <TYPE> void writeOptional(FriendlyByteBuf buffer, @Nullable TYPE value, BiConsumer<FriendlyByteBuf, TYPE> writer) {
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            writer.accept(buffer, value);
        }
    }

    @Nullable
    public static <TYPE> TYPE readOptional(FriendlyByteBuf buffer, Function<FriendlyByteBuf, TYPE> reader) {
        return buffer.readBoolean() ? reader.apply(buffer) : null;
    }

    public static <TYPE> void writeArray(FriendlyByteBuf buffer, TYPE[] array, BiConsumer<TYPE, FriendlyByteBuf> writer) {
        buffer.writeVarInt(array.length);
        for (TYPE element : array) {
            writer.accept(element, buffer);
        }
    }

    public static <TYPE> TYPE[] readArray(FriendlyByteBuf buffer, IntFunction<TYPE[]> arrayFactory, Function<FriendlyByteBuf, TYPE> reader) {
        TYPE[] array = arrayFactory.apply(buffer.readVarInt());
        for (int element = 0; element < array.length; element++) {
            array[element] = reader.apply(buffer);
        }
        return array;
    }

    public static <KEY, VALUE> void writeMap(FriendlyByteBuf buffer, Map<KEY, VALUE> map, TriConsumer<KEY, VALUE, FriendlyByteBuf> writer) {
        buffer.writeVarInt(map.size());
        map.forEach((key, value) -> writer.accept(key, value, buffer));
    }

    public static <KEY, VALUE, MAP extends Map<KEY, VALUE>> MAP readMap(FriendlyByteBuf buffer, IntFunction<MAP> mapFactory, Function<FriendlyByteBuf, KEY> keyReader,
          Function<FriendlyByteBuf, VALUE> valueReader) {
        int elements = buffer.readVarInt();
        MAP map = mapFactory.apply(elements);
        for (int element = 0; element < elements; element++) {
            map.put(keyReader.apply(buffer), valueReader.apply(buffer));
        }
        return map;
    }


    private int index = 0;

    protected abstract SimpleChannel getChannel();

    public abstract void initialize();

    protected <MSG extends INcPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
    }

    protected <MSG extends INcPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
    }

    private <MSG extends INcPacket> void registerMessage(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection networkDirection) {
        getChannel().registerMessage(index++, type, INcPacket::encode, decoder, INcPacket::handle, Optional.of(networkDirection));
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG> void sendTo(MSG message, ServerPlayer player) {
        //Validate it is not a fake player, even though none of our code should call this with a fake player
        if (!(player instanceof FakePlayer)) {
            getChannel().send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public <MSG> void sendToAll(MSG message) {
        getChannel().send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Send this message to everyone connected to the server if the server has loaded.
     *
     * @param message - message to send
     *
     * @apiNote This is useful for reload listeners
     */
    public <MSG> void sendToAllIfLoaded(MSG message) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            //If the server has loaded, send to all players
            sendToAll(message);
        }
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message   - the message to send
     * @param dimension - the dimension to target
     */
    public <MSG> void sendToDimension(MSG message, ResourceKey<Level> dimension) {
        getChannel().send(PacketDistributor.DIMENSION.with(() -> dimension), message);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public <MSG> void sendToServer(MSG message) {
        getChannel().sendToServer(message);
    }

    public <MSG> void sendToAllTracking(MSG message, Entity entity) {
        getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public <MSG> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
        getChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public <MSG> void sendToAllTracking(MSG message, BlockEntity tile) {
        sendToAllTracking(message, tile.getLevel(), tile.getBlockPos());
    }

    public <MSG> void sendToAllTracking(MSG message, Level world, BlockPos pos) {
        if (world instanceof ServerLevel level) {
            //If we have a ServerWorld just directly figure out the ChunkPos to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(message, p));
        } else {
            //Otherwise, fallback to entities tracking the chunk if some mod did something odd and our world is not a ServerWorld
            getChannel().send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))), message);
        }
    }
}