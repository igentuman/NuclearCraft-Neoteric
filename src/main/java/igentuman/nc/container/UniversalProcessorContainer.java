package igentuman.nc.container;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import igentuman.nc.handler.SlotModePair;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Menu for a machine processor: lays out its I/O slots and player inventory and exposes synced progress/redstone state. */
public class UniversalProcessorContainer extends AbstractContainerMenu {

    private final GlobalBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final SlotsLayout slotsLayout;

    public UniversalProcessorContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (GlobalBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                null);
    }

    public UniversalProcessorContainer(int containerId, Inventory playerInventory,
                                       GlobalBlockEntity blockEntity, ContainerData data) {
        super(ModEntries.get(blockEntity.name).menu().get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data != null ? data : new SimpleContainerData(blockEntity.getSyncFieldCount());
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addDataSlots(this.data);

        ModEntry entry = ModEntries.get(blockEntity.name);
        this.slotsLayout = entry.slotsLayout();

        if (blockEntity.hasInventory() && slotsLayout != null) {
            IItemHandler inv = blockEntity.getItemHandler(null);

            int inputItemCount  = entry.itemCap()  != null ? entry.itemCap().inputSlots          : 0;
            int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size()   : 0;
            int outputItemCount = entry.itemCap()  != null ? entry.itemCap().outputSlots          : 0;

            int inputItemOffset  = 0;
            int outputItemOffset = inputItemCount + inputFluidCount;

            boolean internalInputs = entry.itemCap() != null && entry.itemCap().internalInputs;
            if (!internalInputs) {
                for (int i = 0; i < inputItemCount; i++) {
                    SlotDef def = slotsLayout.slots.get(inputItemOffset + i);
                    addSlot(new SlotItemHandler(inv, i, def.x, def.y));
                }
            }
            for (int i = 0; i < outputItemCount; i++) {
                SlotDef def = slotsLayout.slots.get(outputItemOffset + i);
                addSlot(new SlotItemHandler(inv, inputItemCount + i, def.x, def.y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
            if (entry.hasCatalysts() && entry.itemCap() != null) {
                int catalystSlotCount = entry.itemCap().catalystSlots;
                int catalystHandlerBase = entry.itemCap().inputSlots + entry.itemCap().outputSlots + entry.itemCap().globalSlots;
                int outputFluidCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;
                int catalystLayoutBase = inputItemCount + inputFluidCount + outputItemCount + outputFluidCount;
                for (int i = 0; i < catalystSlotCount; i++) {
                    SlotDef def = slotsLayout.slots.get(catalystLayoutBase + i);
                    addSlot(new SlotItemHandler(inv, catalystHandlerBase + i, def.x, def.y));
                }
            }
        } else if (blockEntity.hasInventory()) {
            IItemHandler inv = blockEntity.getItemHandler(null);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int index = row * 3 + col;
                    if (index < blockEntity.slotCount) {
                        addSlot(new SlotItemHandler(inv, index, 62 + col * 18, 17 + row * 18));
                    }
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 10 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 10 + col * 18, 154));
        }
    }

    public int getSyncedValue(int index) {
        return data.get(index);
    }

    public int getProgress() {
        return blockEntity.getSyncFieldIndex("progress") != -1
                ? data.get(blockEntity.getSyncFieldIndex("progress")) : 0;
    }

    public int getMaxProgress() {
        return blockEntity.getSyncFieldIndex("maxProgress") != -1
                ? data.get(blockEntity.getSyncFieldIndex("maxProgress")) : 0;
    }

    public int getRedstoneMode() {
        return blockEntity.getSyncFieldIndex("redstoneMode") != -1
                ? data.get(blockEntity.getSyncFieldIndex("redstoneMode")) : 0;
    }

    public GlobalBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public SlotsLayout getLayout() {
        return slotsLayout;
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public SlotModePair.SlotMode getSlotMode(int relativeDir, int slotId) {
        Direction facing;
        try {
            facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        } catch (Exception e) {
            facing = Direction.NORTH;
        }
        RelativeDirection relDir = RelativeDirection.values()[relativeDir];
        Direction absoluteDir = RelativeDirection.toAbsolute(relDir, facing);

        var itemHandler = blockEntity.contentHandler.getItemHandler();
        if (itemHandler != null && slotId < itemHandler.getSlots()) {
            return itemHandler.getModeForAbsoluteSide(absoluteDir, slotId);
        }
        var fluidHandler = blockEntity.contentHandler.getFluidHandler();
        if (fluidHandler != null) {
            int fluidSlot = slotId - (itemHandler != null ? itemHandler.getSlots() : 0);
            if (fluidSlot >= 0 && fluidSlot < fluidHandler.getTanks()) {
                return fluidHandler.getModeForAbsoluteSide(absoluteDir, fluidSlot);
            }
        }
        return SlotModePair.SlotMode.DEFAULT;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        int beSlots = slots.size() - 36;

        if (slotIndex < beSlots) {
            if (!moveItemStackTo(stack, beSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, beSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModEntries.get(blockEntity.name).block().get());
    }

}
