package igentuman.nc.container;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.processors.config.ProcessorSlots;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.handler.sided.SlotModePair;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class NCProcessorContainer extends Container {
    protected NCProcessorBE blockEntity;
    protected PlayerEntity playerEntity;
    protected IItemHandler playerInventory;

    public ProcessorPrefab getProcessor() {
        return processor;
    }

    protected ProcessorPrefab processor;

    public int slotIndex = 0;

    protected String name;

    public NCProcessorContainer(@Nullable ContainerType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public NCProcessorContainer(int windowId, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player, String name) {
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
                    blockEntity.getCapability(ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                        addSlot(new SlotItemHandler(h, idx, pos[0], pos[1]));
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
                    .allowed(NCItems.NC_ITEMS.get("upgrade_energy").get()));
            i++;
            ux -= 18;
        }

        if(getProcessor().supportSpeedUpgrade) {
            int idx = i;
            addSlot(new NCSlotItemHandler(blockEntity.upgradesHandler, idx, ux, 77)
                    .allowed(NCItems.NC_ITEMS.get("upgrade_speed").get()));
            ux -= 18;
        }

        if(getProcessor().supportsCatalyst) {
            addSlot(new NCSlotItemHandler(blockEntity.catalystHandler, 0, ux, 77));
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
        int topRow = 154;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    @Override
    public boolean stillValid(@NotNull PlayerEntity playerIn) {
        return true;
/*        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos()),
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

    public TileEntity getBlockEntity() {
        return blockEntity;
    }
}
