package igentuman.nc.container;

import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import igentuman.nc.handler.SlotModePair;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Menu for a multiblock controller: lays out its I/O slots and player inventory and exposes synced progress/state. */
public class MultiblockControllerContainer extends AbstractContainerMenu {

    private final MultiblockControllerBE blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final SlotsLayout slotsLayout;

    public MultiblockControllerContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (MultiblockControllerBE) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                null);
    }

    public MultiblockControllerContainer(int containerId, Inventory playerInventory,
                                          MultiblockControllerBE blockEntity, ContainerData data) {
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

            for (int i = 0; i < inputItemCount; i++) {
                SlotDef def = slotsLayout.slots.get(inputItemOffset + i);
                addSlot(new SlotItemHandler(inv, i, def.x, def.y));
            }
            for (int i = 0; i < outputItemCount; i++) {
                SlotDef def = slotsLayout.slots.get(outputItemOffset + i);
                addSlot(new SlotItemHandler(inv, inputItemCount + i, def.x, def.y));
            }
        } else if (blockEntity.hasInventory()) {
            IItemHandler inv = blockEntity.getItemHandler(null);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int index = row * 3 + col;
                    if (index < blockEntity.slotCount) {
                        addSlot(new SlotItemHandler(inv, index, 56 + col * 60, 35 + row * 18));
                    }
                }
            }
        }

        layoutPlayerSlots(playerInventory);
    }

    protected void layoutPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 95 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 153));
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

    public boolean isFormed() {
        int idx = blockEntity.getSyncFieldIndex("formed");
        return idx != -1 && data.get(idx) != 0;
    }

    public MultiblockControllerBE getBlockEntity() {
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

        int beSlots = blockEntity.slotCount;

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
