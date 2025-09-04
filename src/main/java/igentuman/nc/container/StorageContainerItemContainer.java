package igentuman.nc.container;

import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.UUID;

import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_ITEM_CONTAINER;

public class StorageContainerItemContainer<T extends AbstractContainerMenu> extends AbstractContainerMenu {

    private final Player playerEntity;
    private final IItemHandler playerInventory;
    private final IItemHandler containerInventory;
    private final int playerSlot;
    private final UUID uuid;
    private final String tier;
    private final int rows;
    private final int colls;

    public StorageContainerItemContainer(int pContainerId, BlockPos pos, Inventory pPlayerInventory, int slot) {
        super(STORAGE_ITEM_CONTAINER.get(), pContainerId);
        this.playerSlot = slot;
        this.playerEntity = pPlayerInventory.player;
        this.playerInventory = new InvWrapper(pPlayerInventory);

        ItemStack stack = slot == 40 ? pPlayerInventory.offhand.get(0) : pPlayerInventory.items.get(slot);
        if (stack.getItem() instanceof ContainerBlockItem containerBlockItem) {
            this.containerInventory = containerBlockItem.getInventory(stack);
            uuid = containerBlockItem.getUUID(stack);
            rows = getRows(containerBlockItem);
            colls = getColls(containerBlockItem);
            tier = containerBlockItem.getTier();
        } else {
            this.containerInventory = null;
            uuid = UUID.randomUUID();
            tier = "none";
            rows = 0;
            colls = 0;
            return;
        }
        int idx = 0;
        int x = 0;
        int y = 0;

        for(int k = 0; k < rows; ++k) {
         for(int l = 0; l < colls; ++l) {
            x = 5 + l * 18;
            y = 5 + k * 18;
            this.addSlot(new SlotItemHandler(containerInventory, idx++, x, y));
         }
        }
        int xShift = 5;
        switch (colls) {
         case 12 -> xShift = 32;
         case 13 -> xShift = 41;
        }
        y += 23;
        for(int i1 = 0; i1 < 3; ++i1) {
         for(int k1 = 0; k1 < 9; ++k1) {
            this.addSlot(new Slot(pPlayerInventory, k1 + i1 * 9 + 9, xShift + k1 * 18, y + i1 * 18));
         }
        }
        y += 18*3+4;
        for(int j1 = 0; j1 < 9; ++j1) {
         this.addSlot(new Slot(pPlayerInventory, j1, xShift + j1 * 18, y));
        }

   }

    public int getColls() {
        return colls;
    }

    public int getRows() {
        return rows;
    }

   public int getColls(ContainerBlockItem containerBlockItem) {
        return containerBlockItem.getColls();
   }

   public int getRows(ContainerBlockItem containerBlockItem) {
        return containerBlockItem.getRows();
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      ItemStack stack = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
       if(stack.getItem() instanceof ContainerBlockItem containerItem) {
           return containerItem.getUUID(stack).equals(uuid);
       }
       return false;
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
          if(pPlayer.getItemInHand(InteractionHand.MAIN_HAND).equals(itemstack1)) {
              return ItemStack.EMPTY;
          }
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

    public String getTier() {
        return tier;
    }

    public boolean isMagnetModeEnabled() {
        ItemStack stack = playerEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        if(stack.getItem() instanceof ContainerBlockItem containerBlockItem) {
            return containerBlockItem.isMagnetModeEnabled(stack);
        }
        return false;
    }
}