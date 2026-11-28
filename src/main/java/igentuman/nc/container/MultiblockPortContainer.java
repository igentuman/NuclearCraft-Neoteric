package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/** Menu opened from a multiblock port; mirrors the controller's inventory slots and exposes the redstone mode. */
public class MultiblockPortContainer extends AbstractContainerMenu {

    private final ModEntry portEntry;
    private final BlockPos position;
    private final Level level;
    @Nullable
    private final MultiblockPortBE port;
    @Nullable
    private final MultiblockControllerBE serverController;
    @Nullable
    private final BlockPos controllerPos;
    @Nullable
    private final ModEntry controllerEntry;
    private final SlotsLayout layout;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final int beSlots;

    public MultiblockPortContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, OpeningData.read(extraData), null, null, null);
    }

    public MultiblockPortContainer(int containerId, Inventory playerInventory,
                                   MultiblockPortBE port, ContainerData data) {
        this(containerId, playerInventory, port, data, port.controller());
    }

    private MultiblockPortContainer(int containerId, Inventory playerInventory, MultiblockPortBE port,
                                    ContainerData data, @Nullable MultiblockControllerBE controller) {
        this(containerId, playerInventory, new OpeningData(port.name, port.getBlockPos(),
                controller == null ? null : controller.getBlockPos(),
                controller == null ? "" : controller.getMultiblockName(), 0, data.getCount()),
                port, controller, data);
    }

    private MultiblockPortContainer(int containerId, Inventory playerInventory, OpeningData opening,
                                    @Nullable MultiblockPortBE serverPort,
                                    @Nullable MultiblockControllerBE serverController,
                                    @Nullable ContainerData serverData) {
        super(ModEntries.get(opening.portName()).menu().get(), containerId);
        this.portEntry = ModEntries.get(opening.portName());
        this.position = opening.position();
        this.level = playerInventory.player.level();
        this.serverController = serverController;
        this.controllerPos = opening.controllerPos();
        this.controllerEntry = controllerPos == null ? null : ModEntries.get(opening.controllerName());
        this.layout = createLayout(controllerEntry);

        this.port = serverPort != null ? serverPort : findClientPort();
        this.access = serverPort == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(level, position);
        this.data = serverData == null ? new SimpleContainerData(opening.dataCount()) : serverData;
        addDataSlots(this.data);

        IItemHandler items = serverController == null ? null : serverController.getItemHandler(null);
        // The inventory packet populates these slots even if the client cannot see the controller.
        if (serverPort == null && opening.itemSlots() > 0) {
            items = new ItemStackHandler(opening.itemSlots());
        }
        if (items != null) addMachineSlots(items);
        this.beSlots = slots.size();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 10 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 10 + col * 18, 154));
        }
    }

    private void addMachineSlots(IItemHandler items) {
        if (controllerEntry == null || controllerEntry.itemCap() == null) return;
        int inputItems = controllerEntry.itemCap().inputSlots;
        int outputItems = controllerEntry.itemCap().outputSlots;
        int inputFluids = controllerEntry.fluidCap() == null ? 0 : controllerEntry.fluidCap().inputTanks.size();
        if (items.getSlots() < inputItems + outputItems) return;

        for (int i = 0; i < inputItems; i++) {
            addItemSlot(items, i, i);
        }
        for (int i = 0; i < outputItems; i++) {
            addItemSlot(items, inputItems + i, inputItems + inputFluids + i);
        }
    }

    private void addItemSlot(IItemHandler items, int slot, int layoutIndex) {
        SlotDef def = layout.slots.get(layoutIndex);
        addSlot(new SlotItemHandler(items, slot, def.x, def.y));
    }

    private static SlotsLayout createLayout(@Nullable ModEntry entry) {
        SlotsLayout layout = SlotsLayout.create();
        if (entry == null) return layout;

        int inputItems = entry.itemCap() == null ? 0 : entry.itemCap().inputSlots;
        int outputItems = entry.itemCap() == null ? 0 : entry.itemCap().outputSlots;
        int inputTanks = entry.fluidCap() == null ? 0 : entry.fluidCap().inputTanks.size();
        int outputTanks = entry.fluidCap() == null ? 0 : entry.fluidCap().outputTanks.size();
        SlotsLayout customLayout = entry.slotsLayout();
        if (customLayout != null && customLayout.slots.size() >= inputItems + inputTanks + outputItems + outputTanks) {
            return customLayout;
        }

        for (int i = 0; i < inputItems; i++) {
            layout.addInput(44 + (i % 3) * 18, 26 + (i / 3) * 18);
        }
        int inputTankY = 30 + ((inputItems + 2) / 3) * 20;
        for (int i = 0; i < inputTanks; i++) {
            layout.addInput(30 + (i % 3) * 20, inputTankY + (i / 3) * 20);
        }
        for (int i = 0; i < outputItems; i++) {
            layout.addOutput(116 + (i % 3) * 18, 26 + (i / 3) * 18);
        }
        int outputTankY = 30 + ((outputItems + 2) / 3) * 20;
        for (int i = 0; i < outputTanks; i++) {
            layout.addOutput(115 + (i % 3) * 20, outputTankY + (i / 3) * 20);
        }
        return layout;
    }

    @Nullable
    private MultiblockPortBE findClientPort() {
        return level.getBlockEntity(position) instanceof MultiblockPortBE clientPort
                && clientPort.name.equals(portEntry.name()) ? clientPort : null;
    }

    public void writeOpeningData(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(portEntry.name());
        buffer.writeBlockPos(position);
        buffer.writeBoolean(controllerEntry != null);
        if (controllerEntry != null) {
            buffer.writeBlockPos(controllerPos);
            buffer.writeUtf(controllerEntry.name());
        }
        buffer.writeVarInt(beSlots);
        buffer.writeVarInt(data.getCount());
    }

    private record OpeningData(String portName, BlockPos position, @Nullable BlockPos controllerPos,
                               String controllerName, int itemSlots, int dataCount) {
        private static OpeningData read(RegistryFriendlyByteBuf buffer) {
            String portName = buffer.readUtf();
            BlockPos position = buffer.readBlockPos();
            boolean attached = buffer.readBoolean();
            BlockPos controllerPos = attached ? buffer.readBlockPos() : null;
            String controllerName = attached ? buffer.readUtf() : "";
            return new OpeningData(portName, position, controllerPos, controllerName,
                    buffer.readVarInt(), buffer.readVarInt());
        }
    }

    public BlockPos getPosition() {
        return position;
    }

    public String[] getRedstoneModes() {
        return port == null ? new String[0] : port.redstoneModes();
    }

    public int getRedstoneMode() {
        int idx = port == null ? -1 : port.getSyncFieldIndex("redstoneMode");
        return idx >= 0 && idx < data.getCount() ? data.get(idx) : 0;
    }

    @Nullable
    public ModEntry getControllerEntry() {
        return controllerEntry;
    }

    @Nullable
    public MultiblockControllerBE getController() {
        if (!level.isClientSide()) return serverController;
        if (controllerPos == null || controllerEntry == null || !level.hasChunkAt(controllerPos)) return null;
        return level.getBlockEntity(controllerPos) instanceof MultiblockControllerBE controller
                && !controller.isRemoved() && controllerEntry.name().equals(controller.getMultiblockName())
                ? controller : null;
    }

    public SlotsLayout getLayout() {
        return layout;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
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
        if (level.isClientSide()) return true;
        return port != null && !port.isRemoved() && port.getLevel() == player.level()
                && port.controller() == serverController
                && stillValid(access, player, portEntry.block().get());
    }
}
