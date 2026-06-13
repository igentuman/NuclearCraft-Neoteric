package igentuman.nc.container;

import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberPortBE;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.DECAY_CHAMBER_PORT_CONTAINER;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.energy2Display;

public class DecayChamberPortContainer extends AbstractContainerMenu {
    protected final DecayChamberPortBE portBE;
    protected final Player playerEntity;
    protected String name = "decay_chamber_port";
    private int slotIndex = 0;
    protected IItemHandler playerInventory;

    public DecayChamberPortContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(DECAY_CHAMBER_PORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory = new InvWrapper(playerInventory);
        portBE = (DecayChamberPortBE) playerEntity.getCommandSenderWorld().getExistingBlockEntity(pos);
        slotIndex = 0;
        layoutPlayerInventorySlots();
    }

    public BlockPos getPosition() {
        return portBE.getBlockPos();
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        if (portBE.controller() == null) return ItemStack.EMPTY;
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (!this.moveItemStackTo(stack, slots.size() - 2, slots.size(), true)) {
                return ItemStack.EMPTY;
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

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(portBE.getLevel(), portBE.getBlockPos()),
                playerEntity,
                PARTICLE_CHAMBER_BLOCKS.get(name).get()
        );
    }

    public Component getTitle() {
        return __("block." + MODID + "." + name);
    }

    private void addSlotRange(IItemHandler handler, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, slotIndex, x, y));
            x += dx;
            slotIndex++;
        }
    }

    protected void addSlotBox(IItemHandler handler, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            addSlotRange(handler, x, y, horAmount, dx);
            y += dy;
        }
    }

    protected void layoutPlayerInventorySlots() {
        int leftCol = 8;
        int topRow = 176;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    public int getEnergy() {
        return energy2Display(portBE.getEnergyStored());
    }

    public double getProgress() {
        return portBE.getDepletionProgress();
    }

    public int getMaxEnergy() {
        return energy2Display(portBE.getMaxEnergyStored());
    }

    public int energyPerTick() {
        return energy2Display(portBE.energyPerTick());
    }

    public byte getComparatorMode() {
        return portBE.redstoneMode;
    }

    public byte getAnalogSignalStrength() {
        return portBE.analogSignal;
    }

    public boolean hasParticle() {
        return portBE.hasParticle();
    }

    public ParticleStack getParticleStack() {
        return portBE.getParticleStack();
    }

    public ParticleStack getOutputParticle(int i) {
        DecayChamberControllerBE ctrl = portBE.controller();
        if (ctrl == null || !ctrl.hasRecipe()) return null;
        DecayChamberControllerBE.Recipe r = (DecayChamberControllerBE.Recipe) ctrl.recipeInfo().recipe();
        return r.outputParticles.length > i ? r.outputParticles[i] : null;
    }
}
