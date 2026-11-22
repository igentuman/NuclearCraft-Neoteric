package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.item.ItemResource;
import igentuman.nc.handler.item.CustomInventoryHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/** Menu opened from a multiblock port; mirrors the controller's inventory slots and exposes the redstone mode. */
public class MultiblockPortContainer extends AbstractContainerMenu {

    private final MultiblockPortBE blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private int beSlots = 0;

    public MultiblockPortContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (MultiblockPortBE) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                null);
    }

    public MultiblockPortContainer(int containerId, Inventory playerInventory,
                                   MultiblockPortBE blockEntity, ContainerData data) {
        super(ModEntries.get(blockEntity.name).menu().get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        this.data = data != null ? data : new SimpleContainerData(Math.max(1, blockEntity.getSyncFieldCount()));
        addDataSlots(this.data);

        MultiblockControllerBE controllerForLookup = blockEntity.controller();
        MultiblockEntry mbEntry = controllerForLookup != null
                ? MultiblockRegistry.getByController(controllerForLookup.getMultiblockName())
                : MultiblockRegistry.getByPort(blockEntity.name);
        if (mbEntry != null) {
            ModEntry controllerEntry = mbEntry.controllerEntry();
            if (controllerEntry != null && controllerEntry.itemCap() != null) {
                SlotsLayout slotsLayout = controllerEntry.slotsLayout();
                int inputItemCount = controllerEntry.itemCap().inputSlots;
                int inputFluidCount = controllerEntry.fluidCap() != null ? controllerEntry.fluidCap().inputTanks.size() : 0;
                int outputItemCount = controllerEntry.itemCap().outputSlots;

                MultiblockControllerBE controller = controllerForLookup;
                ResourceHandler<ItemResource> inv;
                IndexModifier<ItemResource> modifier;
                boolean hasInventory;
                if (controller != null) {
                    var items = controller.contentHandler.getItemHandler();
                    inv = items;
                    modifier = (index, resource, amount) -> items.set(index, resource, amount);
                    hasInventory = controller.hasInventory();
                } else {
                    var items = new CustomInventoryHandler(inputItemCount + outputItemCount);
                    inv = items;
                    modifier = items::set;
                    hasInventory = true;
                }

                if (hasInventory) {
                    if (slotsLayout != null) {
                        int inputItemOffset = 0;
                        int outputItemOffset = inputItemCount + inputFluidCount;

                        for (int i = 0; i < inputItemCount; i++) {
                            SlotDef def = slotsLayout.slots.get(inputItemOffset + i);
                            addSlot(new ResourceHandlerSlot(inv, modifier, i, def.x, def.y));
                            beSlots++;
                        }
                        for (int i = 0; i < outputItemCount; i++) {
                            SlotDef def = slotsLayout.slots.get(outputItemOffset + i);
                            addSlot(new ResourceHandlerSlot(inv, modifier, inputItemCount + i, def.x, def.y));
                            beSlots++;
                        }
                    } else {
                        for (int i = 0; i < inputItemCount; i++) {
                            addSlot(new ResourceHandlerSlot(inv, modifier, i, 44 + (i % 3) * 18, 26 + (i / 3) * 18));
                            beSlots++;
                        }
                        for (int i = 0; i < outputItemCount; i++) {
                            addSlot(new ResourceHandlerSlot(inv, modifier, inputItemCount + i, 116 + (i % 3) * 18, 26 + (i / 3) * 18));
                            beSlots++;
                        }
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

    public MultiblockPortBE getBlockEntity() {
        return blockEntity;
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    /** Currently selected redstone mode, read from synced ContainerData (client-safe). */
    public int getRedstoneMode() {
        int idx = blockEntity.getSyncFieldIndex("redstoneMode");
        return idx != -1 ? data.get(idx) : 0;
    }

    public SlotsLayout getLayout() {
        MultiblockControllerBE controllerForLookup = blockEntity.controller();
        MultiblockEntry mbEntry = controllerForLookup != null
                ? MultiblockRegistry.getByController(controllerForLookup.getMultiblockName())
                : MultiblockRegistry.getByPort(blockEntity.name);
        if (mbEntry != null && mbEntry.controllerEntry() != null) {
            return mbEntry.controllerEntry().slotsLayout();
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

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
