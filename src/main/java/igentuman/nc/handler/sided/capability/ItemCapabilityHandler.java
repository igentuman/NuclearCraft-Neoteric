package igentuman.nc.handler.sided.capability;

import igentuman.api.platform.NCItemStacks;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import igentuman.api.platform.NCSerialization;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.*;

public class ItemCapabilityHandler extends AbstractCapabilityHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    public Supplier<List<ItemStack>> allowedInputItems;
    public final HashMap<Integer, List<Item>> validItemsForSlot = new HashMap<>();
    protected NonNullList<ItemStack> stacks;
    protected ItemStack[] sortedStacks;
    private final Map<Direction, ItemHandlerWrapper> handlerCache = new HashMap<>();
    public final List<ItemStack> holdedInputs = new ArrayList<>();

    public ItemCapabilityHandler(int input, int output) {
        this.inputSlots = input;
        this.outputSlots = output;
        stacks = NonNullList.withSize(input + output, ItemStack.EMPTY);
        initDefault();
    }

    @Override
    public int toggleMode(int slot, int side) {
        cleanCache();
        return super.toggleMode(slot, side);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        try {
            validateSlotIndex(slot);
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }


    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        return extractItem(slot, amount, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, NCItemStacks.copyWithCount(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return NCItemStacks.copyWithCount(existing, toExtract);
        }
    }

    public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isValidForSlotInternal(stack, slot)) return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!(NCItemStacks.canStack(stack, existing) && existing.getCount() <= limit))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? NCItemStacks.copyWithCount(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? NCItemStacks.copyWithCount(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(getType(slot).equals(SidedContentHandler.SlotType.OUTPUT))
            return stack;
        return insertItemInternal(slot, stack, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if(isValidInputItem(stack)) {
            return true;
        }
        if(validItemsForSlot.containsKey(slot)) {
            return validItemsForSlot.get(slot).contains(stack.getItem());
        }
        return false;
    }

    private int isValidForAnyInputSlot(ItemStack stack) {
        for (int i = 0; i < inputSlots; i++) {
            if (!isItemValid(i, stack)) continue;
            if (getStackInSlot(i).isEmpty()) return i;
            if (NCItemStacks.canStack(getStackInSlot(i), stack) && getStackInSlot(i).getCount() < getSlotLimit(i)) {
                return i;
            }
        }
        return -1;
    }

    protected boolean isValidForSlotInternal(ItemStack stack, int slot) {
        return getStackInSlot(slot).isEmpty()
                || (NCItemStacks.canStack(getStackInSlot(slot), stack)
                && getStackInSlot(slot).getCount() < getSlotLimit(slot));
    }

    private int isValidForAnyOutputSlot(ItemStack stack) {
        for (int i = inputSlots; i < getSlots(); i++) {
            if (!isItemValid(i, stack)) continue;
            if (getStackInSlot(i).isEmpty()) return i;
            if (NCItemStacks.canStack(getStackInSlot(i), stack) && getStackInSlot(i).getCount() < getSlotLimit(i)) {
                return i;
            }
        }
        return -1;
    }

    private int getNextSlot(int currentSlot) {
        for (int i = currentSlot; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) return i;
        }
        return 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = (CompoundTag) NCSerialization.saveItemStack(stacks.get(i), provider);
                itemTag.putInt("Slot", i);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        nbt.put("sideMap", SidedContentHandler.serializeSideMap(sideMap));
        return nbt;
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, NCSerialization.loadItemStack(provider, itemTags));
            }
        }
        if (!nbt.getCompound("sideMap").isEmpty()) {
            sideMap = SidedContentHandler.deserializeSideMap(nbt.getCompound("sideMap"));
        }
        onLoad();

    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onContentsChanged(int slot) {
        if(slot > inputSlots-1) {
            sortedStacks = null;
        }
    }

    public ItemHandlerWrapper getCapability(Direction side) {
        if(side == null) return globalCap();
        if(!handlerCache.containsKey(side)) {
            handlerCache.put(side, new ItemHandlerWrapper(side, this, (i) -> outputAllowed(i, side), (i, s) -> inputAllowed(i, s, side)));
        }
        return handlerCache.get(side);
    }
    ItemHandlerWrapper globalCap;
    private ItemHandlerWrapper globalCap() {
        if(globalCap == null) {
            globalCap = new ItemHandlerWrapper(null, this, (i) -> outputAllowed(i, null), (i, s) -> inputAllowed(i, s, null));
        }
        return globalCap;
    }

    public void cleanCache() {
        handlerCache.clear();
    }

    private boolean outputAllowed(Integer i, Direction side) {
            if(side == null) return true;
            SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(side, getFacing());
            SlotModePair.SlotMode mode = sideMap.get(relativeDirection.ordinal())[i].getMode();
            return mode == OUTPUT || mode == PUSH || mode == PUSH_EXCESS;
    }

    private boolean inputAllowed(Integer i, ItemStack stack, Direction side) {
        boolean validForSlot = true;
        if (validItemsForSlot.containsKey(i)) {
            validForSlot = validItemsForSlot.get(i).contains(stack.getItem());
        }
        if (!validForSlot) return false;
        if (side == null) {
        //todo remove
            return i == isValidForAnyInputSlot(stack);
        }
        SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(side, getFacing());
        SlotModePair.SlotMode mode = sideMap.get(relativeDirection.ordinal())[i].getMode();
        return (mode == INPUT || mode == PULL || mode == DEFAULT) && isValidInputItem(stack);
    }

    public boolean isValidInputItem(ItemStack item)
    {
        if(allowedInputItems == null) return false;
        if(allowedInputItems.get().contains(item)) return true;
        for(ItemStack stack: allowedInputItems.get()) {
            if(stack.is(item.getItem())) {
                return true;
            }
        }
        return allowedInputItems.get().isEmpty();
    }
    public boolean pushItems(Direction dir) {
       return pushItems(dir, false, tile.getBlockPos());
    }

    public boolean pushItems(Direction dir, boolean forceFlag, BlockPos pos) {
        IItemHandler handler = tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
        if(handler == null) return false;
        SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
        boolean pushed = false;
        for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
            if(pair.getMode() == PUSH || (forceFlag && pair.getMode() == OUTPUT)) {
                ItemStack stack = getStackInSlot(pair.getSlot());
                if(stack.isEmpty()) continue;
                ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, false);
                if(remainder.getCount() != stack.getCount()) {
                    setStackInSlot(pair.getSlot(), remainder);
                    pushed = true;
                }
            }
        }
        return pushed;
    }
    public boolean pullItems(Direction dir) {
       return pullItems(dir, false, tile.getBlockPos());
    }

    public boolean pullItems(Direction dir, boolean forceFlag, BlockPos pos) {
        IItemHandler handler = tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
        if(handler == null) return false;
        SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
        for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
            if(pair.getMode() == PULL || (forceFlag && pair.getMode() == INPUT)) {
                for(int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.extractItem(i, 64, true);
                        if(stack.isEmpty()) continue;
                    if(!isValidInputItem(stack)) continue;
                    ItemStack remainder = insertItem(pair.getSlot(), stack.copy(), false);
                    if(remainder.getCount() != stack.getCount()) {
                        handler.extractItem(i, stack.getCount() - remainder.getCount(), false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isValidForOutputSlots(ItemStack outputItem) {
        for(int i = inputSlots; i < getSlots(); i++) {
            if(isValidForOutputSlot(i, outputItem)) return true;
        }
        return false;
    }

    public boolean isValidForOutputSlot(int i, ItemStack outputItem) {
        if (outputAllowed(i, null)) {
            ItemStack stack = getStackInSlot(i);
            if(stack.isEmpty()) return true;
            return NCItemStacks.canStack(stack, outputItem)
                    && stack.getMaxStackSize() >= outputItem.getCount() + stack.getCount();
        }
        return false;
    }

    public boolean canPushExcessItems(int i, ItemStack outputItem) {
        for(Direction dir: Direction.values()) {
            IItemHandler handler = tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos().relative(dir), dir.getOpposite());
            if(handler == null) continue;
            SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
            for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
                if(pair.getSlot() != i) continue;
                if(pair.getMode() == PUSH || pair.getMode() == PUSH_EXCESS) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, outputItem, true);
                    if(remainder.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ItemStack pushExcessItems(int i, ItemStack outputItem) {
        for(Direction dir: Direction.values()) {
            IItemHandler handler = tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos().relative(dir), dir.getOpposite());
            if(handler == null) continue;
            SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
            for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
                if(pair.getSlot() != i) continue;
                if(pair.getMode() == PUSH || pair.getMode() == PUSH_EXCESS) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, outputItem, true);
                    if(remainder.isEmpty()) {
                        return ItemHandlerHelper.insertItem(handler, outputItem, false);
                    }
                }
            }
        }
        return outputItem;
    }

    public String getCacheKey() {
        String key = "";
        if(sortedStacks == null) {
            sortedStacks = new ItemStack[inputSlots];
            for(int i = 0; i < inputSlots; i++) {
                sortedStacks[i] = getStackInSlot(i);
            }
            Arrays.sort(sortedStacks, Comparator.comparing(itemStack -> itemStack.getItem().toString()));
        }

        for(ItemStack stack : sortedStacks) {
            if(!stack.isEmpty()) {
                key += stack.getItem().toString();
            }
        }
        return key;
    }

    public void voidSlot(int i) {
        setStackInSlot(i, ItemStack.EMPTY);
    }

    public Object[] getSlotContent(int slotId) {
        ItemStack stack = getStackInSlot(slotId);
        if(stack.isEmpty()) return new Object[]{};
        return new Object[]{stack.getCount(), BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()};
    }

    public boolean canPush() {
        for(int i = inputSlots; i < getSlots(); i++) {
            if(!getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    public boolean canPull() {
        for(int i = 0; i < inputSlots; i++) {
            final ItemStack stack = getStackInSlot(i);
            if(stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    public void setValidItemsForSlot(List<Item> fuelItems, int slotId) {
        validItemsForSlot.put(slotId, fuelItems);
    }
}
