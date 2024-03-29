package igentuman.nc.container;

import igentuman.nc.block.entity.ContainerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_CONTAINER;

public class StorageContainerContainer<T extends AbstractContainerMenu> extends AbstractContainerMenu {
   private final ContainerBE blockEntity;
   private final Player playerEntity;
   private final IItemHandler playerInventory;
   private final IItemHandler containerInventory;


   public StorageContainerContainer(int pContainerId, BlockPos pos, Inventory pPlayerInventory) {
      super(STORAGE_CONTAINER.get(), pContainerId);
      blockEntity = (ContainerBE) pPlayerInventory.player.getCommandSenderWorld().getBlockEntity(pos);
      this.playerEntity = pPlayerInventory.player;
      this.playerInventory = new InvWrapper(pPlayerInventory);
       assert blockEntity != null;
       this.containerInventory = blockEntity.getItemHandler().orElse(null);
       int idx = 0;
      int i = getRows();
      int j = getColls();

      int x = 0;
      int y = 0;

      for(int k = 0; k < i; ++k) {
         for(int l = 0; l < j; ++l) {
            x = 5 + l * 18;
            y = 5 + k * 18;
            this.addSlot(new SlotItemHandler(containerInventory, idx++, x, y));
         }
      }
      int xShift = 5;
      switch (j) {
         case 12 -> xShift = 32;
         case 13 -> xShift = 41;
      }
      y += 24;
      for(int i1 = 0; i1 < 3; ++i1) {
         for(int k1 = 0; k1 < 9; ++k1) {
            this.addSlot(new Slot(pPlayerInventory, k1 + i1 * 9 + 9, xShift + k1 * 18, y + i1 * 18));
         }
      }
      y += 18*3+2;
      for(int j1 = 0; j1 < 9; ++j1) {
         this.addSlot(new Slot(pPlayerInventory, j1, xShift + j1 * 18, y));
      }

   }

   public int getColls() {
        return blockEntity.getColls();
   }

   public int getRows() {
        return blockEntity.getRows();
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return true;
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex < this.containerInventory.getSlots()) {
            if (!this.moveItemStackTo(itemstack1, this.containerInventory.getSlots(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.containerInventory.getSlots(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
   }

    public String getTier() {
        return blockEntity.getTier();
    }
}