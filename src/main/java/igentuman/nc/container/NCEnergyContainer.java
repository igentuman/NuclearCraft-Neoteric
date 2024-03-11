package igentuman.nc.container;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class NCEnergyContainer extends Container {
    protected NCProcessorBE blockEntity;
    protected PlayerEntity playerEntity;
    protected IItemHandler playerInventory;

    public ProcessorPrefab getProcessor() {
        return processor;
    }

    protected ProcessorPrefab processor;

    public int slotIndex = 0;

    protected String name;
    public NCEnergyContainer(@Nullable ContainerType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public NCEnergyContainer(int windowId, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player, String name) {
        this(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), windowId);
        blockEntity = (NCProcessorBE) player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.name = name;
        this.processor = Processors.all().get(name);
        processorSlots();
        layoutPlayerInventorySlots();
    }

    private void processorSlots() {
        int itemIdx = 0;
        for(int[] pos: processor.getSlotsConfig().getSlotPositions()) {
            if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item")) {
                int idx = itemIdx;
                blockEntity.getCapability(ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                    addSlot(new SlotItemHandler(h, idx, pos[0], pos[1]));
                });
                itemIdx++;
            }
        }
        int ux = 154;
        if(getProcessor().supportSpeedUpgrade) {
            int idx = itemIdx;
            int finalUx = ux;
            blockEntity.getCapability(ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlot(new SlotItemHandler(h, idx, finalUx, 77));
            });
            itemIdx++;
            ux -= 18;
        }
        if(getProcessor().supportEnergyUpgrade) {
            int idx = itemIdx;
            int finalUx = ux;
            blockEntity.getCapability(ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlot(new SlotItemHandler(h, idx, finalUx, 77));
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity pPlayer, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                 if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !this.moveItemStackTo(stack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, stack);
        }

        return itemstack;
    }


    private void addSlotRange(IItemHandler handler, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, slotIndex, x, y));
            x += dx;
            slotIndex++;
        }
    }

    protected void addSlotBox(IItemHandler handler, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            addSlotRange(handler, x, y, horAmount, dx);
            y += dy;
        }
    }


    protected void layoutPlayerInventorySlots() {
        int leftCol = 10;
        int topRow = 96;
        // PlayerEntity inventory
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
/*        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                NCProcessors.PROCESSORS.get(name).get()
        );*/
    }

    public TextComponent getTitle() {
        return new TranslationTextComponent("block."+MODID+"."+name);
    }

    public IEnergyStorage getEnergy() {
        return (IEnergyStorage) blockEntity.getEnergy().orElse(null);
    }

    public double getProgress() {
        return blockEntity.getProgress();
    }
}
