package igentuman.nc.container;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.api.pipe.PipeCapabilityType;
import igentuman.nc.api.pipe.RedstoneMode;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_COUNT;
import static igentuman.nc.util.TextUtils.__;

public class PipeConnectorContainer extends AbstractContainerMenu {

    public static final int BTN_MODE = 200;
    public static final int BTN_REDSTONE = 201;
    public static final int BTN_CAP_ITEM = 202;
    public static final int BTN_CAP_FLUID = 203;
    public static final int BTN_CAP_ENERGY = 204;

    public final PipeConnectorBE blockEntity;
    private final Player player;
    private int clientMode;
    private int clientRedstone;
    private final int[] clientCaps = new int[CAP_COUNT];
    private final DataSlot modeSlot = new DataSlot() {
        @Override
        public int get() {
            return isServer() ? blockEntity.getMode().ordinal() : clientMode;
        }

        @Override
        public void set(int value) {
            clientMode = value;
        }
    };
    private final DataSlot redstoneSlot = new DataSlot() {
        @Override
        public int get() {
            return isServer() ? blockEntity.getRedstoneMode().ordinal() : clientRedstone;
        }

        @Override
        public void set(int value) {
            clientRedstone = value;
        }
    };
    private final DataSlot[] capSlots = new DataSlot[CAP_COUNT];

    public PipeConnectorContainer(int windowId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(windowId, inv, (PipeConnectorBE) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PipeConnectorContainer(int windowId, Inventory inv, PipeConnectorBE blockEntity) {
        super(ModEntries.get("pipe_connector").menu().get(), windowId);
        this.player = inv.player;
        this.blockEntity = blockEntity;
        addDataSlot(modeSlot);
        addDataSlot(redstoneSlot);
        for (int i = 0; i < CAP_COUNT; i++) {
            final int index = i;
            capSlots[i] = new DataSlot() {
                @Override
                public int get() {
                    return isServer() ? (blockEntity.isCapabilityEnabled(index) ? 1 : 0) : clientCaps[index];
                }

                @Override
                public void set(int value) {
                    clientCaps[index] = value;
                }
            };
            addDataSlot(capSlots[i]);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 10 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 10 + col * 18, 154));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        int invStart = 0;
        int invEnd = 27;
        int hotbarStart = 27;
        int hotbarEnd = 36;
        if (index < hotbarStart) {
            if (!moveItemStackTo(stack, hotbarStart, hotbarEnd, false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, invStart, invEnd, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(p, stack);
        return original;
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModEntries.get("pipe_connector").block().get());
    }

    public Component getTitle() {
        return __("block." + MODID + ".pipe_connector");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    private boolean isServer() {
        return blockEntity != null && blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide();
    }

    public ConnectorMode getConnectorMode() {
        ConnectorMode[] all = ConnectorMode.values();
        return all[Math.floorMod(modeSlot.get(), all.length)];
    }

    public RedstoneMode getConnectorRedstoneMode() {
        RedstoneMode[] all = RedstoneMode.values();
        return all[Math.floorMod(redstoneSlot.get(), all.length)];
    }

    public boolean isCapabilityEnabled(PipeCapabilityType type) {
        return capSlots[type.index].get() != 0;
    }
}
