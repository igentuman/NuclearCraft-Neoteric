package igentuman.nc.container;

import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberPortBE;
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
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.COLLISION_CHAMBER_PORT_CONTAINER;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.energy2Display;

public class CollisionChamberPortContainer extends AbstractContainerMenu {
    protected final CollisionChamberPortBE portBE;
    protected final Player playerEntity;
    protected String name = "collision_chamber_port";
    private int slotIndex = 0;
    protected IItemHandler playerInventory;

    public CollisionChamberPortContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(COLLISION_CHAMBER_PORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory = new InvWrapper(playerInventory);
        portBE = (CollisionChamberPortBE) playerEntity.getCommandSenderWorld().getExistingBlockEntity(pos);
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

    public byte getPortRole() {
        return portBE.getPortRole();
    }

    public boolean isInput() {
        return portBE.isInput();
    }

    public boolean isOutput() {
        return portBE.isOutput();
    }

    public boolean hasParticle() {
        return portBE.hasParticle();
    }

    public ParticleStack getParticleStack() {
        return portBE.getParticleStack();
    }

    public ParticleStack getSecondaryParticleStack() {
        CollisionChamberControllerBE ctrl = portBE.controller();
        if (ctrl == null) return null;
        return ctrl.particleStorageB.getClientParticleStack();
    }

    public ParticleStack getOutputParticle(int i) {
        CollisionChamberControllerBE ctrl = portBE.controller();
        if (ctrl == null || !ctrl.hasRecipe()) return null;
        CollisionChamberControllerBE.Recipe r = (CollisionChamberControllerBE.Recipe) ctrl.recipeInfo().recipe();
        return r.outputParticles.length > i ? r.outputParticles[i] : null;
    }
}
