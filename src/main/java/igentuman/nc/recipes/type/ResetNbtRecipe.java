package igentuman.nc.recipes.type;

import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.item.BatteryItem;
import igentuman.nc.item.ProcessorBlockItem;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


@NothingNullByDefault
public class ResetNbtRecipe extends SpecialRecipe {

    public ResetNbtRecipe(ResourceLocation id) {
        super(id);
    }


    @Override
    public boolean matches(CraftingInventory inv, World world) {
        ItemStack targetStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if(targetStack != ItemStack.EMPTY && !inv.getItem(i).isEmpty()) {
                return false; //only allow 1 item
            }
            if(inv.getItem(i).getItem() instanceof BatteryBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof BatteryItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof ProcessorBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }
        }
        return targetStack != ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack targetStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if(targetStack != ItemStack.EMPTY && !inv.getItem(i).isEmpty()) {
                return ItemStack.EMPTY; //only allow 1 item
            }
            if(inv.getItem(i).getItem() instanceof BatteryBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof BatteryItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof ProcessorBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }
        }

        ItemStack result = targetStack.copy();
        result.setTag(new CompoundNBT());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
      //  return NcRecipeSerializers.RESET_NBT.get();
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();

    }
}