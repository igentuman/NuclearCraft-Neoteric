package igentuman.api.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Platform translation layer for NeoForge 1.21.1 serialization APIs.
 *
 * <p>All NC code that serializes or deserializes NBT data should go through
 * this class rather than calling NeoForge APIs directly. If the underlying
 * API changes in a future version, only this file needs to be updated.
 *
 * <h3>INBTSerializable (handler serialize/deserialize)</h3>
 * <ul>
 *   <li>{@code handler.serializeNBT()} → {@link #serialize(INBTSerializable, HolderLookup.Provider)}</li>
 *   <li>{@code handler.deserializeNBT(tag)} → {@link #deserialize(INBTSerializable, HolderLookup.Provider, Tag)}</li>
 * </ul>
 *
 * <h3>ItemStack</h3>
 * <ul>
 *   <li>{@code ItemStack.of(tag)} → {@link #loadItemStack(HolderLookup.Provider, CompoundTag)}</li>
 *   <li>{@code stack.save(tag)} → {@link #saveItemStack(ItemStack, HolderLookup.Provider)}</li>
 * </ul>
 *
 * <h3>FluidStack</h3>
 * <ul>
 *   <li>{@code FluidStack.loadFluidStackFromNBT(tag)} → {@link #loadFluidStack(HolderLookup.Provider, CompoundTag)}</li>
 *   <li>{@code fluid.writeToNBT(tag)} → {@link #saveFluidStack(FluidStack, HolderLookup.Provider)}</li>
 * </ul>
 *
 * <h3>FluidTank</h3>
 * <ul>
 *   <li>{@code tank.readFromNBT(tag)} → {@link #readFluidTank(FluidTank, HolderLookup.Provider, CompoundTag)}</li>
 *   <li>{@code tank.writeToNBT(tag)} → {@link #writeFluidTank(FluidTank, HolderLookup.Provider, CompoundTag)}</li>
 * </ul>
 */
public final class NCSerialization {

    private NCSerialization() {}

    // --- INBTSerializable ---

    /**
     * Serialize a handler to NBT.
     * Wraps {@code handler.serializeNBT(provider)}.
     */
    public static <T extends Tag> T serialize(INBTSerializable<T> handler, HolderLookup.Provider provider) {
        return handler.serializeNBT(provider);
    }

    /**
     * Deserialize a handler from NBT.
     * Wraps {@code handler.deserializeNBT(provider, tag)}.
     */
    public static <T extends Tag> void deserialize(INBTSerializable<T> handler, HolderLookup.Provider provider, T tag) {
        handler.deserializeNBT(provider, tag);
    }

    // --- ItemStack ---

    /**
     * Load an ItemStack from NBT.
     * Replaces {@code ItemStack.of(tag)} (1.20.1) / {@code ItemStack.parseOptional(provider, tag)} (1.21.1).
     */
    public static ItemStack loadItemStack(HolderLookup.Provider provider, CompoundTag tag) {
        return ItemStack.parseOptional(provider, tag);
    }

    /**
     * Save an ItemStack to NBT.
     * Replaces {@code stack.save(tag)} (1.20.1) / {@code stack.save(provider)} (1.21.1).
     */
    public static Tag saveItemStack(ItemStack stack, HolderLookup.Provider provider) {
        return stack.save(provider);
    }

    // --- FluidStack ---

    /**
     * Load a FluidStack from NBT.
     * Replaces {@code FluidStack.loadFluidStackFromNBT(tag)} (1.20.1) / {@code FluidStack.parseOptional(provider, tag)} (1.21.1).
     */
    public static FluidStack loadFluidStack(HolderLookup.Provider provider, CompoundTag tag) {
        return FluidStack.parseOptional(provider, tag);
    }

    /**
     * Save a FluidStack to NBT.
     * Replaces {@code fluid.writeToNBT(tag)} (1.20.1) / {@code fluid.save(provider)} (1.21.1).
     */
    public static Tag saveFluidStack(FluidStack fluid, HolderLookup.Provider provider) {
        return fluid.save(provider);
    }

    // --- FluidTank ---

    /**
     * Read a FluidTank's contents from NBT.
     * Replaces {@code tank.readFromNBT(tag)} (1.20.1) / {@code tank.readFromNBT(provider, tag)} (1.21.1).
     */
    public static FluidTank readFluidTank(FluidTank tank, HolderLookup.Provider provider, CompoundTag tag) {
        return tank.readFromNBT(provider, tag);
    }

    /**
     * Write a FluidTank's contents to NBT.
     * Replaces {@code tank.writeToNBT(tag)} (1.20.1) / {@code tank.writeToNBT(provider, tag)} (1.21.1).
     */
    public static CompoundTag writeFluidTank(FluidTank tank, HolderLookup.Provider provider, CompoundTag tag) {
        return tank.writeToNBT(provider, tag);
    }

    // --- FluidStack Network I/O ---

    /**
     * Read a FluidStack from a network buffer.
     * Replaces {@code FluidStack.readFromPacket(buf)} (removed in 1.21.1).
     */
    public static FluidStack readFluidFromNetwork(RegistryFriendlyByteBuf buf) {
        return FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }

    /**
     * Write a FluidStack to a network buffer.
     * Replaces {@code fluid.writeToPacket(buf)} (removed in 1.21.1).
     */
    public static void writeFluidToNetwork(RegistryFriendlyByteBuf buf, FluidStack fluid) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, fluid);
    }
}
