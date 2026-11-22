package igentuman.nc.handler.storage;

import igentuman.nc.handler.fluid.FluidStackHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Transactional single-barrel item access sharing the placed barrel's typed block-entity data. */
public class BarrelItemFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    private static final String TANK_KEY = "FluidTank";
    private final Item validItem;
    private final int capacity;
    private final Supplier<BlockEntityType<?>> beType;

    public BarrelItemFluidHandler(ItemAccess access, int capacity, Supplier<BlockEntityType<?>> beType) {
        super(access, 1);
        if (capacity < 0) throw new IllegalArgumentException("Negative barrel capacity");
        this.validItem = access.getResource().getItem();
        this.capacity = capacity;
        this.beType = beType;
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        return FluidResource.of(read(accessResource).fluid());
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        return read(accessResource).fluid().getAmount();
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return read(itemAccess.getResource()).capacity();
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return itemAccess.getResource().is(validItem) && itemAccess.getAmount() == 1;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return itemAccess.getAmount() == 1 && itemAccess.getResource().is(validItem)
                ? super.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return itemAccess.getAmount() == 1 && itemAccess.getResource().is(validItem)
                ? super.extract(index, resource, amount, transaction) : 0;
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource resource, int amount) {
        var data = accessResource.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag root = data == null ? new CompoundTag() : data.copyTagWithoutId();
        int savedCapacity = read(accessResource).capacity();
        var tank = new FluidStackHandler.SavedTank(0, savedCapacity, resource.toStack(amount));
        CompoundTag saved = new CompoundTag();
        saved.putInt("Size", 1);
        saved.put("Tanks", FluidStackHandler.TANKS_CODEC.encodeStart(NbtOps.INSTANCE, List.of(tank)).getOrThrow());
        root.put(TANK_KEY, saved);
        return accessResource.with(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(beType.get(), root));
    }

    private FluidStackHandler.SavedTank read(ItemResource resource) {
        var empty = new FluidStackHandler.SavedTank(0, capacity, FluidStack.EMPTY);
        if (!resource.is(validItem)) return empty;
        var data = resource.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return empty;
        if (data.type() != beType.get()) throw new IllegalArgumentException("Unexpected barrel block-entity type");
        CompoundTag root = data.copyTagWithoutId();
        if (!root.contains(TANK_KEY)) return empty;
        CompoundTag saved = root.getCompound(TANK_KEY)
                .orElseThrow(() -> new IllegalArgumentException("Invalid barrel fluid storage"));
        ListTag tanks = saved.getList("Tanks")
                .orElseThrow(() -> new IllegalArgumentException("Missing barrel tank list"));
        List<FluidStackHandler.SavedTank> decoded = FluidStackHandler.TANKS_CODEC
                .parse(NbtOps.INSTANCE, tanks).getOrThrow();
        FluidStackHandler.validateSavedTanks(saved.getIntOr("Size", -1), decoded).getOrThrow();
        if (decoded.size() != 1) throw new IllegalArgumentException("Expected one barrel tank");
        return decoded.getFirst();
    }
}
