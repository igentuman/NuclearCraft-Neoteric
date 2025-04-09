package igentuman.nc.container;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.processors.config.ProcessorSlots;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.handler.sided.SlotModePair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class NCProcessorContainer<T extends AbstractContainerMenu> extends AbstractContainerMenu {
    protected NCProcessorBE blockEntity;
    protected Player playerEntity;
    protected IItemHandler playerInventory;

    public ProcessorPrefab<?, ?> getProcessor() {
        return processor;
    }

    protected ProcessorPrefab<?, ?> processor;

    public int slotIndex = 0;

    protected String name;

    public NCProcessorContainer(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public NCProcessorContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player, String name) {
        this(NCProcessors.PROCESSORS_CONTAINERS.get(name).get(), windowId);
        blockEntity = (NCProcessorBE) player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.name = name;
        this.processor = Processors.all().get(name);
        processorSlots();
        layoutPlayerInventorySlots();
    }

    protected void addMainSlots()
    {
        int i = 0;
        int slotIdx = 0;
        ProcessorSlots slots = processor.getSlotsConfig();
        for(int[] pos: slots.getSlotPositions()) {
            if(slots.getSlotType(i).contains("item")) {
                int idx = slotIdx;
                if(!processor.isSlotHidden(idx+slots.getInputFluids())) {
                    blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                        NCSlotItemHandler slotItemHandler = new NCSlotItemHandler(h, idx, pos[0], pos[1]);
                        slotItemHandler.allowed(blockEntity.getAllowedItems(idx));
                        addSlot(slotItemHandler);
                    });
                }
                slotIdx++;
            }
            i++;
        }
    }

    protected void processorSlots() {
        addMainSlots();
        int ux = 154;
        int i = 0;

        if(getProcessor().supportEnergyUpgrade) {
            int idx = i;
            addSlot(new NCSlotItemHandler(blockEntity.upgradesHandler, idx, ux, 77)
                    .allowed(NC_ITEMS.get("upgrade_energy").get()));
            i++;
            ux -= 18;
        }

        if(getProcessor().supportSpeedUpgrade) {
            int idx = i;
            addSlot(new NCSlotItemHandler(blockEntity.upgradesHandler, idx, ux, 77)
                    .allowed(NC_ITEMS.get("upgrade_speed").get(), NC_ITEMS.get("upgrade_stack").get()));
            ux -= 18;
        }

        if(getProcessor().supportsCatalyst) {
            addSlot(new NCSlotItemHandler(blockEntity.catalystHandler, 0, ux, 77));
        }
    }

    public int inputSlots() {
        return processor.getSlotsConfig().getInputItems();
    }

    public int allProcessorSlots() {
        int i = processor.getSlotsConfig().getInputItems() + processor.getSlotsConfig().getOutputItems();
        if(processor.supportEnergyUpgrade) {
            i++;
        }
        if(processor.supportSpeedUpgrade) {
            i++;
        }
        if(processor.supportsCatalyst) {
            i++;
        }
        return i;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int index) {
        Slot slot = this.slots.get(index);
        int maxSlotId = 35+allProcessorSlots();
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack itemstack = stack.copy();
        if(slot instanceof NCSlotItemHandler) {
            if (!this.moveItemStackTo(stack, allProcessorSlots(), maxSlotId, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, inputSlots(), false)) {
                if (!this.moveItemStackTo(stack, inputSlots()+outputSlots(), allProcessorSlots(), false)) {
                    return ItemStack.EMPTY;
                }
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

        return itemstack;
    }

    private int outputSlots() {
        return processor.getSlotsConfig().getOutputItems();
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
        int topRow = 154;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos()),
                playerEntity,
                NCProcessors.PROCESSORS.get(name).get()
        );
    }

    public Component getTitle() {
        return Component.translatable("block."+MODID+"."+name);
    }

    public IEnergyStorage getEnergy() {
        return blockEntity.energyStorage();
    }

    public double getProgress() {
        return blockEntity.getProgress();
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        return blockEntity.getSlotMode(direction, slotId);
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public int speedMultiplier() {
        return blockEntity.speedMultiplier;
    }

    public int energyMultiplier() {
        return blockEntity.energyMultiplier;
    }

    public int energyPerTick() {
        return blockEntity.energyPerTick;
    }

    public int getRedstoneMode() {
        return blockEntity.redstoneMode;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
