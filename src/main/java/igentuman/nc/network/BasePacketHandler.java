package igentuman.nc.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @deprecated Networking is now handled via CustomPacketPayload and RegisterPayloadHandlersEvent.
 * Send methods moved to PacketHandler. Only static buffer utility methods remain.
 */
@Deprecated
public abstract class BasePacketHandler {

    /**
     * Reads an array from a packet buffer.
     *
     * @param buffer       The buffer to read from.
     * @param arrayFactory Factory to create the array (e.g., {@code MyType[]::new}).
     * @param reader       Function to read a single element from the buffer.
     * @param <B>          Buffer type.
     * @param <T>          Element type.
     * @return The deserialized array.
     */
    public static <B extends FriendlyByteBuf, T> T[] readArray(B buffer, IntFunction<T[]> arrayFactory, Function<B, T> reader) {
        int size = buffer.readVarInt();
        T[] array = arrayFactory.apply(size);
        for (int i = 0; i < size; i++) {
            array[i] = reader.apply(buffer);
        }
        return array;
    }

    /**
     * Writes an array to a packet buffer.
     *
     * @param buffer The buffer to write to.
     * @param array  The array to write.
     * @param writer BiConsumer that writes a single element to the buffer.
     * @param <B>    Buffer type.
     * @param <T>    Element type.
     */
    public static <B extends FriendlyByteBuf, T> void writeArray(B buffer, T[] array, BiConsumer<T, B> writer) {
        buffer.writeVarInt(array.length);
        for (T element : array) {
            writer.accept(element, buffer);
        }
    }
}
